/**
 *
 */
package roadgraph;

import geography.GeographicPoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import util.GraphLoader;

/**
 * @author UCSD MOOC development team
 *
 *         A class which represents a graph of geographic locations Nodes in the graph are intersections of multiple
 *         roads. Edges are the roads.
 *
 */
public class MapGraph {

	// Maintain both nodes and edges as you will need to
	// be able to look up nodes by lat/lon or by roads
	// that contain those nodes.
	private HashMap<GeographicPoint, MapNode> pointNodeMap;
	private HashSet<MapEdge> edges;

	// flag to switch between Dijkstra and A* search algorithm
	public static boolean isAStar;

	/**
	 * Create a new empty MapGraph
	 *
	 */
	public MapGraph() {
		pointNodeMap = new HashMap<GeographicPoint, MapNode>();
		edges = new HashSet<MapEdge>();
	}

	/**
	 * Get the number of vertices (road intersections) in the graph
	 * 
	 * @return The number of vertices in the graph.
	 */
	public int getNumVertices() {
		return pointNodeMap.values().size();
	}

	/**
	 * Get the number of road segments in the graph
	 * 
	 * @return The number of edges in the graph.
	 */
	public int getNumEdges() {
		return edges.size();
	}

	// For us in DEBUGGING. Print the Nodes in the graph
	public void printNodes() {
		System.out.println("****PRINTING NODES ********");
		System.out.println("There are " + getNumVertices() + " Nodes: \n");
		for (GeographicPoint pt : pointNodeMap.keySet()) {
			MapNode n = pointNodeMap.get(pt);
			System.out.println(n);
		}
	}

	// For us in DEBUGGING. Print the Edges in the graph
	public void printEdges() {
		System.out.println("******PRINTING EDGES******");
		System.out.println("There are " + getNumEdges() + " Edges:\n");
		for (MapEdge e : edges) {
			System.out.println(e);
		}

	}

	/**
	 * Add a node corresponding to an intersection
	 *
	 * @param latitude
	 *            The latitude of the location
	 * @param longitude
	 *            The longitude of the location
	 */
	public void addVertex(double latitude, double longitude) {
		GeographicPoint pt = new GeographicPoint(latitude, longitude);
		this.addVertex(pt);
	}

	/**
	 * Add a node corresponding to an intersection at a Geographic Point
	 *
	 * @param location
	 *            The location of the intersection
	 */
	public void addVertex(GeographicPoint location) {
		MapNode n = pointNodeMap.get(location);
		if (n == null) {
			n = new MapNode(location);
			pointNodeMap.put(location, n);
		} else {
			System.out.println("Warning: Node at location " + location + " already exists in the graph.");
		}

	}

	/**
	 * Add an edge representing a segment of a road. Precondition: The corresponding Nodes must have already been added
	 * to the graph.
	 * 
	 * @param roadName
	 *            The name of the road
	 * @param roadType
	 *            The type of the road
	 */
	public void addEdge(double lat1, double lon1, double lat2, double lon2, String roadName, String roadType) {
		// Find the two Nodes associated with this edge.
		GeographicPoint pt1 = new GeographicPoint(lat1, lon1);
		GeographicPoint pt2 = new GeographicPoint(lat2, lon2);

		MapNode n1 = pointNodeMap.get(pt1);
		MapNode n2 = pointNodeMap.get(pt2);

		// check nodes are valid
		if (n1 == null)
			throw new NullPointerException("addEdge: pt1:" + pt1 + "is not in graph");
		if (n2 == null)
			throw new NullPointerException("addEdge: pt2:" + pt2 + "is not in graph");

		addEdge(n1, n2, roadName, roadType, MapEdge.DEFAULT_LENGTH);

	}

	public void addEdge(GeographicPoint pt1, GeographicPoint pt2, String roadName, String roadType) {

		MapNode n1 = pointNodeMap.get(pt1);
		MapNode n2 = pointNodeMap.get(pt2);

		// check nodes are valid
		if (n1 == null)
			throw new NullPointerException("addEdge: pt1:" + pt1 + "is not in graph");
		if (n2 == null)
			throw new NullPointerException("addEdge: pt2:" + pt2 + "is not in graph");

		addEdge(n1, n2, roadName, roadType, MapEdge.DEFAULT_LENGTH);
	}

