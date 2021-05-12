package bisection;

import org.junit.jupiter.api.Test;

import static bisection.NiceTreeDecomposition.*;
import static org.junit.jupiter.api.Assertions.*;

public class NiceTreeDecompositionTest {
    @Test
    void testValidTreeDecomposition() {
        Graph g = TDExample.EXAMPLE_GRAPH;

        NiceTreeNode leaf1 = new LeafNode(new int[]{0, 1, 3});
        NiceTreeNode leaf2 = new LeafNode(new int[]{4, 5, 6});
        NiceTreeNode fn1 = new ForgetNode(leaf1, 0);
        NiceTreeNode fn2 = new ForgetNode(leaf2, 6);
        NiceTreeNode in1 = new IntroduceNode(fn1, 5);
        NiceTreeNode in2 = new IntroduceNode(fn2, 3);
        NiceTreeNode fn3 = new ForgetNode(in1, 1);
        NiceTreeNode in3 = new IntroduceNode(fn3, 4);
        NiceTreeNode jn = new JoinNode(in3, in2);
        NiceTreeNode fn4 = new ForgetNode(jn, 5);
        NiceTreeNode in4 = new IntroduceNode(fn4, 2);

        NiceTreeDecomposition td = new NiceTreeDecomposition(g, in4);

        assertEquals(2, td.width);
        assertEquals(8, td.layers.size());

        for(NiceTreeNode node : td.layers.get(td.layers.size()-1)) {
            assertTrue(node instanceof LeafNode);
        }

        assertFalse(td.layers.get(td.layers.size() - 1).contains(leaf2));
    }

    @Test
    void testTrivialDecomposition() {
        Graph g = TDExample.EXAMPLE_GRAPH;
        NiceTreeDecomposition td = new NiceTreeDecomposition(g);
        assertEquals(g.vertices-1, td.width);
        assertEquals(1, td.layers.size());
    }

    @Test
    void testInvalidNodes() {
        // valid leaf node
        NiceTreeNode leaf = new LeafNode(new int[]{1, 2, 3});

        // invalid introduce node
        try {
            new IntroduceNode(leaf, 1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        // invalid forget node
        try {
            new ForgetNode(leaf, 4);
            fail();
        } catch (IllegalArgumentException e) {
        }
        // invalid join node
        try {
            NiceTreeNode leaf2 = new LeafNode(new int[]{3, 4, 5});
            new JoinNode(leaf, leaf2);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    void testNumSubsets() {
        NiceTreeNode node = new LeafNode(new int[]{0,1,2});
        assertEquals(8, node.numSubsets());

        node = new LeafNode(new int[]{0});
        assertEquals(2, node.numSubsets());
    }

    @Test
    void testNumForgotten() {
        NiceTreeNode node = new LeafNode(new int[]{0,1,2});
        assertEquals(0, node.numForgotten);

        assertEquals(4, TDExample.EXAMPLE_TREE.numForgotten);
    }

    @Test
    void testSubsetToString() {
        NiceTreeNode node = new LeafNode((new int[]{0,1,2,4,5}));
        assertEquals("0,4", node.subsetToString(1 + 8));
    }

    @Test
    void testChildSubset() {
        NiceTreeNode node = new LeafNode((new int[]{0,1,2,4,5}));
        IntroduceNode in = new IntroduceNode(node, 3);
        ForgetNode fn = new ForgetNode(node, 2);

        int s, cs, cs2;

        // test introduce node
        s = 1 + 4 + 16 + 32; // 0, 2, 4, 5
        cs = in.getChildSubset(s); // 0, 2, 4, 5 ?
        assertEquals(in.subsetToString(s), node.subsetToString(cs));
        assertEquals(1 + 4 + 8 + 16, cs);

        // test forget node
        s = 1 + 4 + 8;
        cs = fn.getChildSubset(cs, false);
        assertEquals(node.subsetToString(cs), fn.subsetToString(s));
        cs2 = fn.getChildSubset(cs, true);
        assertTrue(cs < cs2);
    }

    @Test
    void testComplement() {
        NiceTreeNode node = new LeafNode((new int[]{0,1,2,3,4}));

        int s = 2 + 8;
        int c = node.complement(s);

        assertTrue(c > 0);
        assertEquals(0, c & s);
        assertEquals(s, node.complement(c));
    }
}
