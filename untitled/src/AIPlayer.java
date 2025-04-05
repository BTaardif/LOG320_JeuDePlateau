import java.util.ArrayList;
import java.util.Collections;

public class AIPlayer {

    private int aiMark; // Board.X or Board.O
    private int opponentMark;
    private long startTime;
    private final long timeLimitMillis = 2900; // Slightly less than 3 seconds

    public AIPlayer(int aiMark) {
        this.aiMark = aiMark;
        this.opponentMark = (aiMark == Board.X) ? Board.O : Board.X;
    }

    // --- Public method to find the best move ---
    public GlobalMove findBestMove(GlobalBoard board, GlobalMove lastOpponentMove) {
        this.startTime = System.currentTimeMillis();
        GlobalMove bestMoveFound = null;
        int maxDepth = 1; // Start with depth 1

        System.out.println("AI (" + (aiMark == Board.X ? "X" : "O") + ") thinking...");

        try {
            // Iterative Deepening: Increase depth until time limit approaches
            while (System.currentTimeMillis() - startTime < timeLimitMillis) {
                System.out.println("  Trying depth: " + maxDepth);
                MoveScore currentBest = minimaxAlphaBeta(new GlobalBoard(board), lastOpponentMove, maxDepth,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, true);

                // If a valid move was found at this depth and time allows, update bestMoveFound
                if (currentBest != null && currentBest.move != null) {
                    bestMoveFound = currentBest.move;
                    System.out.println("  Depth " + maxDepth + " found move: " + moveToString(bestMoveFound)
                            + " with score: " + currentBest.score);
                } else {
                    System.out.println("  Depth " + maxDepth + " found no better move or timed out partially.");
                    // If minimax returns null (maybe timed out mid-search at this depth), break
                    if (currentBest == null)
                        break;
                }

                // Check time again *after* completing a depth
                if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
                    System.out.println("  Time limit reached after completing depth " + maxDepth);
                    break;
                }

                maxDepth++;
                // Optional: Add a maximum search depth cap if needed
                // if (maxDepth > 10) break;
            }
        } catch (TimeoutException e) {
            System.out.println("  Search timed out during exploration.");
        }

        if (bestMoveFound == null) {
            System.err.println(
                    "WARNING: AI could not find a move (timeout or no valid moves?). Returning random valid move.");
            // Fallback: If no move found (e.g., timed out on depth 1), pick a random valid
            // one.
            ArrayList<GlobalMove> possibleMoves = board.getPossibleMoves(lastOpponentMove);
            if (!possibleMoves.isEmpty()) {
                Collections.shuffle(possibleMoves); // Randomize
                bestMoveFound = possibleMoves.get(0);
            } else {
                // This should ideally not happen if the game isn't over
                System.err.println("CRITICAL WARNING: No possible moves available for AI!");
                return null; // Or handle game end appropriately
            }
        }

        System.out.println("AI chose move: " + moveToString(bestMoveFound));
        return bestMoveFound;
    }

    // --- Minimax with Alpha-Beta Pruning ---
    private MoveScore minimaxAlphaBeta(GlobalBoard currentBoard, GlobalMove lastMoveMade, int depth, int alpha,
            int beta, boolean isMaximizingPlayer) throws TimeoutException {

        // Check for timeout
        if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
            throw new TimeoutException();
        }

        // Check for terminal state (global win/loss/draw) or depth limit
        int globalWinner = currentBoard.checkGlobalWinner();
        if (globalWinner != Board.EMPTY || depth == 0 || currentBoard.isGlobalBoardFull()) {
            // Return evaluation score relative to the AI player
            return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
        }

        ArrayList<GlobalMove> possibleNextMoves = currentBoard.getPossibleMoves(lastMoveMade);
        // Optional: Randomize move order slightly to explore different branches first
        // in iterative deepening
        Collections.shuffle(possibleNextMoves);

        GlobalMove bestMoveForThisNode = null;

        if (isMaximizingPlayer) { // AI's turn (Maximize)
            int maxEval = Integer.MIN_VALUE;
            for (GlobalMove move : possibleNextMoves) {
                GlobalBoard nextBoard = new GlobalBoard(currentBoard);
                boolean played = nextBoard.play(move, this.aiMark);
                if (!played)
                    continue; // Should not happen if getPossibleMoves is correct

                MoveScore evalResult = minimaxAlphaBeta(nextBoard, move, depth - 1, alpha, beta, false); // Opponent's
                                                                                                         // turn next
                if (evalResult == null)
                    continue; // Might happen if timeout occurred deeper

                if (evalResult.score > maxEval) {
                    maxEval = evalResult.score;
                    bestMoveForThisNode = move;
                }
                alpha = Math.max(alpha, evalResult.score);
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }
            if (possibleNextMoves.isEmpty()) { // Handle case where a player has no moves (should be a draw/win state)
                return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
            }
            return new MoveScore(bestMoveForThisNode, maxEval);

        } else { // Opponent's turn (Minimize)
            int minEval = Integer.MAX_VALUE;
            for (GlobalMove move : possibleNextMoves) {
                GlobalBoard nextBoard = new GlobalBoard(currentBoard);
                boolean played = nextBoard.play(move, this.opponentMark);
                if (!played)
                    continue;

                MoveScore evalResult = minimaxAlphaBeta(nextBoard, move, depth - 1, alpha, beta, true); // AI's turn
                                                                                                        // next
                if (evalResult == null)
                    continue; // Might happen if timeout occurred deeper

                if (evalResult.score < minEval) {
                    minEval = evalResult.score;
                    bestMoveForThisNode = move; // Note: we primarily need the *score* for min node
                }
                beta = Math.min(beta, evalResult.score);
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
            if (possibleNextMoves.isEmpty()) { // Handle case where a player has no moves
                return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
            }
            return new MoveScore(bestMoveForThisNode, minEval); // Return score, move isn't directly used by caller here
        }
    }

    // --- Helper class to store move and its score ---
    private static class MoveScore {
        GlobalMove move;
        int score;

        MoveScore(GlobalMove move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    // --- Custom Exception for Timeout ---
    private static class TimeoutException extends Exception {
        public TimeoutException() {
            super("Search time limit exceeded.");
        }
    }

    // --- Utility to convert move to server string ---
    public static String moveToString(GlobalMove move) {
        if (move == null)
            return "";
        // Global board uses 0-2, local board uses 0-2
        // Server needs A1-I9 format
        // Global Col 0-2 -> Local Col 0-2 => Overall Col 0-8 -> A-I
        // Global Row 0-2 -> Local Row 0-2 => Overall Row 0-8 -> 1-9 (inverted?) Check
        // PDF Figure 1 mapping
        // PDF Figure 1 suggests: A-I maps to columns 0-8. 1-9 maps to rows 0-8 (row 1
        // is index 0, row 9 is index 8)

        int overallCol = move.getGlobalCol() * 3 + move.getLocalCol(); // 0-8
        int overallRow = move.getGlobalRow() * 3 + move.getLocalRow(); // 0-8

        char colChar = (char) ('A' + overallCol); // A-I
        char rowChar = (char) ('1' + (8 - overallRow)); // 1-9 (mapping 0->9, 1->8 ... 8->1 seems wrong, let's try 0->1,
                                                        // 8->9)
        rowChar = (char) ('1' + overallRow); // Correct mapping: 0->'1', 8->'9'

        return "" + colChar + rowChar;
    }
}