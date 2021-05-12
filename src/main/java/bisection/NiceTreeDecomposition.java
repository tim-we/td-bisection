package bisection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NiceTreeDecomposition {
    public final Graph graph;
    public final NiceTreeNode treeRoot;
    public final ArrayList<ArrayList<NiceTreeNode>> layers = new ArrayList<>();
    public int width;
    public int numBags;

    public NiceTreeDecomposition(Graph graph, NiceTreeNode tree) {
        this.graph = graph;
        this.treeRoot = tree;
        process();
    }

    public NiceTreeDecomposition(Graph graph) {
        this.graph = graph;
        // trivial tree decomposition
        int[] bag = new int[graph.vertices];
        for (int i = 0; i < bag.length; i++) {
            bag[i] = i+1;
        }
        this.treeRoot = new LeafNode(bag);
        process();
    }

    private void process() {
        assert (treeRoot != null);
        ArrayList<NiceTreeNode> currentLayer = new ArrayList<>(1);
        currentLayer.add(treeRoot);
        layers.add(currentLayer);
        int n = 0;

        while (true) {
            ArrayList<NiceTreeNode> nextLayer = new ArrayList<>(currentLayer.size() + 1);
            n += currentLayer.size();

            for (NiceTreeNode node : currentLayer) {
                width = Math.max(width, node.bag.length - 1);
                if (node.bag.length > 32) {
                    throw new IllegalStateException("Treewidth must be < 32 (was " + width + ")");
                }

                if (node instanceof IntroduceNode) {
                    nextLayer.add(((IntroduceNode) node).child);
                } else if (node instanceof ForgetNode) {
                    nextLayer.add(((ForgetNode) node).child);
                } else if (node instanceof JoinNode) {
                    nextLayer.add(((JoinNode) node).leftChild);
                    nextLayer.add(((JoinNode) node).rightChild);
                }
            }

            if (nextLayer.size() > 0) {
                layers.add(nextLayer);
                currentLayer = nextLayer;
            } else {
                break;
            }
        }

        numBags = n;
    }

    public static abstract class NiceTreeNode {
        // contains nodes in ascending order
        int[] bag;
        int numForgotten = 0;
        public final int id;
        private static int count = 0;

        private NiceTreeNode() {
            id = count;
            count += 1;
        }

        public int numSubsets() {
            assert (bag.length < 32);
            return 1 << bag.length;
        }

        public int complement(int s) {
            int mask = (1 << bag.length) - 1;
            return ~s & mask;
        }

        public int size() {
            return this.bag.length;
        }

        public String subsetToString(int s) {
            return IntStream.range(0, bag.length)
                    .filter(i -> (s & (1 << i)) != 0)
                    .map(i -> bag[i])
                    .mapToObj(i -> ((Integer) i).toString())
                    .collect(Collectors.joining(","));
        }

        public String toString() {
            return "{" + IntStream.of(this.bag)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(",")) + "}";
        }
    }

    public static class LeafNode extends NiceTreeNode {
        LeafNode(int[] bag) {
            bag = bag.clone();
            Arrays.sort(bag);
            this.bag = bag;
        }

        public String toString() {
            return "Leaf " + super.toString();
        }
    }

    public static class IntroduceNode extends NiceTreeNode {
        public final NiceTreeNode child;
        final int newVertex;
        int newVertexIndex = -1;

        IntroduceNode(NiceTreeNode child, int newVertex) {
            if (IntStream.of(child.bag).anyMatch(v -> v == newVertex)) {
                throw new IllegalArgumentException("Invalid introduce node!");
            }
            this.numForgotten = child.numForgotten;
            this.bag = new int[child.bag.length + 1];
            this.newVertex = newVertex;
            this.child = child;
            for (int i = 0; i < child.bag.length; i++) {
                if (newVertexIndex < 0) {
                    if (child.bag[i] > newVertex) {
                        this.bag[i] = newVertex;
                        this.bag[i + 1] = child.bag[i];
                        this.newVertexIndex = i;
                    } else {
                        this.bag[i] = child.bag[i];
                    }
                } else {
                    this.bag[i + 1] = child.bag[i];
                }
            }
            if (this.newVertexIndex < 0) {
                this.bag[child.bag.length] = newVertex;
                this.newVertexIndex = child.bag.length;
            }
        }

        public int getChildSubset(int s) {
            final int mask = (1 << newVertexIndex) - 1;
            return ((s >> 1) & ~mask) | (s & mask);
        }

        public boolean subsetContainsNewVertex(int s) {
            final int mask = (1 << newVertexIndex);
            return (s & mask) != 0;
        }

        public String toString() {
            return "Intro " + newVertex + " " + super.toString();
        }
    }

    public static class ForgetNode extends NiceTreeNode {
        public final NiceTreeNode child;
        final int forgottenVertex;
        int childVertexIndex = -1;

        ForgetNode(NiceTreeNode child, int forgottenVertex) {
            if (IntStream.of(child.bag).noneMatch(v -> v == forgottenVertex)) {
                throw new IllegalArgumentException("Invalid forget node!");
            }
            this.numForgotten = child.numForgotten + 1;
            this.bag = new int[child.bag.length - 1];
            this.forgottenVertex = forgottenVertex;
            this.child = child;
            for (int i = 0; i < child.bag.length; i++) {
                if (this.childVertexIndex < 0) {
                    if (child.bag[i] == forgottenVertex) {
                        this.childVertexIndex = i;
                    } else {
                        this.bag[i] = child.bag[i];
                    }
                } else {
                    this.bag[i - 1] = child.bag[i];
                }
            }
            assert (this.childVertexIndex >= 0);
        }

        public int getChildSubset(int s, boolean includeForgotten) {
            final int mask = (1 << childVertexIndex) - 1;
            int subset = ((s & ~mask) << 1) | (s & mask);
            if (includeForgotten) {
                subset |= 1 << childVertexIndex;
            }
            return subset;
        }

        public String toString() {
            return "Forget " + forgottenVertex + " " + super.toString();
        }
    }

    public static class JoinNode extends NiceTreeNode {
        public final NiceTreeNode leftChild, rightChild;

        JoinNode(NiceTreeNode left, NiceTreeNode right) {
            leftChild = left;
            rightChild = right;
            bag = left.bag;
            // left & right set of forgotten vertices is disjoint!
            numForgotten = left.numForgotten + right.numForgotten;
            if (!Arrays.equals(left.bag, right.bag)) {
                throw new IllegalArgumentException(
                        "Invalid join node!\n"
                                + " -> left  = " + Arrays.toString(left.bag) + "\n"
                                + " -> right = " + Arrays.toString(right.bag));
            }
        }

        public String toString() {
            return "Join " + super.toString();
        }
    }
}
