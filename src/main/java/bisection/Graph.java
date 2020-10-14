package bisection;

public class Graph {
    private int vertices;
    private double[][] edgeWeights;

    public Graph(int numVertices) {
        if(numVertices < 2) {
            throw new IllegalArgumentException("numVertices should be > 1");
        }
        this.vertices = numVertices;
        this.edgeWeights = new double[numVertices - 1][];
        for(int u=0; u < numVertices-1; u++) {
            this.edgeWeights[u] = new double[numVertices - u - 1];
        }
    }

    public void setEdgeWeight(int u, int v, double weight) {
        if(u < 0 || u >= vertices) {
            throw new IllegalArgumentException("invalid index u: " + u);
        }
        if(v < 0 || v >= vertices) {
            throw new IllegalArgumentException("invalid index v: " + v);
        }
        if(u == v) {
            throw new IllegalArgumentException("self-loops are not supported");
        }

        int i = Math.min(u,v);
        int j = Math.max(u,v) - i - 1;

        assert(j >= 0);

        edgeWeights[i][j] = weight;
    }

    public void addEdge(int u, int v) {
        setEdgeWeight(u, v, 1.0);
    }

    public double getEdgeWeight(int u, int v) {
        assert(u < v);
        assert(0 <= u && v < vertices);
        return edgeWeights[u][v-u-1];
    }

    public double getEdgeWeightSafe(int u, int v) {
        if(u == v) {
            System.out.println("Warning: u=v="+u);
            return 0.0;
        }

        int i = Math.min(u,v);
        int j = Math.max(u,v);

        return getEdgeWeight(i,j);
    }
}
