import java.util.*;

public class CourseScheduler {

    // Simple class to represent a course and its prerequisites
    static class Course {
        String code;
        List<String> prereqs = new ArrayList<>();

        Course(String code, String... prereqs) {
            this.code = code;
            this.prereqs.addAll(Arrays.asList(prereqs));
        }
    }

    // Store all our courses and build an adjacency list for the graph
    Map<String, Course> courseMap = new HashMap<>();
    Map<String, List<String>> adj = new HashMap<>();  // shows what courses unlock after each one

    // Add a course to our system along with its prerequisites
    void addCourse(String code, String... prereqs) {
        Course c = new Course(code, prereqs);
        courseMap.put(code, c);

        // Build the adjacency list - for each prerequisite, track what it unlocks
        adj.putIfAbsent(code, new ArrayList<>());
        for (String p : prereqs) {
            adj.putIfAbsent(p, new ArrayList<>());
            adj.get(p).add(code);  // p unlocks this course
        }
    }

    // -------------------------
    // PRINT FULL CURRICULUM
    // -------------------------
    void printFullCurriculum() {
        System.out.println("====================================");
        System.out.println("FULL CURRICULUM (COURSES + PREREQS)");
        System.out.println("====================================");

        // Show every course and what you need before taking it
        for (Course c : courseMap.values()) {
            System.out.println(c.code + ": " + c.prereqs);
        }

        // Calculate some stats
        int V = courseMap.size();
        int E = adj.values().stream().mapToInt(List::size).sum();

        System.out.println("\nTotal Vertices (Courses): " + V);
        System.out.println("Total Edges (Prereqs):   " + E);
    }

    // -------------------------
    // CYCLE DETECTION
    // -------------------------
    // Check if there are any impossible prerequisite loops
    List<List<String>> detectCycles() {
        Map<String, Boolean> visited = new HashMap<>();      // courses we've fully explored
        Map<String, Boolean> stack = new HashMap<>();        // courses in our current path
        List<List<String>> cycles = new ArrayList<>();       // all cycles we find
        List<String> path = new ArrayList<>();               // our current exploration path

        // Check every course in the system
        for (String c : courseMap.keySet()) {
            if (!visited.getOrDefault(c, false)) {
                dfsCycle(c, visited, stack, path, cycles);
            }
        }
        return cycles;
    }

    // DFS to find cycles - if we visit a course that's already in our current path, we found a loop!
    void dfsCycle(String node, Map<String, Boolean> visited, Map<String, Boolean> stack,
                List<String> path, List<List<String>> cycles) {

        visited.put(node, true);
        stack.put(node, true);    // mark this course as "currently exploring"
        path.add(node);

        // Look at all courses this one unlocks
        for (String next : adj.getOrDefault(node, List.of())) {
            if (!visited.getOrDefault(next, false)) {
                // Haven't seen this course yet, keep exploring
                dfsCycle(next, visited, stack, path, cycles);
            } else if (stack.getOrDefault(next, false)) {
                // We're visiting a course that's already in our path - cycle detected!
                int start = path.indexOf(next);
                List<String> cycle = new ArrayList<>(path.subList(start, path.size()));
                cycle.add(next);  // complete the loop
                cycles.add(cycle);
            }
        }

        stack.put(node, false);  // done exploring this path
        path.remove(path.size() - 1);
    }

    // -------------------------
    // TOPOLOGICAL SORT
    // -------------------------
    // Figure out a valid order to take all the courses
    List<String> topoSort() {
        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>();

        // Start DFS from every unvisited course
        for (String c : courseMap.keySet()) {
            if (!visited.contains(c)) {
                dfsTopo(c, visited, stack);
            }
        }

        // Pop everything off the stack to get our final order
        List<String> result = new ArrayList<>();
        while (!stack.isEmpty()) result.add(stack.pop());
        return result;
    }

    // DFS for topological sort - courses get added to stack AFTER all their dependencies
    void dfsTopo(String node, Set<String> visited, Stack<String> stack) {
        visited.add(node);
        
        // Visit all courses that come after this one
        for (String next : adj.getOrDefault(node, List.of())) {
            if (!visited.contains(next)) {
                dfsTopo(next, visited, stack);
            }
        }
        
        // Only add to stack after exploring everything that depends on this course
        stack.push(node);
    }