	public void addEdge(GeographicPoint pt1, GeographicPoint pt2, String roadName, String roadType, double length) {
		MapNode n1 = pointNodeMap.get(pt1);
		MapNode n2 = pointNodeMap.get(pt2);

		// check nodes are valid
		if (n1 == null)
			throw new NullPointerException("addEdge: pt1:" + pt1 + "is not in graph");
		if (n2 == null)
			throw new NullPointerException("addEdge: pt2:" + pt2 + "is not in graph");

		addEdge(n1, n2, roadName, roadType, length);
	}

	/** Given a point, return if there is a corresponding MapNode **/
	public boolean isNode(GeographicPoint point) {
		return pointNodeMap.containsKey(point);
	}

	// Add an edge when you already know the nodes involved in the edge
	private void addEdge(MapNode n1, MapNode n2, String roadName, String roadType, double length) {
		MapEdge edge = new MapEdge(roadName, roadType, n1, n2, length);
		edges.add(edge);
		n1.addEdge(edge);
	}

	/** Returns the nodes in terms of their geographic locations */
	public Collection<GeographicPoint> getVertices() {
		return pointNodeMap.keySet();
	}

	// get a set of neighbor nodes from a mapnode
	private Set<MapNode> getNeighbors(MapNode node) {
		return node.getNeighbors();
	}

	public List<GeographicPoint> bfs(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		Consumer<GeographicPoint> temp = (x) -> {
		};
		return bfs(start, goal, temp);
	}

	/**
	 * Find the path from start to goal using Breadth First Search
	 *
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> bfs(GeographicPoint start, GeographicPoint goal, Consumer<GeographicPoint> nodeSearched) {
		// Setup - check validity of inputs
		if (start == null || goal == null)
			throw new NullPointerException("Cannot find route from or to null node");
		MapNode startNode = pointNodeMap.get(start);
		MapNode endNode = pointNodeMap.get(goal);
		if (startNode == null) {
			System.err.println("Start node " + start + " does not exist");
			return null;
		}
		if (endNode == null) {
			System.err.println("End node " + goal + " does not exist");
			return null;
		}

		// setup to begin BFS
		HashMap<MapNode, MapNode> parentMap = new HashMap<MapNode, MapNode>();
		Queue<MapNode> toExplore = new LinkedList<MapNode>();
		HashSet<MapNode> visited = new HashSet<MapNode>();
		toExplore.add(startNode);
		MapNode next = null;

		while (!toExplore.isEmpty()) {
			next = toExplore.remove();

			// hook for visualization
			nodeSearched.accept(next.getLocation());

			if (next.equals(endNode))
				break;
			Set<MapNode> neighbors = getNeighbors(next);
			for (MapNode neighbor : neighbors) {
				if (!visited.contains(neighbor)) {
					visited.add(neighbor);
					parentMap.put(neighbor, next);
					toExplore.add(neighbor);
				}
			}
		}
		if (!next.equals(endNode)) {
			System.out.println("No path found from " + start + " to " + goal);
			return null;
		}

		// Reconstruct the parent path
		List<GeographicPoint> path = reconstructPath(parentMap, startNode, endNode);

		return path;
	}

	/**
	 * Reconstruct a path from start to goal using the parentMap
	 *
	 * @param parentMap
	 *            the HashNode map of children and their parents
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */

	private List<GeographicPoint> reconstructPath(HashMap<MapNode, MapNode> parentMap, MapNode start, MapNode goal) {
		LinkedList<GeographicPoint> path = new LinkedList<GeographicPoint>();
		MapNode current = goal;

		while (!current.equals(start)) {
			path.addFirst(current.getLocation());
			current = parentMap.get(current);
		}

		// add start
		path.addFirst(start.getLocation());
		return path;
	}
	
	/**
	 * Reconstruct a path from start and back using the parentMap
	 *
	 * @param parentMap
	 *            the HashNode map of children and their parents
	 * @param start
	 *            The starting location
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */

	private List<GeographicPoint> reconstructTSPPath(HashMap<MapNode, MapNode> parentMap, MapNode start) {
		LinkedList<GeographicPoint> path = new LinkedList<GeographicPoint>();
		for (MapNode node : parentMap.keySet()) {
			path.addFirst(node.getLocation());
		}
		path.addFirst(start.getLocation());
		return path;
	}

