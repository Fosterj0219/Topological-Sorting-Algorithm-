🚀 Overview
This project uses a Directed Acyclic Graph (DAG) to represent courses and their dependencies. The algorithm determines a valid linear order in which all courses can be completed without violating any prerequisite requirements.

🛠 Tech Stack
Language: Java

Concepts: Graph Theory, Depth-First Search (DFS) / Kahn’s Algorithm, Recursion

Tools: VS Code

✨ Key Features
Dependency Mapping: Correctly identifies which tasks must be completed before others.

Cycle Detection: Includes logic to detect if a "circular dependency" exists (e.g., Course A needs Course B, but Course B needs Course A), which would make scheduling impossible.

Efficient Processing: Optimized to handle multiple nodes and complex prerequisite chains.

📂 Project Structure
TopoSort.java: The core algorithm logic for sorting the graph nodes.

CourseScheduler.java: The driver class that manages course data and outputs the final schedule.

Powerpoint slides final...: Documentation explaining the algorithmic approach and logic flow.

📝 How It Works (Code Reference)
The project utilizes a graph-based approach to ensure each node is visited only after its dependencies are clear:

Java
// Logic snippet from TopoSort.java
// This ensures we only add a course to the schedule 
// once all prerequisites have been satisfied.
if (!visited[node]) {
    dfs(node, visited, stack);
}
