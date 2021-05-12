package bisection;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bisection.NiceTreeDecomposition.*;
import static java.util.Arrays.stream;

public class TreeDecomposition {
    public Graph graph;
    public int width;
    public TreeNode root;
    private static final Pattern NEWLINE = Pattern.compile("\n");

    public static class TreeNode {
        public int[] bag;
        public List<TreeNode> childNodes = new ArrayList<>(2);
    }

    public static TreeDecomposition fromString(String input) {
        return fromLines(NEWLINE.splitAsStream(input));
    }

    public static TreeDecomposition fromFile(Path file) throws IOException {
        try (Stream<String> lineStream = Files.lines(file, Charset.defaultCharset())) {
            return fromLines(lineStream);
        }
    }

    private static TreeDecomposition fromLines(Stream<String> lineStream) {
        Iterator<String> lines = lineStream.filter(line -> !line.startsWith("c")).iterator();
        String firstLine = lines.next();
        String[] parts = firstLine.split("\\s+");
        if (!firstLine.startsWith("s td ") || parts.length != 5) {
            throw new IllegalArgumentException("Invalid tree decomposition file");
        }
        TreeDecomposition td = new TreeDecomposition();
        final int numBags = Integer.parseInt(parts[2], 10);
        td.width = Integer.parseInt(parts[3], 10) - 1;
        final int numVertices = Integer.parseInt(parts[4], 10);

        TreeNode[] nodes = new TreeNode[numBags];

        // build bags
        for (int i = 0; i < numBags; i++) {
            String bagLine = lines.next();
            parts = bagLine.split("\\s+");
            if (parts.length < 2 || !String.valueOf(i + 1).equals(parts[1])) {
                throw new IllegalArgumentException("Invalid bag \"" + bagLine + "\"");
            }
            nodes[i] = new TreeNode();
            nodes[i].bag = stream(parts).skip(2)
                    .mapToInt(s -> Integer.parseInt(s, 10))
                    .sorted()
                    .toArray();

            assert (nodes[i].bag.length == 0 || nodes[i].bag[nodes[i].bag.length - 1] <= numVertices);
        }

        boolean[] connectedToRoot = new boolean[numBags];
        td.root = nodes[0];
        connectedToRoot[0] = true;

        EdgeIterator edges = new EdgeIterator(lines);

        // build tree
        while (edges.hasNext()) {
            TreeEdge edge = edges.next();
            assert (edge.to <= numBags);

            if (connectedToRoot[edge.from - 1]) {
                if (connectedToRoot[edge.to - 1]) {
                    throw new IllegalStateException("This graph is not a tree.");
                }
                nodes[edge.from - 1].childNodes.add(nodes[edge.to - 1]);
                connectedToRoot[edge.to - 1] = true;
            } else if (connectedToRoot[edge.to - 1]) {
                nodes[edge.to - 1].childNodes.add(nodes[edge.from - 1]);
                connectedToRoot[edge.from - 1] = true;
            } else {
                // can't handle this edge right now, try again later
                edges.add(edge);
            }
        }

        return td;
    }

    public int countNodes() {
        return countNodes(this.root);
    }

    private int countNodes(TreeNode node) {
        int n = 1;
        for (TreeNode child : node.childNodes) {
            n += countNodes(child);
        }
        return n;
    }

    public NiceTreeDecomposition normalize() {
        NiceTreeNode root = normalize(this.root);
        return new NiceTreeDecomposition(this.graph, root);
    }

    private static NiceTreeNode normalize(TreeNode root) {
        assert (root != null);
        final int numChildNodes = root.childNodes.size();

        if (numChildNodes == 0) {
            return new LeafNode(root.bag);
        } else if (numChildNodes == 1) {
            return bridge(root, normalize(root.childNodes.get(0)));
        } else {
            List<NiceTreeNode> nodes = root.childNodes.stream()
                    .map(TreeDecomposition::normalize)
                    .map(n -> bridge(root, n))
                    .collect(Collectors.toList());

            while (nodes.size() > 2) {
                List<NiceTreeNode> parents = new ArrayList<>((nodes.size() + 1) / 2);
                int i;
                for (i = 0; i + 1 < nodes.size(); i += 2) {
                    final NiceTreeNode left = nodes.get(i);
                    final NiceTreeNode right = nodes.get(i + 1);
                    parents.add(new JoinNode(left, right));
                }
                if (i < nodes.size()) {
                    // add last remaining node
                    parents.add(nodes.get(nodes.size() - 1));
                }
                nodes = parents;
            }

            return new JoinNode(nodes.get(0), nodes.get(1));
        }
    }

    private static NiceTreeNode bridge(TreeNode parent, NiceTreeNode child) {
        final Set<Integer> ps = Arrays.stream(parent.bag).boxed().collect(Collectors.toSet());
        final Set<Integer> cs = Arrays.stream(child.bag).boxed().collect(Collectors.toSet());

        Set<Integer> forget = new HashSet<>(cs);
        forget.removeAll(ps);
        Set<Integer> introduce = new HashSet<>(ps);
        introduce.removeAll(cs);

        NiceTreeNode node = child;

        // forget vertices
        for (Integer vertex : forget) {
            node = new ForgetNode(node, vertex);
        }

        // introduce vertices
        for (Integer vertex : introduce) {
            node = new IntroduceNode(node, vertex);
        }

        assert (node.bag.length == parent.bag.length);

        return node;
    }

    private static class EdgeIterator implements Iterator<TreeEdge> {

        private Iterator<String> lines;
        private List<TreeEdge> moreLines = new LinkedList<>();

        EdgeIterator(Iterator<String> lines) {
            assert (lines != null);
            this.lines = lines;
        }

        @Override
        public boolean hasNext() {
            if (lines.hasNext()) {
                return true;
            } else {
                return moreLines.size() > 0;
            }
        }

        @Override
        public TreeEdge next() {
            if (lines.hasNext()) {
                return new TreeEdge(lines.next());
            } else {
                return moreLines.remove(0);
            }
        }

        public void add(TreeEdge edge) {
            moreLines.add(edge);
        }
    }

    private static class TreeEdge {
        int from;
        int to;

        TreeEdge(String edgeLine) {
            String[] parts = edgeLine.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid tree edge \"" + edgeLine + "\"");
            }
            from = Integer.parseInt(parts[0], 10);
            to = Integer.parseInt(parts[1], 10);
            assert (0 < from && from < to);
        }

        void swap() {
            int tmp = from;
            from = to;
            to = tmp;
        }
    }
}