	/**
	 * Find the path from start to goal using Dijkstra's algorithm
	 * 
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> dijkstra(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		// You do not need to change this method.
		Consumer<GeographicPoint> temp = (x) -> {
		};

		// Set flag to represent a Dijskstra search
		isAStar = false;
		return discoverPath(start, goal, temp);
	}

	/**
	 * Find the path from start to goal using Dijkstra's algorithm
	 * 
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @param nodeSearched
	 *            A hook for visualization. See assignment instructions for how to use it.
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> discoverPath(GeographicPoint start, GeographicPoint goal, Consumer<GeographicPoint> nodeSearched) {
		// Setup - check validity of inputs
		if (start == null || goal == null)
			throw new NullPointerException("Cannot find route from or to null node");

		// Get nodes from geographic points
		MapNode startNode = pointNodeMap.get(start);
		MapNode endNode = pointNodeMap.get(goal);

		// Validate
		if (startNode == null) {
			System.err.println("Start node " + start + " does not exist");
			return null;
		}
		if (endNode == null) {
			System.err.println("End node " + goal + " does not exist");
			return null;
		}

		// Initialize path
		HashMap<MapNode, MapNode> parentMap = new HashMap<MapNode, MapNode>();

		// Call the actual search algorithm which will trigger i Dijkstra or A* search
		boolean found = genericSearch(startNode, endNode, parentMap, nodeSearched);

		// Handle result
		if (!found) {
			System.out.println("No path found from " + start + " to " + goal);
			return null;
		}

		// Reconstruct the parent path
		return reconstructPath(parentMap, startNode, endNode);
	}

	/**
	 * The search algorithm that is used both for a Disjkstra and a A* search. Depending on wether the static flag
	 * isAStart iset to true or false, this method will run the appropriate logic
	 * 
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @param parentMap
	 *            The HashNode map of children and their parent
	 * @param nodeSearched
	 *            A hook for visualization. See assignment instructions for how to use it.
	 * @return boolean result representing the existence (or not) of a valid path.
	 */
	private boolean genericSearch(MapNode startNode, MapNode endNode, HashMap<MapNode, MapNode> parentMap, Consumer<GeographicPoint> nodeSearched) {
		// Initialize: Priority queue (PQ), visited HashSet, parent HashMap, and
		PriorityQueue<MapNode> toExplore = new PriorityQueue<MapNode>();
		HashSet<MapNode> visited = new HashSet<MapNode>();
		MapNode curr = null;

		// Keep track of how many nodes have been visited
		int visits = 0;

		// Set distances to infinity.
		for (GeographicPoint pt : getVertices()) {
			pointNodeMap.get(pt).setDistance(Double.POSITIVE_INFINITY);
			pointNodeMap.get(pt).setActualDistance(Double.POSITIVE_INFINITY);
		}

		// Enqueue {S,0} onto the PQ
		startNode.setDistance(0.0);
		startNode.setActualDistance(0.0);
		toExplore.add(startNode);

		// Search implementation
		while (!toExplore.isEmpty()) {
			curr = toExplore.remove();
			// hook for visualization
			nodeSearched.accept(curr.getLocation());
			// if (curr is not visited)
			if (!visited.contains(curr)) {
				visited.add(curr);
				visits++;
				if (curr.equals(endNode))
					break;
				Set<MapNode> neighbors = getNeighbors(curr);
				for (MapNode neighbor : neighbors) {
					if (!visited.contains(neighbor)) {
						// Find distance between neigbor and current node
						Double distanceFromCurrent = findDistanceBetweenNeighbors(curr.getLocation(), neighbor.getLocation());

						// If an A* search is called, find straight line from node to target, else h(n) = 0
						Double distanceFromTarget = isAStar ? findStraightLineBetweenNodes(neighbor.getLocation(), endNode.getLocation()) : 0.0;

						// Check if is shortest path
						if (distanceFromCurrent + curr.getDistance() < neighbor.getDistance()) {
							neighbor.setActualDistance(distanceFromCurrent + curr.getActualDistance());
							neighbor.setDistance(neighbor.getActualDistance() + distanceFromTarget);
							parentMap.put(neighbor, curr);
							toExplore.add(neighbor);
						}
					}
				}

			}
		}

		 System.out.println("Visited: " + visits + " nodes.");

		return curr.equals(endNode);
	}

