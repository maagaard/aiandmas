package sc;

import java.util.Comparator;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
    public int goalRow, goalColumn;
	public Heuristic(Node initialState) {
		this.initialState = initialState;

        char[][] goals = initialState.goals;

        for (int i = 0; i < goals.length; i++) {
            for (int j = 0; j < goals.length; j++) {
                if (goals[i][j] != 0) {
                    goalRow = i;
                    goalColumn = j;
                }
            }
        }
	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

	public int h( Node n ) {

        double a = Math.pow(n.agentCol-goalColumn, 2);
        double b = Math.pow(n.agentRow-goalRow, 2);
        double distance = Math.sqrt(a+b);
//        System.err.println("a: "+a+ " b: "+b + " c: " + distance);
		return (int)distance;
	}

	public abstract int f( Node n );

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super( initialState );
		}

		public int f( Node n ) {
//            System.err.println("g: "+n.g()+ " h: "+h(n));
			return n.g() + h( n );
		}

		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState) {
			super( initialState );
			W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
		}

		public int f( Node n ) {
			return n.g() + W * h( n );
		}

		public String toString() {
			return String.format( "WA*(%d) evaluation", W );
		}
	}

	public static class Greedy extends Heuristic {

		public Greedy(Node initialState) {
			super( initialState );
		}

		public int f( Node n ) {
			return h( n );
		}

		public String toString() {
			return "Greedy evaluation";
		}
	}
}
