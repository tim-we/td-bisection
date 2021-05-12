package bisection;

public class TDExample extends NiceTreeDecomposition {
    public static final Graph EXAMPLE_GRAPH;
    public static final NiceTreeNode EXAMPLE_TREE;

    static {
        Graph g = new Graph(7);
        EXAMPLE_GRAPH = g;

        g.addEdge(1, 2);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(5, 6);
        g.addEdge(5, 7);
        g.addEdge(6, 7);

        NiceTreeNode leaf1 = new LeafNode(new int[]{1, 2, 4});
        NiceTreeNode leaf2 = new LeafNode(new int[]{5, 6, 7});
        NiceTreeNode fn1 = new ForgetNode(leaf1, 1);
        NiceTreeNode fn2 = new ForgetNode(leaf2, 7);
        NiceTreeNode in1 = new IntroduceNode(fn1, 6);
        NiceTreeNode in2 = new IntroduceNode(fn2, 4);
        NiceTreeNode fn3 = new ForgetNode(in1, 2);
        NiceTreeNode in3 = new IntroduceNode(fn3, 5);
        NiceTreeNode jn = new JoinNode(in3, in2);
        NiceTreeNode fn4 = new ForgetNode(jn, 6);
        EXAMPLE_TREE = new IntroduceNode(fn4, 3);
    }

    public TDExample() {
        super(EXAMPLE_GRAPH, EXAMPLE_TREE);
    }
}
