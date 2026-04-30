import java.util.*;

public class TopoSort {

    // Custom exception we throw when we find a cycle in the graph
    static class CycleDetectedException extends Exception {
        List<String> cycle;
        public CycleDetectedException(List<String> cycle) { this.cycle = cycle; }
    }

    // Main function that kicks off the topological sort
    // Uses depth-first search (DFS) to explore the graph
    public static List<String> topologicalSortDFS(Map<String, List<String>> graph) throws CycleDetectedException {
        Set<String> visited = new HashSet<>();      // nodes we've completely processed
        Set<String> recStack = new HashSet<>();     // nodes in our current path (helps detect cycles)
        List<String> result = new ArrayList<>();    // final sorted order (built backwards)
        List<String> path = new ArrayList<>();      // keeps track of our current exploration path

        // Go through every node in the graph (sorted alphabetically for consistency)
        for (String node : new TreeSet<>(graph.keySet())) {
            if (!visited.contains(node)) {
                // Haven't seen this node yet, so explore it
                dfs(node, graph, visited, recStack, result, path);
            }
        }

        // We built the list backwards, so flip it around
        Collections.reverse(result);
        return result;
    }

    // The actual DFS exploration - this is where the magic happens
    private static void dfs(String node, Map<String, List<String>> graph,
                            Set<String> visited, Set<String> recStack,
                            List<String> result, List<String> path) throws CycleDetectedException {
        
        // If we're visiting a node that's already in our current path, we found a cycle!
        if (recStack.contains(node)) {
            // Figure out where the cycle starts and extract it
            int start = path.indexOf(node);
            List<String> cycle = new ArrayList<>(path.subList(start, path.size()));
            cycle.add(node); // add the node again to show it loops back
            throw new CycleDetectedException(cycle);
        }

        // Already fully processed this node? Skip it
        if (visited.contains(node)) return;

        // Mark this node as visited and add it to our current path
        visited.add(node);
        recStack.add(node);
        path.add(node);

        // Visit all the neighbors (outgoing edges from this node)
        for (String neighbor : graph.getOrDefault(node, Collections.emptyList())) {
            dfs(neighbor, graph, visited, recStack, result, path);
        }

        // Done exploring this node and all its children
        // Remove it from the recursion stack and path
        recStack.remove(node);
        path.remove(path.size() - 1);
        
        // Add to result - nodes get added AFTER all their dependencies
        result.add(node);
    }

    // Helper function to print out the graph in a readable way
    public static void printGraph(Map<String, List<String>> graph) {
        System.out.println("\nGraph Structure:");
        System.out.println("----------------------------------------");
        for (String node : new TreeSet<>(graph.keySet())) {
            List<String> neighbors = graph.get(node);
            if (neighbors.isEmpty())
                System.out.println(node + " -> (no outgoing edges)");
            else
                System.out.println(node + " -> " + String.join(", ", neighbors));
        }
        System.out.println("----------------------------------------");
    }

    public static void main(String[] args) {

        // Build our test graph - each node points to its dependencies
        // For example, M depends on Q, R, and X
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("M", Arrays.asList("Q", "R", "X"));
        graph.put("N", Arrays.asList("Q", "U", "O"));
        graph.put("O", Arrays.asList("R", "V", "S"));
        graph.put("P", Arrays.asList("O", "S", "Z"));
        graph.put("Q", Arrays.asList("T"));
        graph.put("R", Arrays.asList("U", "Y"));
        graph.put("S", Arrays.asList("R"));
        graph.put("T", Collections.emptyList());  // T has no dependencies
        graph.put("U", Arrays.asList("T"));
        graph.put("V", Arrays.asList("X", "W"));
        graph.put("W", Arrays.asList("Z"));
        graph.put("X", Collections.emptyList());  // X has no dependencies
        graph.put("Y", Arrays.asList("V"));
        graph.put("Z", Collections.emptyList());  // Z has no dependencies

        System.out.println("============================================================");
        System.out.println("              TOPOLOGICAL SORTING ALGORITHM - DFS BASED");
        System.out.println("============================================================");

        printGraph(graph);

        // Calculate some stats about our graph
        int V = graph.size();
        int E = graph.values().stream().mapToInt(List::size).sum();

        System.out.println("\nGraph Statistics:");
        System.out.println("  Vertices (V): " + V);
        System.out.println("  Edges (E): " + E);
        System.out.println("  Time Complexity: O(V + E) = O(" + (V + E) + ")");

        // TEST 1: Try sorting the original graph (should work fine - it's acyclic)
        System.out.println("\n============================================================");
        System.out.println("TEST 1: ACYCLIC GRAPH (Original DAG)");
        System.out.println("============================================================");

        try {
            List<String> result = topologicalSortDFS(graph);
            System.out.println("\n✓ Topological sort successful!");
            System.out.println("Topological Order:");
            System.out.println(String.join(" -> ", result));
        } catch (CycleDetectedException e) {
            System.out.println("\n✗ Topological sort failed: CYCLE DETECTED!");
        }

        // TEST 2: Mess it up by adding a cycle
        System.out.println("\n============================================================");
        System.out.println("TEST 2: CYCLIC GRAPH (Adding edge Z -> Y)");
        System.out.println("============================================================");

        // Make a copy of the graph so we don't mess up the original
        Map<String, List<String>> cycleGraph = new HashMap<>();
        for (String key : graph.keySet())
            cycleGraph.put(key, new ArrayList<>(graph.get(key)));

        // Add an edge that creates a cycle
        cycleGraph.get("Z").add("Y");

        System.out.println("\nAdding edge: Z -> Y");
        System.out.println("This creates a cycle: Y -> V -> W -> Z -> Y");

        printGraph(cycleGraph);

        try {
            List<String> resultCycle = topologicalSortDFS(cycleGraph);
            System.out.println("\n✓ Topological sort successful!");
            System.out.println(String.join(" -> ", resultCycle));
        } catch (CycleDetectedException e) {
            // Should catch the cycle here
            System.out.println("\n✗ Topological sort failed: CYCLE DETECTED!");
            System.out.println("Cycle detected: " + String.join(" -> ", e.cycle));
            System.out.println("\nExplanation:");
            System.out.println("Topological sorting is only possible for DAGs.");
            System.out.println("A cycle prevents a valid linear ordering.");
        }

        System.out.println("\n============================================================");
        System.out.println("SUMMARY");
        System.out.println("============================================================");
    }
}
