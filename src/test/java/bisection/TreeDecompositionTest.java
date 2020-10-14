package bisection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class TreeDecompositionTest {
    @Test
    void testValidTreeDecomposition() {
        Graph g = new Graph(7);
        g.addEdge(0, 1);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(5, 6);

        NiceTreeDecomposition.LeafNode leaf1 = new NiceTreeDecomposition.LeafNode(new int[]{0, 1, 3});
        NiceTreeDecomposition.LeafNode leaf2 = new NiceTreeDecomposition.LeafNode(new int[]{4, 5, 6});
        NiceTreeDecomposition.ForgetNode fn1 = new NiceTreeDecomposition.ForgetNode(leaf1, 0);
        NiceTreeDecomposition.ForgetNode fn2 = new NiceTreeDecomposition.ForgetNode(leaf2, 6);
        NiceTreeDecomposition.IntroduceNode in1 = new NiceTreeDecomposition.IntroduceNode(fn1, 5);
        NiceTreeDecomposition.IntroduceNode in2 = new NiceTreeDecomposition.IntroduceNode(fn2, 3);
        NiceTreeDecomposition.ForgetNode fn3 = new NiceTreeDecomposition.ForgetNode(in1, 1);
        NiceTreeDecomposition.IntroduceNode in3 = new NiceTreeDecomposition.IntroduceNode(fn3, 4);
        NiceTreeDecomposition.JoinNode jn = new NiceTreeDecomposition.JoinNode(in3, in2);
        NiceTreeDecomposition.ForgetNode fn4 = new NiceTreeDecomposition.ForgetNode(jn, 5);
        NiceTreeDecomposition.IntroduceNode in4 = new NiceTreeDecomposition.IntroduceNode(fn4, 2);

        NiceTreeDecomposition td = new NiceTreeDecomposition(g, in4);

        assertEquals(2, td.width);
        assertTrue(td.layers.size() > 0);
        assertTrue(td.layers.get(td.layers.size() - 1).contains(leaf1));
        assertFalse(td.layers.get(td.layers.size() - 1).contains(leaf2));
    }

    @Test
    void testInvalidNodes() {
        NiceTreeDecomposition.LeafNode leaf = new NiceTreeDecomposition.LeafNode(new int[]{0, 1, 2});
        NiceTreeDecomposition.LeafNode leaf2 = new NiceTreeDecomposition.LeafNode(new int[]{3, 4, 5});
        // invalid introduce node
        try {
            NiceTreeDecomposition.IntroduceNode intro = new NiceTreeDecomposition.IntroduceNode(leaf, 1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        // invalid forget node
        try {
            NiceTreeDecomposition.ForgetNode fn = new NiceTreeDecomposition.ForgetNode(leaf, 3);
            fail();
        } catch (IllegalArgumentException e) {
        }
        // invalid join node
        try {
            NiceTreeDecomposition.JoinNode fn = new NiceTreeDecomposition.JoinNode(leaf, leaf2);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
