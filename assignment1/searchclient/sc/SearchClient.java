package sc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import sc.Heuristic.*;
import sc.Strategy.*;

public class SearchClient {
	// Auxiliary static classes
	public static void error( String msg ) throws Exception {
		throw new Exception( "GSCError: " + msg );
	}

	public static class Memory {
		public static Runtime runtime = Runtime.getRuntime();
		public static final float mb = 1024 * 1024;
		public static final float limitRatio = .9f;
		public static final int timeLimit = 180;

		public static float used() {
			return ( runtime.totalMemory() - runtime.freeMemory() ) / mb;
		}

		public static float free() {
			return runtime.freeMemory() / mb;
		}

		public static float total() {
			return runtime.totalMemory() / mb;
		}

		public static float max() {
			return runtime.maxMemory() / mb;
		}

		public static boolean shouldEnd() {
			return ( used() / max() > limitRatio );
		}

		public static String stringRep() {
			return String.format( "[Used: %.2f MB, Free: %.2f MB, Alloc: %.2f MB, MaxAlloc: %.2f MB]", used(), free(), total(), max() );
		}
	}

	public Node initialState = null;

	public SearchClient( BufferedReader serverMessages ) throws Exception {
		Map< Character, String > colors = new HashMap< Character, String >();
		String line, color = "";

		int agentCol = -1, agentRow = -1;
		int colorLines = 0, levelLines = 0;

		// Read lines specifying colors
		while ( ( line = serverMessages.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			line = line.replaceAll( "\\s", "" );
			String[] colonSplit = line.split( ":" );
			color = colonSplit[0].trim();

			for ( String id : colonSplit[1].split( "," ) ) {
				colors.put( id.trim().charAt( 0 ), color );
			}
			colorLines++;
		}
		
		if ( colorLines > 0 ) {
			error( "Box colors not supported" );
		}


        int MAX_SIZE = 100;

        boolean[][] walls = new boolean[MAX_SIZE][MAX_SIZE];
        char[][] boxes = new char[MAX_SIZE][MAX_SIZE];
        char[][] goals = new char[MAX_SIZE][MAX_SIZE];
        int initialAgentRow = 0;
        int initialAgentCol = 0;
        int longestLine = 0;

        while ( !line.equals( "" ) ) {
            int length = line.length();
            if(length > longestLine) longestLine = length;

            for ( int i = 0; i < length; i++ ) {
                char chr = line.charAt( i );
                if ( '+' == chr ) { // Walls
                    walls[levelLines][i] = true;
                } else if ( '0' <= chr && chr <= '9' ) { // Agents
                    if ( agentCol != -1 || agentRow != -1 ) {
                        error( "Not a single agent level" );
                    }
                    initialAgentRow = levelLines;
                    initialAgentCol = i;
                } else if ( 'A' <= chr && chr <= 'Z' ) { // Boxes
                    boxes[levelLines][i] = chr;
                } else if ( 'a' <= chr && chr <= 'z' ) { // Goal cells
                    goals[levelLines][i] = chr;
                }
            }
            line = serverMessages.readLine();
            levelLines++;
        }

        //Initialize new agent
        initialState = new Node(null, levelLines +1, longestLine+1);

        System.err.format("Column size: %d, row size: %d\n", Node.MAX_COLUMN, Node.MAX_ROW);
        //Copy walls, boxes and goals into smaller array
        for(int i = 0; i < Node.MAX_ROW; ++i) {
            for (int j = 0; j < Node.MAX_COLUMN ; j++) {
                initialState.walls[i][j] = walls[i][j];
                initialState.goals[i][j] = goals[i][j];
                initialState.boxes[i][j] = boxes[i][j];
            }
        }

        initialState.agentCol = initialAgentCol;
        initialState.agentRow = initialAgentRow;

	}

	public LinkedList< Node > Search( Strategy strategy ) throws IOException {
		System.err.format( "Search starting with strategy %s\n", strategy );
		strategy.addToFrontier( this.initialState );

		int iterations = 0;
		while ( true ) {
			if ( iterations % 200 == 0 ) {
				System.err.println( strategy.searchStatus() );
			}
			if ( Memory.shouldEnd() ) {
				System.err.format( "Memory limit almost reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}
			if ( strategy.timeSpent() > 300 ) { // Minutes timeout
				System.err.format( "Time limit reached, terminating search %s\n", Memory.stringRep() );
				return null;
			}

			if ( strategy.frontierIsEmpty() ) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored( leafNode );
			for ( Node n : leafNode.getExpandedNodes() ) {
				if ( !strategy.isExplored( n ) && !strategy.inFrontier( n ) ) {
					strategy.addToFrontier( n );
				}
			}
			iterations++;
		}
	}

	public static void main( String[] args ) throws Exception {
		BufferedReader serverMessages = new BufferedReader( new InputStreamReader( System.in ) );

		// Use stderr to print to console
		System.err.println( "SearchClient initializing. I am sending this using the error output stream." );

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient( serverMessages );

		Strategy strategy = null;
//		strategy = new StrategyBFS();
		// Ex 1:
//		strategy = new StrategyDFS();
		
		// Ex 3:
		strategy = new StrategyBestFirst( new AStar( client.initialState ) );
//		strategy = new StrategyBestFirst( new WeightedAStar( client.initialState ) );
//		strategy = new StrategyBestFirst( new Greedy( client.initialState ) );

		LinkedList< Node > solution = client.Search( strategy );

		if ( solution == null ) {
			System.err.println( "Unable to solve level" );
			System.exit( 0 );
		} else {
			System.err.println( "\nSummary for " + strategy );
			System.err.println( "Found solution of length " + solution.size() );
			System.err.println( strategy.searchStatus() );

			for ( Node n : solution ) {
				String act = n.action.toActionString();
				System.out.println( act );
				String response = serverMessages.readLine();
				if ( response.contains( "false" ) ) {
					System.err.format( "Server responsed with %s to the inapplicable action: %s\n", response, act );
					System.err.format( "%s was attempted in \n%s\n", act, n );
					break;
				}
			}

		}
	}
}
