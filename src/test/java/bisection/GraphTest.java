package bisection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphTest {
    @Test
    void testConstructor() {
        Graph graph = new Graph(42);
    }

    @Test
    void testEdgeWeight() {
        Graph graph = new Graph(10);
        graph.setEdgeWeight(0, 1, 1.0);
        graph.setEdgeWeight(1, 2, 1.0);
        graph.setEdgeWeight(2, 3, 1.0);

        assertEquals(0.0, graph.getEdgeWeight(0,2));
        assertEquals(1.0, graph.getEdgeWeight(2,3));
        assertEquals(1.0, graph.getEdgeWeight(1,2));
        assertEquals(1.0, graph.getEdgeWeight(0,1));
    }
}
