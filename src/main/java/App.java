import bisection.Graph;
import bisection.MaxBisection;
import bisection.NiceTreeDecomposition;
import bisection.TreeDecomposition;
import org.apache.commons.cli.*;
import visualization.Visualizer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    private static final Options options;

    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cl;

        try {
            cl = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helpFormatter.printHelp("td-bisection OPTIONS", options);

            System.exit(1);
            return;
        }

        Path graphFile = Paths.get(cl.getOptionValue("graph"));
        Path tdFile = Paths.get(cl.getOptionValue("tree-decomposition"));

        System.out.println("Graph: " + graphFile.getFileName());
        System.out.println("Tree decomposition: " + tdFile.getFileName());

        final long time1 = System.currentTimeMillis();

        // read & parse input files
        Graph graph = Graph.fromFile(graphFile);
        TreeDecomposition td = TreeDecomposition.fromFile(tdFile);
        td.graph = graph;

        // output graph & TD info
        System.out.println("Graph vertices: " + graph.vertices);
        System.out.println("Treewidth: " + td.width);

        // create nice tree decomposition
        NiceTreeDecomposition ntd = td.normalize();
        System.out.println(" TD #bags: " + td.countNodes());
        System.out.println("NTD #bags: " + ntd.numBags);

        if (ntd.numBags > 4*graph.vertices) {
            System.out.println("(!) Warning: NTD is not optimal.");
        }

        if (cl.hasOption("show")) {
            System.out.println("Displaying graph and nice tree decomposition...");
            Visualizer.display(ntd);
        }
        System.out.println();

        // compute max bisection
        final long time2 = System.currentTimeMillis();
        double bisection = MaxBisection.compute(ntd);
        final long time3 = System.currentTimeMillis();

        System.out.println("Max Bisection size: " + bisection);
        System.out.println("Max Bisection time: " + timeToString(time3 - time2));
        System.out.println("Total time: " + timeToString(time3 - time1));
    }

    private static String timeToString(long time) {
        if(Math.abs(time) >= 1000) {
            return (time/1000.0) + "s";
        } else {
            return time + "ms";
        }
    }

    static {
        options = new Options();

        Option input1 = new Option("g", "graph", true, "input graph file (.gr)");
        Option input2 = new Option("td", "tree-decomposition", true, "input tree-decomposition file (.td)");
        Option show = new Option("s", "show", false, "display graph and decomposition");

        input1.setRequired(true);
        input2.setRequired(true);
        show.setRequired(false);

        options.addOption(input1);
        options.addOption(input2);
        options.addOption(show);
    }
}
