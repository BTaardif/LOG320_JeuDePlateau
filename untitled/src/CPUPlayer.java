import java.util.ArrayList;
import java.util.Collections;

public class CPUPlayer {

    private int aiMark; // Board.X or Board.O
    private int opponentMark;
    private long startTime;
    private final long timeLimitMillis = 2900; // un peu moins que 3 seconds

    public CPUPlayer(int aiMark) {
        this.aiMark = aiMark;
        this.opponentMark = (aiMark == LocalBoard.X) ? LocalBoard.O : LocalBoard.X;
    }

    // --- Trouver un meilleur coup ---
    public GlobalMove findBestMove(GlobalBoard board, GlobalMove lastOpponentMove) {
        this.startTime = System.currentTimeMillis();
        GlobalMove bestMoveFound = null;

        System.out.println("AI (" + (aiMark == LocalBoard.X ? "X" : "O") + ") thinking...");

        // --- D'abord verifier si on peut ganger directement ---
        ArrayList<GlobalMove> possibleMoves = board.getPossibleMoves(lastOpponentMove);
        for (GlobalMove immediateMove : possibleMoves) {
            GlobalBoard testBoard = new GlobalBoard(board);
            if (testBoard.play(immediateMove, this.aiMark)) { // Check if move is valid
                if (testBoard.checkGlobalWinner() == this.aiMark) {
                    System.out.println("Found immediate winning move: " + moveToString(immediateMove));
                    return immediateMove;
                }
            }
        }

        // If no immediate win, proceed with iterative deepening search
        int maxDepth = 1;
        try {
            while (System.currentTimeMillis() - startTime < timeLimitMillis) {
                System.out.println("  Trying depth: " + maxDepth);
                // Pass the already generated list (or regenerate if needed)
                // Make sure minimax uses a *copy* of the board for exploration
                MoveScore currentBest = minimaxAlphaBeta(new GlobalBoard(board), lastOpponentMove, maxDepth,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, true);

                // ...(rest of the iterative deepening loop as before)...
                if (currentBest != null && currentBest.move != null) {
                    bestMoveFound = currentBest.move;
                    System.out.println("  Depth " + maxDepth + " found move: " + moveToString(bestMoveFound)
                            + " with score: " + currentBest.score);
                } else {
                    System.out.println("  Depth " + maxDepth + " found no better move or timed out partially.");
                    if (currentBest == null)
                        break; // Break if timeout occurred deeper
                }

                // Check time again *after* completing a depth
                if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
                    System.out.println("  Time limit reached after completing depth " + maxDepth);
                    break;
                }

                maxDepth++;

            } // End while loop
        } catch (TimeoutException e) {
            System.out.println("  Search timed out during exploration.");
        }

        if (bestMoveFound == null) {
            System.err.println(
                    "WARNING: AI could not find a move (timeout or no valid moves?). Returning fallback move.");
            // Use the list generated earlier for fallback
            if (!possibleMoves.isEmpty()) {
                // Fallback: Choose first valid move if search failed completely
                // Shuffling here makes fallback random, but a simple heuristic might be better
                Collections.shuffle(possibleMoves);
                bestMoveFound = possibleMoves.get(0);
            } else {
                System.err.println("CRITICAL WARNING: No possible moves available for AI!");
                return null;
            }
        }

        System.out.println("AI chose move: " + moveToString(bestMoveFound));
        return bestMoveFound;
    }

    private MoveScore minimaxAlphaBeta(GlobalBoard currentBoard, GlobalMove lastMoveMade, int depth, int alpha,
            int beta, boolean isMaximizingPlayer) throws TimeoutException {

        // Check for timeout
        if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
            throw new TimeoutException();
        }

        // Check for terminal state (global win/loss/draw) or depth limit
        int globalWinner = currentBoard.checkGlobalWinner();
        if (globalWinner != LocalBoard.EMPTY || depth == 0 || currentBoard.isGlobalBoardFull()) {
            // Return evaluation score relative to the AI player
            return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
        }

        ArrayList<GlobalMove> possibleNextMoves = currentBoard.getPossibleMoves(lastMoveMade);
        Collections.shuffle(possibleNextMoves);

        GlobalMove bestMoveForThisNode = null;

        if (isMaximizingPlayer) { // AI's turn (Maximize)
            int maxEval = Integer.MIN_VALUE;
            for (GlobalMove move : possibleNextMoves) {
                GlobalBoard nextBoard = new GlobalBoard(currentBoard);
                boolean played = nextBoard.play(move, this.aiMark);
                if (!played)
                    continue;

                MoveScore evalResult = minimaxAlphaBeta(nextBoard, move, depth - 1, alpha, beta, false);
                if (evalResult == null)
                    continue;

                if (evalResult.score > maxEval) {
                    maxEval = evalResult.score;
                    bestMoveForThisNode = move;
                }
                alpha = Math.max(alpha, evalResult.score);
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }
            if (possibleNextMoves.isEmpty()) {
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
                if (evalResult == null)
                    continue;

                if (evalResult.score < minEval) {
                    minEval = evalResult.score;
                    bestMoveForThisNode = move;
                }
                beta = Math.min(beta, evalResult.score);
                if (beta <= alpha) {
                    break;
                }
            }
            if (possibleNextMoves.isEmpty()) { // Handle case where a player has no moves
                return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
            }
            return new MoveScore(bestMoveForThisNode, minEval);
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

        int overallCol = move.getGlobalCol() * 3 + move.getLocalCol(); // 0-8
        int overallRow = move.getGlobalRow() * 3 + move.getLocalRow(); // 0-8

        char colChar = (char) ('A' + overallCol);
        char rowChar = (char) ('1' + (8 - overallRow));
        rowChar = (char) ('1' + overallRow);

        return "" + colChar + rowChar;
    }
}