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
        graph.setEdgeWeight(1, 2, 1.0);
        graph.setEdgeWeight(2, 3, 1.0);
        graph.setEdgeWeight(4, 5, 1.0);

        assertEquals(0.0, graph.getEdgeWeight(1,3));
        assertEquals(1.0, graph.getEdgeWeight(1,2));
        assertEquals(1.0, graph.getEdgeWeight(2,3));
        assertEquals(1.0, graph.getEdgeWeight(4,5));
    }

    @Test
    void testPACEFormat() {
        Graph graph = Graph.fromString("c This file describes a path with five vertices and four edges.\n" +
                "p tw 5 4\n" +
                "1 2\n" +
                "2 3\n" +
                "c we are half-way done with the instance definition.\n" +
                "3 4\n" +
                "4 5");

        assertEquals(1.0, graph.getEdgeWeight(1, 2));
        assertEquals(1.0, graph.getEdgeWeight(2, 3));
        assertEquals(1.0, graph.getEdgeWeight(3, 4));
        assertEquals(1.0, graph.getEdgeWeight(4, 5));
        assertEquals(0.0, graph.getEdgeWeight(1, 3));
    }
}