    // -------------------------
    // SEMESTER ASSIGNMENT
    // -------------------------
    // Group courses into semesters based on prerequisites
    Map<String, Integer> assignSemesters(List<String> order) {
        Map<String, Integer> incoming = new HashMap<>();  // count of prerequisites for each course
        Map<String, Integer> semester = new HashMap<>();  // which semester each course belongs to

        // Initialize - count how many prerequisites each course has
        for (String c : order) incoming.put(c, 0);
        for (Course c : courseMap.values()) {
            for (String p : c.prereqs)
                if (incoming.containsKey(c.code))
                    incoming.put(c.code, incoming.get(c.code) + 1);
        }

        // Start with courses that have no prerequisites - they go in semester 1
        Queue<String> q = new LinkedList<>();
        for (String c : order) {
            if (incoming.get(c) == 0) {
                semester.put(c, 1);
                q.add(c);
            }
        }

        // Process courses level by level (semester by semester)
        while (!q.isEmpty()) {
            String cur = q.poll();
            int sem = semester.get(cur);

            // For each course this one unlocks, reduce its prerequisite count
            for (String next : adj.getOrDefault(cur, List.of())) {
                if (!incoming.containsKey(next)) continue;

                incoming.put(next, incoming.get(next) - 1);
                
                // If all prerequisites are met, assign it to the next semester
                if (incoming.get(next) == 0) {
                    semester.put(next, sem + 1);
                    q.add(next);
                }
            }
        }
        return semester;
    }

    // -------------------------
    // PRINT SCHEDULE
    // -------------------------
    // Main function to generate and display the course schedule
    static void printSchedule(String title, CourseScheduler s) {
        System.out.println("\n==============================");
        System.out.println(title);
        System.out.println("==============================");

        // Show graph statistics
        int V = s.courseMap.size();
        int E = s.adj.values().stream().mapToInt(List::size).sum();

        System.out.println("Vertices (courses): " + V);
        System.out.println("Edges (prereqs):   " + E);
        System.out.println("Time Complexity:   O(V + E)");

        // Check for cycles first - can't schedule if there's a loop!
        List<List<String>> cycles = s.detectCycles();
        if (!cycles.isEmpty()) {
            System.out.println("\n⚠ CYCLE DETECTED:");
            for (List<String> c : cycles)
                System.out.println("  " + String.join(" → ", c));
            System.out.println("\nCannot generate a valid schedule until cycle is fixed.");
            return;  // bail out early if there's a problem
        }

        // No cycles? Great! Generate the course order
        List<String> order = s.topoSort();
        System.out.println("\nValid Course Order:");
        for (String c : order) System.out.println("  " + c);

        // Now organize courses into semesters
        Map<String, Integer> sem = s.assignSemesters(order);
        System.out.println("\nSemester Breakdown:");
        
        // Group courses by semester number for nice display
        TreeMap<Integer, List<String>> map = new TreeMap<>();
        for (var e : sem.entrySet()) {
            map.putIfAbsent(e.getValue(), new ArrayList<>());
            map.get(e.getValue()).add(e.getKey());
        }

        // Print out each semester's courses
        for (var e : map.entrySet()) {
            System.out.println("Semester " + e.getKey() + ": " + e.getValue());
        }
    }

    // -------------------------
    // MAIN
    // -------------------------
    public static void main(String[] args) {

        CourseScheduler s = new CourseScheduler();

        // Build a typical computer science curriculum
        // Each course lists what you need to take first
        s.addCourse("CS101");                    // Intro course - no prereqs
        s.addCourse("CS102", "CS101");           // Need CS101 first
        s.addCourse("CS103", "CS102");           // Need CS102 first
        s.addCourse("CS201", "CS103");           // Need CS103 first
        s.addCourse("CS202", "CS201");           // Need CS201 first
        s.addCourse("CS301", "CS202");           // Need CS202 first
        s.addCourse("CS302", "CS301");           // Need CS301 first

        // -------------------------
        // Show what we're working with
        // -------------------------
        s.printFullCurriculum();

        // -------------------------
        // SCENARIO 1 — Normal schedule (should work fine)
        // -------------------------
        printSchedule("SCENARIO 1: VALID COURSE SCHEDULE", s);

        // -------------------------
        // SCENARIO 2 — Let's break it by adding a cycle
        // -------------------------
        System.out.println("\n\nAdding cycle: CS201 → CS202 → CS301 → CS302 → CS201");
        System.out.println("(CS302 now requires CS201, but CS201 requires CS302 through the chain!)");

        // Add cycle: CS302 now points back to CS201 - creates an impossible loop!
        s.addCourse("CS302", "CS301", "CS201");

        printSchedule("SCENARIO 2: AFTER ADDING CYCLE", s);

        // -------------------------
        // FIX: Remove the problematic prerequisite
        // -------------------------
        System.out.println("\nFixing cycle by removing extra CS201 prerequisite from CS302...");
        s.addCourse("CS302", "CS301"); // overwrite with just the valid prerequisite

        // Show that everything works again
        printSchedule("SCENARIO 2 (FIXED): VALID AGAIN", s);
    }
}

