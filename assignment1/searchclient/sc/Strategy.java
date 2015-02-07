package sc;

import java.util.*;

import sc.SearchClient.Memory;

public abstract class Strategy {

	public HashSet< Node > explored;
	public long startTime = System.currentTimeMillis();

	public Strategy() {
		explored = new HashSet< Node >();
	}

	public void addToExplored( Node n ) {
		explored.add( n );
	}

	public boolean isExplored( Node n ) {
		return explored.contains( n );
	}

	public int countExplored() {
		return explored.size();
	}

	public String searchStatus() {
		return String.format( "#Explored: %4d, #Frontier: %3d, Time: %3.2f s \t%s", countExplored(), countFrontier(), timeSpent(), Memory.stringRep() );
	}
	
	public float timeSpent() {
		return ( System.currentTimeMillis() - startTime ) / 1000f;
	}

	public abstract Node getAndRemoveLeaf();

	public abstract void addToFrontier( Node n );

	public abstract boolean inFrontier( Node n );

	public abstract int countFrontier();

	public abstract boolean frontierIsEmpty();
	
	public abstract String toString();

	public static class StrategyBFS extends Strategy {

		private ArrayDeque< Node > frontier;

		public StrategyBFS() {
			super();
			frontier = new ArrayDeque< Node >();
		}

		public Node getAndRemoveLeaf() {
			return frontier.pollFirst();
		}

		public void addToFrontier( Node n ) {
			frontier.addLast( n );
		}

		public int countFrontier() {
			return frontier.size();
		}

		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		public boolean inFrontier( Node n ) {
			return frontier.contains( n );
		}

		public String toString() {
			return "Breadth-first Search";
		}
	}

	public static class StrategyDFS extends Strategy {

		private ArrayDeque< Node > frontier;

		public StrategyDFS() {
			super();
			frontier = new ArrayDeque<Node>();
		}

		public Node getAndRemoveLeaf() {
			return frontier.pop();
		}

		public void addToFrontier( Node n ) {
			frontier.push(n);

		}

		public int countFrontier() {
			return frontier.size();
		}

		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		public boolean inFrontier( Node n ) {
			return frontier.contains(n);
		}

		public String toString() {
			return "Depth-first Search";
		}
	}

	// Ex 3: Best-first Search uses a priority queue (Java contains no implementation of a Heap data structure)
	public static class StrategyBestFirst extends Strategy {
		private Heuristic heuristic;

		private List<Node> frontier;
//		private HashMap<Node, Integer> cost = new HashMap<Node, Integer>();

		public StrategyBestFirst( Heuristic h ) {
			super();
			heuristic = h;
//			frontier = new ArrayDeque<Node>();
            frontier = new ArrayList<Node>();

			// Unimplemented
		}
		public Node getAndRemoveLeaf() {
			// Unimplemented
//            List<Integer> distance = new ArrayList<Integer>();

//			for (Node node : frontier) {
//				distance.add(heuristic.f(node));
//                cost.put(node, heuristic.f(node));
//
//                heuristic.compare(node, )
//
//			}
//            for (Integer distance : cost.values()) {
//
//            }

            //frontier.pop();

            Node bestNode = frontier.get(0);
            frontier.remove(0);
			return bestNode;
		}

		public void addToFrontier( Node n ) {
			// Unimplemented

            int comparison;
            for (Node node : frontier) {
                comparison = heuristic.compare(n, node);
                if (comparison <= 0) {
                    frontier.add(frontier.indexOf(node), n);
                    return;
                }
            }
            frontier.add(n);

//            if (frontier.size() == 0) {
//                frontier.add(n);
//            } else {
//                int comparison;
//
//                for (Node node : frontier) {
//                    comparison = heuristic.compare(n, node);
//                    if (comparison <= 0) {
//                        frontier.add(frontier.indexOf(node), n);
//                        return;
//                    }
//                }
//                frontier.add(n);
//                frontier.push(n);
//                cost.put(n, heuristic.f(n));
//            }

		}

		public int countFrontier() {
			frontier.size();
			return 0;
		}

		public boolean frontierIsEmpty() {
			return frontier.isEmpty();
		}

		public boolean inFrontier( Node n ) {
			return frontier.contains(n);
		}

		public String toString() {
			return "Best-first Search (PriorityQueue) using " + heuristic.toString();
		}
	}
}