	/**
	 * Find the path from start to goal using A-Star search
	 * 
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> aStarSearch(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		Consumer<GeographicPoint> temp = (x) -> {
		};

		// Set flag to represent an A* search
		isAStar = true;
		return discoverPath(start, goal, temp);
	}

	/**
	 * Find the path from start and back after visiting all nodes to the graph. Implementation of greedy algorithm in
	 * order to solve the traveling salesman problem
	 * 
	 * @param start
	 *            The starting location
	 * @return The list of intersections that form a decent path (could be optimal) from start and back (including all
	 *         nodes of the graph).
	 */
	public List<GeographicPoint> greedy(GeographicPoint start) {
		// Setup - check validity of inputs
		if (start == null)
			throw new NullPointerException("Cannot find route from or to null node");

		// Get nodes from geographic points
		MapNode startNode = pointNodeMap.get(start);

		// Validate
		if (startNode == null) {
			System.err.println("Start node " + start + " does not exist");
			return null;
		}

		// Initialize path
		HashMap<MapNode, MapNode> parentMap = new HashMap<MapNode, MapNode>();

		// Call the actual greedy algorithm
		boolean found = greedySearch(startNode, parentMap);

		// Handle result
		if (!found) {
			System.out.println("No path found from " + start);
			return null;
		}

		// Reconstruct the parent path
		return reconstructTSPPath(parentMap, startNode);

	}

	/**
	 * The search algorithm that implements greedy algorithm logic to solve the TSP problem
	 * 
	 * @param start
	 *            The starting location
	 * @param parentMap
	 *            The HashNode map of children and their parent
	 * @return boolean result representing the existence (or not) of a valid path.
	 */
	private boolean greedySearch(roadgraph.MapNode startNode, HashMap<roadgraph.MapNode, roadgraph.MapNode> parentMap) {

		// Initialize: Priority queue (PQ), visited HashSet, parent HashMap, and
		PriorityQueue<MapNode> toExplore = new PriorityQueue<MapNode>();
		HashSet<MapNode> visited = new HashSet<MapNode>();
		MapNode curr = null;

		// Keep track of how many nodes have been visited
		int visits = 1;

		// Add closest neighbor to queue
		startNode.setDistance(Double.POSITIVE_INFINITY);
		toExplore.add(startNode);
		visited.add(startNode);
		startNode.getClosestNeighbor().setDistance(0.0);
		toExplore.add(startNode.getClosestNeighbor());
		parentMap.put(startNode.getClosestNeighbor(), startNode);

		// Search implementation
		while (!toExplore.isEmpty()) {
			curr = toExplore.remove();
			// if (curr is not visited)
			if (!visited.contains(curr)) {
				visited.add(curr);
				visits++;
				// Find which of the unvisited neighbors is the closest and just add it to the path
				Set<MapNode> unvisitedneighbors = findUnvisited(getNeighbors(curr), visited);
				MapNode closestUnvisited = curr.getClosestNeighbor(unvisitedneighbors, startNode);
				closestUnvisited.setDistance(0.0);
				parentMap.put(closestUnvisited, curr);
				toExplore.add(closestUnvisited);
			}

		}

		System.out.println("Visited: " + visits + " nodes.");

		return curr.equals(startNode);
	}

	private Set<MapNode> findUnvisited(Set<MapNode> neighbors, HashSet<MapNode> visited) {
		Set<MapNode> unvisited = new HashSet<MapNode>();
		for (MapNode mapNode : neighbors) {
			if (!visited.contains(mapNode)) {
				unvisited.add(mapNode);
			}
		}
		return unvisited;
	}

	/**
	 * Find the path from start to goal using A-Star search
	 * 
	 * @param start
	 *            The starting location
	 * @param goal
	 *            The goal location
	 * @param nodeSearched
	 *            A hook for visualization. See assignment instructions for how to use it.
	 * @return The list of intersections that form the shortest path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> aStarSearch(GeographicPoint start, GeographicPoint goal, Consumer<GeographicPoint> nodeSearched) {
		return null;
	}

	/**
	 * Find the distance between 2 neighboring nodes using the edge that connects them
	 * 
	 * @param start
	 *            The starting location
	 * @param end
	 *            The ending location
	 * @return The double number representing the distance between the 2 selected nodes
	 */
	public Double findDistanceBetweenNeighbors(GeographicPoint start, GeographicPoint end) {
		for (MapEdge e : this.edges) {
			if (e.getEndNode().getLocation().equals(end) && e.getOtherNode(pointNodeMap.get(end)).getLocation().equals(start)) {
				return e.getLength();
			}
			if (start.equals(end)) {
				return 0.0;
			}
		}
		return null;
	}

