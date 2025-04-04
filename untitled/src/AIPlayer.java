import java.util.ArrayList;

public class AIPlayer {
    private static final int MAX_DEPTH = 5;  // Adjust this or use iterative deepening as needed.
    private static final int PLAYER_X = 4;
    private static final int PLAYER_O = 2;

    /**
     * Returns the best global move for the given board state.
     * 
     * @param board The current GlobalBoard.
     * @param activeLocalRow The row index (0-2) of the forced local board, or null if free move.
     * @param activeLocalCol The column index (0-2) of the forced local board, or null if free move.
     * @param player The AI player's mark (PLAYER_X or PLAYER_O).
     * @return The chosen GlobalMove.
     */
    public GlobalMove getBestMove(GlobalBoard board, Integer activeLocalRow, Integer activeLocalCol, int player) {
        MoveScorePair result = minimax(board, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, activeLocalRow, activeLocalCol, player);
        return result.move;
    }

    /**
     * The minimax algorithm with alpha-beta pruning.
     */
    private MoveScorePair minimax(GlobalBoard board, int depth, int alpha, int beta, boolean maximizingPlayer,
                                   Integer activeLocalRow, Integer activeLocalCol, int player) {
        // Terminal condition: maximum depth reached or game over.
        if (depth == 0 || board.getGlobalState() != GlobalBoard.GlobalBoardState.ONGOING) {
            int eval = evaluateGlobalBoard(board, player);
            return new MoveScorePair(null, eval);
        }
        
        ArrayList<GlobalMove> moves = board.getPossibleGlobalMoves(activeLocalRow, activeLocalCol);
        GlobalMove bestMove = null;
        
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (GlobalMove move : moves) {
                GlobalBoard newBoard = new GlobalBoard(board); // Deep copy for simulation.
                int globalRow = move.getLocalBoardRow() * 3 + move.getCellRow();
                int globalCol = move.getLocalBoardCol() * 3 + move.getCellCol();
                newBoard.playMove(globalRow, globalCol, player);
                
                // Determine the next forced local board.
                Integer nextActiveLocalRow = move.getCellRow();
                Integer nextActiveLocalCol = move.getCellCol();
                if (newBoard.getLocalBoard(nextActiveLocalRow, nextActiveLocalCol).getState() != LocalBoard.BoardState.ONGOING) {
                    nextActiveLocalRow = null;
                    nextActiveLocalCol = null;
                }
                
                MoveScorePair result = minimax(newBoard, depth - 1, alpha, beta, false, nextActiveLocalRow, nextActiveLocalCol, player);
                if (result.score > maxEval) {
                    maxEval = result.score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, maxEval);
                if (beta <= alpha) break; // Beta cutoff.
            }
            return new MoveScorePair(bestMove, maxEval);
        } else {
            int minEval = Integer.MAX_VALUE;
            int opponent = (player == PLAYER_X) ? PLAYER_O : PLAYER_X;
            for (GlobalMove move : moves) {
                GlobalBoard newBoard = new GlobalBoard(board);
                int globalRow = move.getLocalBoardRow() * 3 + move.getCellRow();
                int globalCol = move.getLocalBoardCol() * 3 + move.getCellCol();
                newBoard.playMove(globalRow, globalCol, opponent);
                
                Integer nextActiveLocalRow = move.getCellRow();
                Integer nextActiveLocalCol = move.getCellCol();
                if (newBoard.getLocalBoard(nextActiveLocalRow, nextActiveLocalCol).getState() != LocalBoard.BoardState.ONGOING) {
                    nextActiveLocalRow = null;
                    nextActiveLocalCol = null;
                }
                
                MoveScorePair result = minimax(newBoard, depth - 1, alpha, beta, true, nextActiveLocalRow, nextActiveLocalCol, player);
                if (result.score < minEval) {
                    minEval = result.score;
                    bestMove = move;
                }
                beta = Math.min(beta, minEval);
                if (beta <= alpha) break; // Alpha cutoff.
            }
            return new MoveScorePair(bestMove, minEval);
        }
    }

    /**
     * A basic heuristic evaluation for the global board.
     */
    private int evaluateGlobalBoard(GlobalBoard board, int player) {
        GlobalBoard.GlobalBoardState state = board.getGlobalState();
        if (state == GlobalBoard.GlobalBoardState.X_WON) {
            return (player == PLAYER_X) ? 100000 : -100000;
        } else if (state == GlobalBoard.GlobalBoardState.O_WON) {
            return (player == PLAYER_O) ? 100000 : -100000;
        } else if (state == GlobalBoard.GlobalBoardState.DRAW) {
            return 0;
        }
        
        int score = 0;
        // Evaluate each local board.
        for (int i = 0; i < GlobalBoard.SIZE; i++) {
            for (int j = 0; j < GlobalBoard.SIZE; j++) {
                LocalBoard lb = board.getLocalBoard(i, j);
                score += lb.evaluate(player);
            }
        }
        // Additional heuristics (e.g., global win line potential) can be added here.
        return score;
    }
    
    /**
     * Helper inner class to store a move and its corresponding evaluation score.
     */
    private class MoveScorePair {
        GlobalMove move;
        int score;
        
        public MoveScorePair(GlobalMove move, int score) {
            this.move = move;
            this.score = score;
        }
    }
}
