package bisection;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Graph {
    public final int vertices;
    private final double[][] edgeWeights;
    private static final Pattern NEWLINE = Pattern.compile("\n");

    public Graph(int numVertices) {
        if (numVertices < 2) {
            throw new IllegalArgumentException("numVertices should be > 1");
        }
        this.vertices = numVertices;
        this.edgeWeights = new double[numVertices - 1][];
        for (int u = 0; u < numVertices - 1; u++) {
            this.edgeWeights[u] = new double[numVertices - u - 1];
        }
    }

    public static Graph fromString(String grStr) {
        Stream<String> lines = NEWLINE.splitAsStream(grStr);
        return fromLines(lines);
    }

    public static Graph fromFile(Path file) throws IOException {
        try (Stream<String> lineStream = Files.lines(file, Charset.defaultCharset())) {
            return fromLines(lineStream);
        }
    }

    private static Graph fromLines(Stream<String> lineStream) {
        Iterator<String> lines = lineStream.filter(line -> !line.startsWith("c")).iterator();
        String firstLine = lines.next();
        String[] parts = firstLine.split("\\s+");
        if (!firstLine.startsWith("p tw ") || parts.length != 4) {
            throw new IllegalArgumentException("Invalid graph file");
        }
        final int n = Integer.parseInt(parts[2], 10);
        final int numEdges = Integer.parseInt(parts[3], 10);
        Graph graph = new Graph(n);

        for (int i = 0; i < numEdges; i++) {
            String edgeLine = lines.next();
            parts = edgeLine.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid edge \"" + edgeLine + "\"");
            }
            final int start = Integer.parseInt(parts[0], 10);
            final int end = Integer.parseInt(parts[1], 10);
            assert (start < n);
            assert (0 < start && end <= n);

            graph.setEdgeWeight(start, end, 1.0);
        }

        assert !lines.hasNext();

        return graph;
    }

    public void setEdgeWeight(int u, int v, double weight) {
        if (u <= 0 || u > vertices) {
            throw new IllegalArgumentException("invalid index u: " + u);
        }
        if (v <= 0 || v > vertices) {
            throw new IllegalArgumentException("invalid index v: " + v);
        }
        if (u == v) {
            throw new IllegalArgumentException("self-loops are not supported");
        }

        int i = Math.min(u, v) - 1;
        int j = Math.max(u, v) - i - 2;

        assert (j >= 0);

        edgeWeights[i][j] = weight;
    }

    public void addEdge(int u, int v) {
        setEdgeWeight(u, v, 1.0);
    }

    public double getEdgeWeight(int u, int v) {
        assert (u < v);
        assert (0 < u && v <= vertices);
        return edgeWeights[u - 1][v - u - 1];
    }

    public double getEdgeWeightSafe(int u, int v) {
        if (u == v) {
            System.out.println("Warning: u=v=" + u);
            return 0.0;
        }

        int i = Math.min(u, v);
        int j = Math.max(u, v);

        return getEdgeWeight(i, j);
    }
}
