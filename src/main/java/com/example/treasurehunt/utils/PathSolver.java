package com.example.treasurehunt.utils;

import com.example.treasurehunt.models.*;
import java.util.*;
import static com.example.treasurehunt.models.GameModel.MAP_SIZE;
import static com.example.treasurehunt.models.GameModel.DIRECTIONS;

public class PathSolver {

    /**
     * An inner static class for representing a node in the pathfinding algorithms.
     * It contains the x and y coordinates, the parent node and the depth of the node as variables.
     */
    private static class PathNode {
        int x, y;
        PathNode parent;
        int depth;

        PathNode(){}

        PathNode(int x, int y, PathNode parent, int depth) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.depth = depth;
        }
    }
    /**
     * An inner static class for representing a node customized for the A* pathfinding algorithm.
     * It extends the PathNode class and adds gScore and fScore variables.
     */
    private static class PathNodeAStar extends PathNode {
        int gScore; // Cost in the number of move from the start to this node
        int fScore; // The sum of the gScore and the heuristic value (the Mahattan distance to the target)

        PathNodeAStar(int x, int y, PathNodeAStar parent, int gScore) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.gScore = gScore;
        }
    }


    /**
     * Checks if there is at least one path from the starting position to some
     * other part of the map.
     *
     * @return true if a path exists, false if the starting position is isolated
     */
    public static boolean isAllCellsUnclosed(Cell[][] map, int startX, int startY) {
        boolean[][] visited = new boolean[MAP_SIZE][MAP_SIZE]; // a 2D array to keep track of visited cells
        Queue<int[]> queue = new LinkedList<>(); // A First-In-First-Out queue using a linked list to store the cells visited but not yet explored.
        queue.add(new int[]{startX, startY}); // Add the given coordinates of the starting cell to the queue.
        visited[startY][startX] = true; // Set the first cell as visited before exploring its neighbours.

        // Continue searching and marking all the empty cells as visited that
        // are not enclosed by obstacles until the queue is empty.
        while (!queue.isEmpty()) {
            // Get the coordinates of the cell at the front of the queue and remove it.
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];

            // For the 4 possible neighbours of the current cell,
            // add them to the queue if they are valid, unvisited and empty
            for (int[] dir : DIRECTIONS) {
                int newX = x + dir[0];  // Calculate the coordinates of a neighbour cell
                int newY = y + dir[1];

                // Check the conditions and add the neighbour if satisfied.
                if (newX >= 0 && newX < MAP_SIZE && newY >= 0 && newY < MAP_SIZE &&
                        !visited[newY][newX] && map[newY][newX].getType() != Cell.CellType.OBSTACLE) {
                    visited[newY][newX] = true;
                    queue.add(new int[]{newX, newY});
                }
            }
        }

        // Iterate over the 2D array and if there is an empty that has not been visited.
        // If there is an empty cell that has not been visited, there is at least a cell enclosed by
        // the obstacles, hence return false.
        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                if ((x != 0 || y != 0) && map[y][x].getType() != Cell.CellType.OBSTACLE && !visited[y][x]) {
                    return false;
                }
            }
        }
        // If all empty cells on the map have been visited, no empty cells are enclosed by obstacles.
        return true;
    }


    /**
     * BFS implementation using a FIFO queue (linkedList) to find the shortest path
     * Finds the shortest path from the starting position to the nearest treasure (if any) using BFS (Breadth).
     *
     * @param map    The game map
     * @param startX The starting x-coordinate
     * @param startY The starting y-coordinate
     * @param targets The list of target coordinates (treasures)
     * @return A list of integer arrays containing x and y coordinates, representing the path to the nearest treasure
     *         or an empty list if no path is found
     */
    public static List<int[]> findShortestPathBFS(Cell[][] map, int startX, int startY,
                                                  List<int[]> targets) {
        // If the targets are empty, return an empty list.
        if (targets.isEmpty()) {
            return new ArrayList<>(); // No targets to find
        }

        boolean[][] visited = new boolean[MAP_SIZE][MAP_SIZE];
        Queue<PathNode> queue = new LinkedList<>(); // Create a FIFO queue using a linked list to store the nodes visited but not yet explored.

        // Initialize the starting node and add it to the queue
        queue.add(new PathNode(startX, startY, null, 0));
        visited[startY][startX] = true;

        // The variable that stores the closest treasure node found
        PathNode treasureNode = null;
        // Continue searching, visiting and exploring all the empty nodes
        // until a treasure node is found.
        while (!queue.isEmpty()) {
            // Get the first node at the front of the queue and remove it.
            PathNode current = queue.poll();

            // Check if the current position has a treasure
            for (int[] treasure : targets) {
                if (current.x == treasure[0] && current.y == treasure[1]) {
                    treasureNode = current;
                    break;
                }
            }

            // Break the while loop to end the searching process if a treasure is found
            if (treasureNode != null) break;

            // For the 4 possible neighbours of the current node,
            // add them to the queue if they are valid, unvisited and don't have an obstacle.
            for (int[] dir : DIRECTIONS) {
                int newX = current.x + dir[0]; // In each direction calculate the coordinates of a neighbour node.
                int newY = current.y + dir[1];

                // Check boundaries
                if (newX < 0 || newX >= MAP_SIZE || newY < 0 || newY >= MAP_SIZE) continue;

                // Check if a node is already visited or has an obstacle
                if (visited[newY][newX]) continue;
                if (map[newY][newX].getType() == Cell.CellType.OBSTACLE) continue;

                // Mark the node as visited
                visited[newY][newX] = true;

                // Add the new node to the queue and assign the current node as its parent.
                queue.add(new PathNode(newX, newY, current, current.depth + 1));
            }
        }

        // Return an empty path if no treasures are found
        if (treasureNode == null) return new ArrayList<>();

        return reconstructLinkedListPath(treasureNode); // Return the final path
    }


    /**
     * A* implementation using a priority queue to find the shortest path
     * Finds the shortest path from the starting position to the nearest treasure (if any) using A*.
     *
     * @param map    The game map
     * @param startX The starting x-coordinate
     * @param startY The starting y-coordinate
     * @param targets The list of target coordinates (treasures)
     * @return A list of integer arrays containing x and y coordinates, representing the path to the nearest treasure
     *         or an empty list if no path is found
     */
    public static List<int[]> findShortestPathAStar(Cell[][] map, int startX, int startY,
                                                    List<int[]> targets) {
        // If the targets are empty, return an empty list.
        if (targets.isEmpty()) {
            return new ArrayList<>(); // No targets to find
        }

        // Convert targets to a HashSet for O(1) lookup compared to a 2D array
        Set<String> targetSet = new HashSet<>();
        for (int[] target : targets) {
            targetSet.add(target[0] + "," + target[1]);
        }

        // Create a priority queue ordered by fScore (gScore + heuristics)
        // Nodes with lower fScores are processed first
        PriorityQueue<PathNodeAStar> priorityQueue = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.fScore)
        );

        // Create a hasp map for tracking visited nodes and their g-scores
        Map<String, Integer> gScore = new HashMap<>();

        // Initialize the starting node
        PathNodeAStar startNode = new PathNodeAStar(startX, startY, null, 0);
        startNode.fScore = calculateHeuristic(startX, startY, targets);

        priorityQueue.add(startNode);
        gScore.put(startX + "," + startY, Integer.valueOf(0));


        while (!priorityQueue.isEmpty()) {
            // Get the node with the lowest fScore from the priority queue
            PathNodeAStar current = priorityQueue.poll();
            String currentKey = current.x + "," + current.y;

            // Check if current node is a target
            if (targetSet.contains(currentKey)) {
                // Build the path from the treasure node to the starting node (exclusive) if a treasure is found
                return reconstructLinkedListPath(current);
            }

            // For the 4 possible neighbours of the current node,
            // add them to the queue if they are valid, unvisited and don't have an obstacle.
            for (int[] dir : DIRECTIONS) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // Check boundaries and obstacles
                if (newX < 0 || newX >= MAP_SIZE || newY < 0 || newY >= MAP_SIZE ||
                        map[newY][newX].getType() == Cell.CellType.OBSTACLE) {
                    continue;
                }

                // Calculate a key in the hash map and the gScore
                String neighborKey = newX + "," + newY;
                int tentativeGScore = gScore.get(currentKey) + 1; // Cost is 1 per step

                // If this path is better than any previous one
                if (!gScore.containsKey(neighborKey) || tentativeGScore < gScore.get(neighborKey)) {
                    // Update path and scores
                    PathNodeAStar neighborNode = new PathNodeAStar(newX, newY, current, tentativeGScore);
                    neighborNode.fScore = tentativeGScore + calculateHeuristic(newX, newY, targets);
                    // Add this neighbour node to the visited map
                    gScore.put(neighborKey, Integer.valueOf(tentativeGScore));

                    // Add to priorityQueue if not already there or update it
                    priorityQueue.removeIf(node -> node.x == newX && node.y == newY);
                    priorityQueue.add(neighborNode);
                }
            }
        }

        return new ArrayList<>(); // No path found
    }

    /**
     * Calculates the heuristic value (Manhattan distance) for A* pathfinding.
     * This is the estimated cost from the current node to the nearest target.
     *
     * @param x      The x-coordinate of the current node
     * @param y      The y-coordinate of the current node
     * @param targets The list of target coordinates (treasures)
     * @return The heuristic value (Manhattan distance) to the nearest target
     */
    private static int calculateHeuristic(int x, int y, List<int[]> targets) {
        int minDistance = Integer.MAX_VALUE;

        // Calculate Manhattan distance to the closest target
        for (int[] target : targets) {
            int distance = Math.abs(x - target[0]) + Math.abs(y - target[1]);
            minDistance = Math.min(minDistance, distance);
        }

        return minDistance;
    }

    /**
     * Reconstructs the path from the end node to the start node (exclusive) using linkList.
     * This method builds the path by traversing back through the parent nodes.
     *
     * @param endNode The end node of the path
     * @return A list of integer arrays containing x and y coordinates, representing the path
     */
    private static List<int[]> reconstructLinkedListPath(PathNode endNode) {
        LinkedList<int[]> path = new LinkedList<>(); // Use LinkedList to represent the path
        PathNode current = endNode;

        // Build the path from the treasure node to the starting node (exclusive)
        // O(1) time complexity for each insertion, O(n) for the whole loop
        while (current.parent != null) {
            path.addFirst(new int[]{current.x, current.y});
            current = current.parent;
        }

        return path;
    }

    /**
     * Reconstructs the path from the end node to the start node (exclusive) using arrayList.
     * This method builds the path by traversing back through the parent nodes.
     *
     * @param endNode The end node of the path
     * @return A list of integer arrays containing x and y coordinates, representing the path
     */
    private static List<int[]> reconstructArrayListPath(PathNode endNode){
        // Alternative way to build the path using ArrayList
        List<int[]> path = new ArrayList<>();
        PathNode current = endNode;

        // Loop until parent node is null
        // O(n) time complexity for each insertion and O(n^2) for the whole loop
        while (current.parent != null) {
            path.addFirst(new int[]{current.x, current.y}); // insert the current node at the front of the arrayList
            current = current.parent;
        }

        return path;
    }

}
