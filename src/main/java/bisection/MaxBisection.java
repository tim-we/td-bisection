package bisection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bisection.NiceTreeDecomposition.*;

public class MaxBisection {

    public static double compute(NiceTreeDecomposition td) {
        assert (td.layers.size() > 0);

        // keep array from previous layer
        Map<NiceTreeNode, double[][]> previousLayerData = Collections.emptyMap();

        // iterate over tree layers (bottom-up)
        for (int layer = td.layers.size() - 1; layer >= 0; layer--) {
            List<NiceTreeNode> currentLayer = td.layers.get(layer);
            Map<NiceTreeNode, double[][]> currentLayerData = new HashMap<>(currentLayer.size());

            // TODO: parallelize
            // iterate over nodes in layer
            for (NiceTreeNode node : currentLayer) {
                final int N = node.numSubsets();
                final int Fi = node.numForgotten;
                double[][] data = new double[node.numSubsets()][node.numForgotten + 1];
                currentLayerData.put(node, data);

                // compute B(i,l,S) for each l and S
                if (node instanceof LeafNode) {
                    final double[] weights = computeWeightsLeafOrJoin(node, td.graph);

                    for (int s = 0; s < N; s++) {
                        data[s][0] = weights[s];
                    }
                } else if (node instanceof IntroduceNode) {
                    final IntroduceNode intro = (IntroduceNode) node;
                    final double[][] childData = previousLayerData.get(intro.child);
                    double[] weights = computeWeightsIntroduce(intro, td.graph);

                    for (int s = 0; s < N; s++) {
                        final int soc = intro.getChildSubset(
                                // X_i \ S if v in S else S
                                intro.subsetContainsNewVertex(s) ? node.complement(s) : s
                        );
                        final int cs = intro.getChildSubset(s);
                        final double w = weights[soc];

                        for (int l = 0; l <= Fi; l++) {
                            data[s][l] = childData[cs][l] + w;
                        }
                    }
                } else if (node instanceof ForgetNode) {
                    final ForgetNode forget = (ForgetNode) node;
                    final double[][] childData = previousLayerData.get(forget.child);
                    int cs1, cs2;

                    for (int s = 0; s < N; s++) {
                        cs1 = forget.getChildSubset(s, false);
                        cs2 = forget.getChildSubset(s, true);
                        for (int l = 0; l <= Fi; l++) {
                            double b1 = l < Fi ? childData[cs1][l] : Double.NEGATIVE_INFINITY;
                            double b2 = l > 0 ? childData[cs2][l - 1] : Double.NEGATIVE_INFINITY;
                            data[s][l] = Math.max(b1, b2);
                        }
                    }
                } else if (node instanceof JoinNode) {
                    final JoinNode join = (JoinNode) node;
                    final double[][] left = previousLayerData.get(join.leftChild);
                    final double[][] right = previousLayerData.get(join.rightChild);
                    final double[] weights = computeWeightsLeafOrJoin(node, td.graph);
                    final int Fj = join.leftChild.numForgotten;
                    final int Fk = join.rightChild.numForgotten;

                    for (int s = 0; s < N; s++) {
                        for (int l = 0; l <= Fi; l++) {
                            double maxb = Double.NEGATIVE_INFINITY;

                            final int start = Math.max(0, l - Fk);
                            final int end = Math.min(l, Fj);
                            for (int l1 = start; l1 <= end; l1++) {
                                final int l2 = l - l1;
                                double b = left[s][l1] + right[s][l2] - weights[s];
                                if (b > maxb) {
                                    maxb = b;
                                }
                            }

                            data[s][l] = maxb;
                        }
                    }
                } else {
                    assert (false);
                }
            }

            previousLayerData = currentLayerData;
        }

        // final max over root (eq. 7)
        final double[][] rootData = previousLayerData.get(td.treeRoot);
        final int N = td.treeRoot.numSubsets();
        final int Fr = td.treeRoot.numForgotten;
        final int Yrh = (Fr + td.treeRoot.size()) / 2;
        double size = Double.NEGATIVE_INFINITY;

        for (int s = 0; s < N; s++) {
            final int l = Yrh - Integer.bitCount(s);
            if(l < 0 || Fr < l) {
                continue;
            }
            double b = rootData[s][l];
            if (b > size) {
                size = b;
            }
        }

        return size;
    }

    public static double[] computeWeightsIntroduce(IntroduceNode node, Graph graph) {
        final int n = node.child.numSubsets();
        final int v = node.newVertex;
        final int[] childBag = node.child.bag;
        double[] di = new double[n];

        // iterate over subsets
        for(int s=1; s<n; s++) {
            final int bitDiff = s ^ (s-1);
            final int newVertexMask = s & bitDiff;
            assert(Integer.bitCount(newVertexMask) == 1);
            final int uIndex = Integer.numberOfTrailingZeros(newVertexMask);
            final int u = childBag[uIndex];
            // s_known = S, s = S \cup {u}
            final int s_known = s & ~newVertexMask;
            di[s] = di[s_known] + graph.getEdgeWeightSafe(u, v);
        }

        return di;
    }

    public static double[] computeWeightsLeafOrJoin(NiceTreeNode node, Graph graph) {
        assert (node instanceof LeafNode || node instanceof JoinNode);
        final int n = node.numSubsets();
        double[] ws = new double[n];

        // iterate over subsets
        for(int s=1; s<n; s++) {
            final int bitDiff = s ^ (s-1);
            final int newVertexMask = s & bitDiff;
            assert(Integer.bitCount(newVertexMask) == 1);
            final int vIndex = Integer.numberOfTrailingZeros(newVertexMask);
            final int v = node.bag[vIndex];
            // s_known = S, s = S \cup {v}
            final int s_known = s & ~newVertexMask;
            final double w = ws[s_known];

            // w1 = w({v}, S) and w2 = w({v}, X_i \ (S \cup {v}))
            double w1 = 0.0, w2 = 0.0;

            // iterate over X_i \ {v} to compute weight diff
            for (int uIndex = 0; uIndex < node.bag.length; uIndex++) {
                // TODO: improve (/2)
                if (uIndex == vIndex) {
                    continue;
                }

                final int u = node.bag[uIndex];
                final double wuv = graph.getEdgeWeightSafe(u, v);

                // is u in S?
                if ((s_known & (1<<uIndex)) != 0) {
                    w1 += wuv;
                } else {
                    w2 += wuv;
                }
            }

            ws[s] = w - w1 + w2;
        }

        return ws;
    }
}
