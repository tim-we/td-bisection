package visualization;

import bisection.NiceTreeDecomposition;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.List;

public class Visualizer {
    public static void display(NiceTreeDecomposition td) {
        System.setProperty("org.graphstream.ui", "swing");

        final int n = td.graph.vertices;
        SingleGraph dg = new SingleGraph("Graph");

        for (int v = 1; v <= n; v++) {
            String label = Integer.toString(v);
            Node vn = dg.addNode(label);
            vn.setAttribute("ui.label", "Vertex " + label);
            vn.setAttribute("ui.class", "graph");
        }
        for (int u = 1; u <= n; u++) {
            for (int v = u + 1; v <= td.graph.vertices; v++) {
                final double w = td.graph.getEdgeWeightSafe(u, v);
                if (w != 0.0) {
                    dg.addEdge(u + "-" + v, Integer.toString(u), Integer.toString(v));
                }
            }
        }

        SingleGraph dt = new SingleGraph("Tree Decomposition");

        for (int l = td.layers.size() - 1; l >= 0; l--) {
            List<NiceTreeDecomposition.NiceTreeNode> layer = td.layers.get(l);
            for (NiceTreeDecomposition.NiceTreeNode node : layer) {
                String label = node.toString();
                if (node == td.treeRoot) {
                    label = "Root: " + label;
                }

                int width = Math.max(80, 7 * label.length());
                Node vn = dt.addNode("Node" + node.id);
                vn.setAttribute("ui.label", label);
                vn.setAttribute("ui.class", "tree");
                vn.setAttribute("ui.style", "size: " + width + "px, 20px;");

                if (node instanceof NiceTreeDecomposition.IntroduceNode) {
                    vn.setAttribute("ui.class", "intro");
                    NiceTreeDecomposition.NiceTreeNode child = ((NiceTreeDecomposition.IntroduceNode) node).child;
                    dt.addEdge(
                            "Edge " + node.id + "-" + child.id,
                            "Node" + node.id,
                            "Node" + child.id
                    );
                } else if (node instanceof NiceTreeDecomposition.ForgetNode) {
                    vn.setAttribute("ui.class", "forget");
                    NiceTreeDecomposition.NiceTreeNode child = ((NiceTreeDecomposition.ForgetNode) node).child;
                    dt.addEdge(
                            "Edge " + node.id + "-" + child.id,
                            "Node" + node.id,
                            "Node" + child.id
                    );
                } else if (node instanceof NiceTreeDecomposition.JoinNode) {
                    vn.setAttribute("ui.class", "join");
                    NiceTreeDecomposition.NiceTreeNode left = ((NiceTreeDecomposition.JoinNode) node).leftChild;
                    dt.addEdge(
                            "Edge " + node.id + "-" + left.id,
                            "Node" + node.id,
                            "Node" + left.id
                    );
                    NiceTreeDecomposition.NiceTreeNode right = ((NiceTreeDecomposition.JoinNode) node).rightChild;
                    dt.addEdge(
                            "Edge " + node.id + "-" + right.id,
                            "Node" + node.id,
                            "Node" + right.id
                    );
                }
            }
        }

        dt.setAttribute("ui.stylesheet",
                "node { shape: box; fill-color: rgb(200,200,200); }" +
                        "node.forget { fill-color: red; }" +
                        "node.intro { fill-color: rgb(0, 200, 255); }" +
                        "node.join { fill-color: orange; }"
        );

        dg.display();
        dt.display();
    }
}
