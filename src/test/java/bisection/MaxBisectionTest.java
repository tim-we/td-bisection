package bisection;

import org.junit.jupiter.api.Test;

import static bisection.NiceTreeDecomposition.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaxBisectionTest {
    @Test
    void testLeafNodeWeights() {
        Graph g = new Graph(3);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        NiceTreeNode node = new LeafNode(new int[]{1, 2, 3});
        double[] weights = MaxBisection.computeWeightsLeafOrJoin(node, g);
        assertEquals(0.0, weights[0]);
        assertEquals(0.0, weights[node.numSubsets()-1]);
        int n = 0;
        for(int s = 0; s<node.numSubsets(); s++) {
            assertTrue(weights[s] >= 0.0, "weight was negative");
            assertTrue(weights[s] <= 2.0, "weight was " + weights[s]);
            if(weights[s] == 2.0) {
                n++;
            }
        }
        assertTrue(n > 0);
    }

    @Test
    void testIntroduceNodeWeights() {
        Graph g = new Graph(3);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        NiceTreeNode leaf = new LeafNode(new int[]{1, 2});
        IntroduceNode intro = new IntroduceNode(leaf, 3);
        double[] wds = MaxBisection.computeWeightsIntroduce(intro, g);
        assertEquals(0.0, wds[0]);
        for(int s=0; s<intro.child.numSubsets(); s++) {
            assertTrue(wds[s] >= 0.0, "weight diff should not be negative");
            assertTrue(wds[s] <= 2.0, "weight diff was " + wds[s]);
        }
    }

    @Test
    void testResult() {
        Graph g = TDExample.EXAMPLE_GRAPH;
        NiceTreeNode tree = TDExample.EXAMPLE_TREE;
        NiceTreeDecomposition td1 = new NiceTreeDecomposition(g, tree);
        NiceTreeDecomposition td2 = new NiceTreeDecomposition(g);
        double res1 = MaxBisection.compute(td1);
        double res2 = MaxBisection.compute(td2);
        assertEquals(res1, res2);
    }
}
