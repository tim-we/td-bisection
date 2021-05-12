package bisection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TreeDecompositionTest {
    @Test
    void testPACEFormat() {
        TreeDecomposition td = TreeDecomposition.fromString(
                "c This file describes a tree decomposition with 4 bags, width 2, for a graph with 5 vertices\n" +
                        "s td 4 3 5\n" +
                        "b 1 1 2 3\n" +
                        "b 2 2 3 4\n" +
                        "b 3 3 4 5\n" +
                        "b 4\n" +
                        "1 2\n" +
                        "2 3\n" +
                        "2 4"
        );
        assertNotNull(td.root);
        assertEquals(2, td.width);
        assertEquals(1, td.root.childNodes.size());
        assertEquals(2, td.root.childNodes.get(0).childNodes.size());
    }

    @Test
    void testNormalize() {
        // https://en.wikipedia.org/wiki/File:Tree_decomposition.svg
        TreeDecomposition td = TreeDecomposition.fromString(
                "s td 6 3 8\n" +
                        "b 1 2 3 5\n" +
                        "b 2 1 2 3\n" +
                        "b 3 3 4 5\n" +
                        "b 4 2 5 7\n" +
                        "b 5 2 6 7\n" +
                        "b 6 5 7 8\n" +
                        "1 2\n" +
                        "1 3\n" +
                        "1 4\n" +
                        "4 5\n" +
                        "4 6"
        );
        td.graph = Graph.fromString(
                "p tw 8 13\n" +
                        "1 2\n" +
                        "1 3\n" +
                        "2 3\n" +
                        "2 5\n" +
                        "2 6\n" +
                        "2 7\n" +
                        "3 4\n" +
                        "3 5\n" +
                        "4 5\n" +
                        "5 7\n" +
                        "5 8\n" +
                        "6 7\n" +
                        "7 8"
        );

        NiceTreeDecomposition ntd = td.normalize();
        assertArrayEquals(td.root.bag, ntd.treeRoot.bag);
        assertEquals(td.graph, ntd.graph);
        assertTrue(ntd.numBags >= td.countNodes());
    }

    @Test
    void testSuboptimalEdgeOrder() {
        TreeDecomposition td = TreeDecomposition.fromString(
                "s td 6 3 8\n" +
                        "b 1 2 3 5\n" +
                        "b 2 1 2 3\n" +
                        "b 3 3 4 5\n" +
                        "b 4 2 5 7\n" +
                        "b 5 2 6 7\n" +
                        "b 6 5 7 8\n" +
                        "4 5\n" +
                        "4 6\n" +
                        "1 2\n" +
                        "1 3\n" +
                        "1 4\n"

        );
        assertNotNull(td.root);
    }
}
