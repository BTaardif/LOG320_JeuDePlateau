// AIPlayer.java
// Implements Minimax with Alpha-Beta pruning.
import java.util.Collections;
import java.util.List;

public class AIPlayer {

    private int aiPlayer; // Board.PLAYER_X or Board.PLAYER_O
    private int opponentPlayer;
    private long startTime;
    private final long timeLimitMillis = 2800; // Slightly less than 3 seconds [cite: 27, 48]

    public AIPlayer(int player) {
        this.aiPlayer = player;
        this.opponentPlayer = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;
    }

    // Entry point for finding the best move
    public int[] findBestMove(GiantBoard board) {
        startTime = System.currentTimeMillis();
        System.out.println("AI (" + (aiPlayer == Board.PLAYER_X ? "X" : "O") + ") is thinking...");
        System.out.println("Current board state:\n" + board);


        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        int depth = 1; // Start with depth 1 for iterative deepening

        List<int[]> availableMoves = board.getAvailableMoves();
        if(availableMoves.isEmpty()){
            System.out.println("AI: No available moves!");
            return null; // Should not happen if called correctly
        }
        // Simple fallback if no moves found within time
        bestMove = availableMoves.get(0);


        try {
            while (System.currentTimeMillis() - startTime < timeLimitMillis) {
                System.out.println("Trying depth: " + depth);
                int[] currentBestMoveAtDepth = null;
                int currentBestScoreAtDepth = Integer.MIN_VALUE; // Reset best score for this depth

                // Introduce randomness by shuffling moves at the top level
                Collections.shuffle(availableMoves);


                for (int[] move : availableMoves) {
                    if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
                        System.out.println("Time limit reached during depth " + depth + " exploration.");
                        throw new RuntimeException("TimeLimitExceeded"); // Use exception to break out
                    }

                    GiantBoard nextBoard = new GiantBoard(board); // Create a copy
                    if (nextBoard.makeMove(move[0], move[1], aiPlayer)) {
                        int score = minimax(nextBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false); // Start minimizing opponent's score
                        if (score > currentBestScoreAtDepth) {
                            currentBestScoreAtDepth = score;
                            currentBestMoveAtDepth = move;
                        }
                    } else {
                        System.err.println("AI Warning: Considered invalid move during simulation: " + move[0] + "," + move[1]);
                    }
                }

                // If a valid move was found at this depth and time allows, update the overall best move
                if (currentBestMoveAtDepth != null) {
                    bestMove = currentBestMoveAtDepth;
                    bestScore = currentBestScoreAtDepth; // Keep track of the score for the best move found so far
                    System.out.println("Depth " + depth + " completed. Best move so far: ["+ bestMove[0] + "," + bestMove[1] + "] with score: " + bestScore);
                } else {
                    System.out.println("Depth " + depth + " completed but no improving move found or all moves were invalid simulations.");
                    // If no move was found at this depth, stick with the best from the previous depth
                }

                depth++; // Increase depth for the next iteration
            }
        } catch (RuntimeException e) {
            if (!"TimeLimitExceeded".equals(e.getMessage())) {
                throw e; // Re-throw unexpected exceptions
            }
            System.out.println("Stopping search due to time limit.");
        }


        if (bestMove == null) {
            System.out.println("AI Warning: No best move found within time limit, returning first available move.");
            bestMove = availableMoves.get(0); // Fallback: return the first available move
        }

        System.out.println("AI chose move: [" + bestMove[0] + "," + bestMove[1] + "] with score: " + bestScore + " (completed depth " + (depth -1) + ")");
        return bestMove;
    }

    // Minimax algorithm with Alpha-Beta Pruning [cite: 47, 59]
    private int minimax(GiantBoard board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
            throw new RuntimeException("TimeLimitExceeded"); // Propagate time limit exceeded
        }


        int winner = board.getGlobalWinner();
        if (winner != Board.EMPTY || depth == 0 || board.isGameOver()) {
            return board.evaluate(aiPlayer); // Return heuristic evaluation
        }

        List<int[]> availableMoves = board.getAvailableMoves();
        // Shuffle moves to explore different branches first in case of time-out
        // Doesn't affect correctness but might find better moves faster under time pressure.
        Collections.shuffle(availableMoves);


        if (isMaximizingPlayer) { // AI's turn (maximize score)
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : availableMoves) {
                GiantBoard nextBoard = new GiantBoard(board);
                if (nextBoard.makeMove(move[0], move[1], aiPlayer)) {
                    int eval = minimax(nextBoard, depth - 1, alpha, beta, false); // Switch to minimizing
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval); // Update alpha
                    if (beta <= alpha) {
                        break; // Beta cut-off
                    }
                } else {
                    // This shouldn't happen if getAvailableMoves is correct
                    System.err.println("AI Minimax Warning (Max): Invalid move simulated " + move[0] + "," + move[1]);
                }

            }
            // If no moves were possible from this state (should only happen if isGameOver is true, caught above)
            if (availableMoves.isEmpty()) {
                return board.evaluate(aiPlayer);
            }
            return maxEval;
        } else { // Opponent's turn (minimize score)
            int minEval = Integer.MAX_VALUE;
            for (int[] move : availableMoves) {
                GiantBoard nextBoard = new GiantBoard(board);
                if(nextBoard.makeMove(move[0], move[1], opponentPlayer)) {
                    int eval = minimax(nextBoard, depth - 1, alpha, beta, true); // Switch to maximizing
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval); // Update beta
                    if (beta <= alpha) {
                        break; // Alpha cut-off
                    }
                } else {
                    // This shouldn't happen if getAvailableMoves is correct
                    System.err.println("AI Minimax Warning (Min): Invalid move simulated " + move[0] + "," + move[1]);
                }

            }
            // If no moves were possible from this state
            if (availableMoves.isEmpty()) {
                return board.evaluate(aiPlayer);
            }
            return minEval;
        }
    }
}