package bisection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class NiceTreeDecomposition {
    public Graph graph;
    public TreeNode treeRoot;
    public ArrayList<ArrayList<TreeNode>> layers = new ArrayList<>();
    public int width;

    public NiceTreeDecomposition(Graph graph, TreeNode tree) {
        this.graph = graph;
        this.treeRoot = tree;
        process();
    }

    public NiceTreeDecomposition() {
        Graph g = new Graph(7);
        this.graph = g;

        g.addEdge(0, 1);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(5, 6);

        LeafNode leaf1 = new LeafNode(new int[]{0, 1, 3});
        LeafNode leaf2 = new LeafNode(new int[]{4, 5, 6});
        ForgetNode fn1 = new ForgetNode(leaf1, 0);
        ForgetNode fn2 = new ForgetNode(leaf2, 6);
        IntroduceNode in1 = new IntroduceNode(fn1, 5);
        IntroduceNode in2 = new IntroduceNode(fn2, 3);
        ForgetNode fn3 = new ForgetNode(in1, 1);
        IntroduceNode in3 = new IntroduceNode(fn3, 4);
        JoinNode jn = new JoinNode(in3, in2);
        ForgetNode fn4 = new ForgetNode(jn, 5);
        IntroduceNode in4 = new IntroduceNode(fn4, 2);
        treeRoot = in4;

        process();
    }

    private void process() {
        assert (treeRoot != null);
        ArrayList<TreeNode> currentLayer = new ArrayList<>(1);
        currentLayer.add(treeRoot);
        int nonLeafNodes = treeRoot instanceof LeafNode ? 0 : 1;

        while (nonLeafNodes > 0) {
            layers.add(currentLayer);
            ArrayList<TreeNode> nextLayer = new ArrayList<>(nonLeafNodes + 1);
            nonLeafNodes = 0;
            for (TreeNode node : currentLayer) {
                width = Math.max(width, node.bag.length - 1);

                if (node instanceof IntroduceNode) {
                    nextLayer.add(((IntroduceNode) node).child);
                    nonLeafNodes++;
                } else if (node instanceof ForgetNode) {
                    nextLayer.add(((ForgetNode) node).child);
                    nonLeafNodes++;
                } else if (node instanceof JoinNode) {
                    nextLayer.add(((JoinNode) node).leftChild);
                    nextLayer.add(((JoinNode) node).rightChild);
                    nonLeafNodes += 2;
                }
            }
            currentLayer = nextLayer;
        }
    }

    private static abstract class TreeNode {
        // contains nodes in ascending order
        int[] bag;
    }

    public static class LeafNode extends TreeNode {
        LeafNode(int[] bag) {
            bag = bag.clone();
            Arrays.sort(bag);
            this.bag = bag;
        }
    }

    public static class IntroduceNode extends TreeNode {
        TreeNode child;
        int newVertex;
        int childVertexIndex = -1;

        IntroduceNode(TreeNode child, int newVertex) {
            if (IntStream.of(child.bag).anyMatch(v -> v == newVertex)) {
                throw new IllegalArgumentException("Invalid introduce node!");
            }
            this.bag = new int[child.bag.length + 1];
            this.newVertex = newVertex;
            this.child = child;
            for (int i = 0; i < child.bag.length; i++) {
                if (childVertexIndex < 0) {
                    if (child.bag[i] > newVertex) {
                        this.bag[i] = newVertex;
                        this.bag[i + 1] = child.bag[i];
                        this.childVertexIndex = i;
                    } else {
                        this.bag[i] = child.bag[i];
                    }
                } else {
                    this.bag[i + 1] = child.bag[i];
                }
            }
            if (this.childVertexIndex < 0) {
                this.bag[child.bag.length] = newVertex;
                this.childVertexIndex = child.bag.length;
            }
        }
    }

    public static class ForgetNode extends TreeNode {
        TreeNode child;
        int forgottenVertex;
        int vertexChildIndex = -1;

        ForgetNode(TreeNode child, int forgottenVertex) {
            if (IntStream.of(child.bag).noneMatch(v -> v == forgottenVertex)) {
                throw new IllegalArgumentException("Invalid forget node!");
            }
            this.bag = new int[child.bag.length - 1];
            this.forgottenVertex = forgottenVertex;
            this.child = child;
            for (int i = 0; i < child.bag.length; i++) {
                if (this.vertexChildIndex < 0) {
                    if (child.bag[i] == forgottenVertex) {
                        this.vertexChildIndex = i;
                    } else {
                        this.bag[i] = child.bag[i];
                    }
                } else {
                    this.bag[i - 1] = child.bag[i];
                }
            }
            assert (this.vertexChildIndex >= 0);
        }
    }

    public static class JoinNode extends TreeNode {
        TreeNode leftChild, rightChild;

        JoinNode(TreeNode left, TreeNode right) {
            leftChild = left;
            rightChild = right;
            bag = left.bag;
            if (!Arrays.equals(left.bag, right.bag)) {
                throw new IllegalArgumentException(
                        "Invalid join node!\n"
                                + " -> left = " + Arrays.toString(left.bag)
                                + " -> right = " + Arrays.toString(right.bag));
            }
        }
    }
}