	/**
	 * Find the straight line between 2 nodes using their geometric data
	 * 
	 * @param start
	 *            The starting location
	 * @param end
	 *            The ending location
	 * @return The double number representing the straight line between the 2 selected nodes
	 */
	public Double findStraightLineBetweenNodes(GeographicPoint start, GeographicPoint end) {
		// The actual geometric distance is multiplied by a factor of 100 to
		// resemble distance in kilometers
		return 100 * Math.sqrt(Math.pow(end.getX() - start.getX(), 2.0) + Math.pow(end.getY() - start.getY(), 2.0));
	}

	// main method for testing
	public static void main(String[] args) {
		/*
		 * Basic testing System.out.print("Making a new map..."); MapGraph theMap = new MapGraph(); System.out.print(
		 * "DONE. \nLoading the map..."); GraphLoader.loadRoadMap("data/testdata/simpletest.map", theMap);
		 * System.out.println("DONE.");
		 */

		// more advanced testing
		/*System.out.print("Making a new map...");
		/*
		 * MapGraph theMap = new MapGraph(); System.out.print( "DONE. \nLoading the map...");
		 * 
		 * GraphLoader.loadRoadMap("data/testdata/simpletest.map", theMap); System.out.println("DONE.");
		 * 
		 * System.out.println("Num nodes: " + theMap.getNumVertices()); System.out.println("Num edges: " +
		 * theMap.getNumEdges());
		 * 
		 * List<GeographicPoint> route = theMap.bfs(new GeographicPoint(1.0,1.0), new GeographicPoint(8.0,-1.0));
		 * 
		 * System.out.println(route);
		 */

		// Use this code in Week 3 End of Week Quiz
		/*MapGraph theMap = new MapGraph();
		System.out.print("DONE. \nLoading the map...");
		GraphLoader.loadRoadMap("data/testdata/dense.map", theMap);
		System.out.println("DONE.");*/

		// MapEdge edge = theMap.findEdgeByStartEndPoints(new
		// GeographicPoint(5.0, 1.0), new GeographicPoint(4.0, 0.0));

		// System.out.println(theMap);
		// GeographicPoint start = new GeographicPoint(32.868629, -117.215393);
		// GeographicPoint end = new GeographicPoint(32.868629, -117.215393);

		// List<GeographicPoint> route = theMap.dijkstra(new
		// GeographicPoint(1.0, 1.0), new GeographicPoint(8.0, -1.0));
		// List<GeographicPoint> route = theMap.dijkstra(new GeographicPoint(1.0, 1.0), new GeographicPoint(8.0, -1.0));

		/*List<GeographicPoint> route = theMap.greedy(new GeographicPoint(1.0, 1.0));

		// List<GeographicPoint> route = theMap.dijkstra(start,end);
		// List<GeographicPoint> route2 = theMap.aStarSearch(new GeographicPoint(1.0, 1.0), new GeographicPoint(8.0,
		// -1.0));

		System.out.println(route);*/
		// System.out.println(route2);

		// System.out.println(theMap.findStraightLineBetweenNodes(new
		// GeographicPoint(5.0, 1.0), new GeographicPoint(8.0, -1.0)));
		MapGraph theMap = new MapGraph();
		System.out.print("DONE. \nLoading the map...");
		GraphLoader.loadRoadMap("data/maps/utc.map", theMap);
		System.out.println("DONE.");

		GeographicPoint start = new GeographicPoint(32.8648772, -117.2254046);
		GeographicPoint end = new GeographicPoint(32.8660691, -117.217393);

		/*List<GeographicPoint> route = theMap.dijkstra(start,end);
		System.out.println("DJ nodes "+route.size());
		List<GeographicPoint> route2 = theMap.aStarSearch(start,end);
		System.out.println("Astar nodes "+route2.size());*/
		List<GeographicPoint> route3 = theMap.greedy(start);
		System.out.println("Astar nodes "+route3.size());
	}

}