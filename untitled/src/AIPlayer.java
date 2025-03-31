// AIPlayer.java (Revised)
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AIPlayer {
    private int aiPlayer;
    private int opponentPlayer;
    private long startTime;
    private final long timeLimitMillis = 2800; // Slightly less than 3 seconds for safety buffer
    private int completedDepth = 0; // Track depth completed within time limit


    public AIPlayer(int player) {
        this.aiPlayer = player;
        this.opponentPlayer = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;
        if (player != Board.PLAYER_X && player != Board.PLAYER_O) {
            throw new IllegalArgumentException("Invalid player ID for AIPlayer");
        }
    }

    // Main function to find the best move using iterative deepening minimax
    public int[] findBestMove(GiantBoard board) {
        startTime = System.currentTimeMillis();
        completedDepth = 0;
        int[] bestMoveOverall = null; // Best move across *completed* depths
        int bestScoreOverall = Integer.MIN_VALUE;

        List<int[]> availableMoves = board.getAvailableMoves();
        if (availableMoves.isEmpty()) {
            System.err.println("AI Error: No available moves at the start of findBestMove!");
            return null; // No move possible
        }
        // Initial fallback: first available move. Should be replaced by search results.
        bestMoveOverall = availableMoves.get(0);

        int depth = 1;
        try {
            while (true) { // Loop indefinitely until time runs out or error
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= timeLimitMillis) {
                    System.out.println("Time limit nearly reached before starting depth " + depth + ". Using best move from depth " + completedDepth);
                    break; // Exit loop, use bestMoveOverall from previous completed depth
                }
                System.out.println("AI: Starting search at depth " + depth + " (Time elapsed: " + (currentTime - startTime) + "ms)");

                int currentBestScoreAtDepth = Integer.MIN_VALUE;
                int[] currentBestMoveAtDepth = null;

                // Order moves for the current depth using the *original* board state
                // Re-fetch available moves in case the board state changed externally (unlikely but safe)
                List<int[]> orderedMoves = orderMoves(new GiantBoard(board), board.getAvailableMoves(), true); // Use copy for ordering simulation

                if (orderedMoves.isEmpty()) {
                    System.err.println("Warning: No moves returned by getAvailableMoves/orderMoves at depth " + depth + ". Using previous best.");
                    // If availableMoves was not empty initially, but orderedMoves is, something is wrong.
                    // Stick with the best move from the previous depth or the initial fallback.
                    break; // Exit loop
                }


                for (int[] move : orderedMoves) {
                    // Check time *before* processing the move
                    if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
                        // System.out.println("Time limit hit during depth " + depth + " exploration.");
                        throw new TimeLimitExceededException("TimeLimitExceeded during depth " + depth);
                    }

                    // --- Simulate the move on a copy of the board ---
                    GiantBoard boardCopy = new GiantBoard(board); // Work on a copy!

                    // Store necessary info for undo (needed by minimax recursive calls)
                    int prevNextRow = boardCopy.getNextLocalBoardRow();
                    int prevNextCol = boardCopy.getNextLocalBoardCol();

                    if (boardCopy.makeMove(move[0], move[1], aiPlayer)) {
                        // Call minimax on the copy
                        int score = minimax(boardCopy, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false); // Start minimizing for opponent

                        // No need to undo on the copy, it's discarded after this iteration

                        // Update best score/move for *this depth*
                        if (score > currentBestScoreAtDepth) {
                            currentBestScoreAtDepth = score;
                            currentBestMoveAtDepth = move;
                        }
                        // Alpha-beta pruning is handled *within* minimax, not here at the top level
                    } else {
                        // This should NOT happen if getAvailableMoves and isValidMove are correct
                        System.err.println("AI findBestMove: makeMove failed on board copy for supposedly valid move: " + move[0] + "," + move[1]);
                        // Consider how to handle this - maybe skip this move?
                    }
                } // End loop through moves for current depth

                // --- Depth completed successfully ---
                // Check time *after* completing the depth's exploration
                if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
                    System.out.println("Time limit likely hit right after finishing depth " + depth + ". Using result from this depth.");
                    // Update overall best move IF a move was found at this depth
                    if (currentBestMoveAtDepth != null) {
                        bestMoveOverall = currentBestMoveAtDepth;
                        bestScoreOverall = currentBestScoreAtDepth;
                        completedDepth = depth;
                        System.out.println("AI: Completed Depth " + depth + ". Best Move: ["+ bestMoveOverall[0] + "," + bestMoveOverall[1] + "] Score: " + bestScoreOverall);
                    } else {
                        System.out.println("AI: Completed Depth " + depth + " but no move found (?). Using result from depth " + completedDepth);
                        // Keep the previous bestMoveOverall
                    }
                    break; // Exit the while loop due to time
                }


                // If the depth completed within time, update the overall best move
                if (currentBestMoveAtDepth != null) {
                    bestMoveOverall = currentBestMoveAtDepth;
                    bestScoreOverall = currentBestScoreAtDepth;
                    completedDepth = depth; // Mark this depth as successfully completed
                    System.out.println("AI: Completed Depth " + depth + ". Best Move: ["+ bestMoveOverall[0] + "," + bestMoveOverall[1] + "] Score: " + bestScoreOverall);
                } else {
                    // If no move improved the score at this depth (e.g., all moves lead to worse states or errors occurred)
                    System.out.println("AI: Completed Depth " + depth + ". No better move found or error occurred. Sticking with best from depth " + completedDepth);
                    // Do not update bestMoveOverall, keep the one from the last successful depth
                    // Break here? If deeper search yields no better options, maybe stopping is reasonable.
                    // break; // Optional: stop if a depth yields no improvement
                }

                depth++; // Go to the next depth

            } // End while loop (iterative deepening)

        } catch (TimeLimitExceededException e) {
            System.out.println("AI: Search stopped due to time limit (" + e.getMessage() + "). Using best move from completed depth " + completedDepth);
            // bestMoveOverall holds the result from the last successfully completed depth
        } catch (Exception e) {
            System.err.println("AI Error: Unexpected exception during search: " + e);
            e.printStackTrace();
            // Fallback to the first available move if search failed badly
            if (bestMoveOverall == null && !availableMoves.isEmpty()) {
                System.err.println("AI Warning: Search failed, falling back to first available move.");
                bestMoveOverall = availableMoves.get(0);
            } else if (bestMoveOverall == null) {
                System.err.println("AI Fatal Error: Search failed and no available moves exist!");
                return null;
            }
        }

        // Final check if a valid move was selected
        if (bestMoveOverall == null) {
            System.err.println("AI Error: No best move determined after search. Falling back to first available (if any).");
            if (!availableMoves.isEmpty()) {
                bestMoveOverall = availableMoves.get(0);
            } else {
                System.err.println("AI Fatal Error: No best move and no available moves!");
                return null; // Should not happen if initial check passed
            }
        }


        System.out.println("AI chose move: [" + bestMoveOverall[0] + "," + bestMoveOverall[1] + "] based on depth " + completedDepth + " search.");
        return bestMoveOverall;
    }

    // Minimax with Alpha-Beta Pruning
    private int minimax(GiantBoard board, int depth, int alpha, int beta, boolean maximizingPlayer) throws TimeLimitExceededException {
        // Check time limit at the start of each recursive call
        if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
            throw new TimeLimitExceededException("TimeLimitExceeded in minimax depth " + depth);
        }

        // Base case: depth limit reached or game is over
        if (depth == 0 || board.isGameOver()) {
            return board.evaluate(aiPlayer); // Evaluate from AI's perspective
        }

        // Get available moves for the current state
        // Use ordering here as well for subsequent levels
        List<int[]> moves = orderMoves(board, board.getAvailableMoves(), maximizingPlayer);

        // Handle case where no moves are available (should align with isGameOver, but check defensively)
        if (moves.isEmpty()) {
            // System.out.println("DEBUG: Minimax found no moves at depth " + depth + " for player " + (maximizingPlayer ? aiPlayer : opponentPlayer));
            // System.out.println("DEBUG: Board state:\n" + board);
            return board.evaluate(aiPlayer); // Return evaluation of the current terminal/stuck state
        }


        if (maximizingPlayer) { // AI's turn (Maximize score)
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : moves) {
                // Store state *before* making move on the board object passed down
                int prevNextRow = board.getNextLocalBoardRow();
                int prevNextCol = board.getNextLocalBoardCol();
                int prevLocalWinner = board.localBoards[move[0]/3][move[1]/3].getWinner(); // More robust undo state
                int prevGlobalWinner = board.getGlobalWinner();

                if (board.makeMove(move[0], move[1], aiPlayer)) {
                    int eval = minimax(board, depth - 1, alpha, beta, false); // Opponent's turn next

                    // Undo the move *on the same board object* to backtrack
                    board.undoMove(move[0], move[1], prevNextRow, prevNextCol);
                    // Explicitly restore winner states if undoMove isn't fully trusted (Optional, depends on undoMove impl)
                    // board.localBoards[move[0]/3][move[1]/3].winner = prevLocalWinner; // If needed
                    // board.globalWinner = prevGlobalWinner; // If needed
                    // board.recalcGlobalWinner(); // Recalculate after undo if necessary

                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        break; // Beta cut-off
                    }
                } else {
                    System.err.println("Minimax (Max): makeMove failed for " + move[0] + "," + move[1]);
                    // How to handle? Score this path low?
                    // maxEval = Math.max(maxEval, Integer.MIN_VALUE + 1); // Penalize?
                }
            }
            // If no moves were successfully evaluated (e.g., all failed makeMove), return current board eval
            return (maxEval == Integer.MIN_VALUE && !moves.isEmpty()) ? board.evaluate(aiPlayer) : maxEval;
        } else { // Opponent's turn (Minimize score from AI's perspective)
            int minEval = Integer.MAX_VALUE;
            for (int[] move : moves) {
                int prevNextRow = board.getNextLocalBoardRow();
                int prevNextCol = board.getNextLocalBoardCol();
                int prevLocalWinner = board.localBoards[move[0]/3][move[1]/3].getWinner();
                int prevGlobalWinner = board.getGlobalWinner();

                if (board.makeMove(move[0], move[1], opponentPlayer)) {
                    int eval = minimax(board, depth - 1, alpha, beta, true); // AI's turn next

                    board.undoMove(move[0], move[1], prevNextRow, prevNextCol);
                    // Restore state if needed (see maximizing player block)

                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        break; // Alpha cut-off
                    }
                } else {
                    System.err.println("Minimax (Min): makeMove failed for " + move[0] + "," + move[1]);
                    // minEval = Math.min(minEval, Integer.MAX_VALUE - 1); // Penalize?
                }
            }
            return (minEval == Integer.MAX_VALUE && !moves.isEmpty()) ? board.evaluate(aiPlayer) : minEval;
        }
    }


    // Order moves: Prioritize wins, blocks, then heuristic score
    private List<int[]> orderMoves(GiantBoard board, List<int[]> moves, boolean maximizing) {
        List<MoveScore> scoredMoves = new ArrayList<>();
        int playerToSimulate = maximizing ? aiPlayer : opponentPlayer;
        int opponentToSimulate = maximizing ? opponentPlayer : aiPlayer;

        for (int[] move : moves) {
            // Create a copy for simulation to avoid altering the original board used in minimax level
            GiantBoard boardCopy = new GiantBoard(board);
            int score = 0;
            int priority = 0; // 0: normal, 1: block, 2: win

            if (boardCopy.makeMove(move[0], move[1], playerToSimulate)) {
                // Check for immediate win caused by this move
                if (boardCopy.getGlobalWinner() == playerToSimulate) {
                    priority = 2; // This move wins the game
                    score = maximizing ? Integer.MAX_VALUE - 10 : Integer.MIN_VALUE + 10; // Highest/lowest priority score
                } else {
                    // Check if this move blocks an immediate opponent win
                    // Simulate opponent's potential winning moves from the *original* state
                    boolean blocksOpponentWin = false;
                    GiantBoard checkBlockBoard = new GiantBoard(board); // Use original state to see opponent threats
                    List<int[]> opponentMoves = checkBlockBoard.getAvailableMoves(); // Opponent's potential moves *before* our move

                    for(int[] oppMove : opponentMoves) {
                        GiantBoard oppBoardCopy = new GiantBoard(checkBlockBoard);
                        if(oppBoardCopy.makeMove(oppMove[0], oppMove[1], opponentToSimulate)) {
                            if (oppBoardCopy.getGlobalWinner() == opponentToSimulate) {
                                // If opponent *could* have won with oppMove, does our current 'move' prevent that?
                                // The most direct block is playing in the same square oppMove would have.
                                if (move[0] == oppMove[0] && move[1] == oppMove[1]) {
                                    blocksOpponentWin = true;
                                    break;
                                }
                                // More complex blocking logic could be added (e.g., winning a needed local board)
                            }
                        }
                    }

                    if (blocksOpponentWin) {
                        priority = 1; // This move blocks an opponent win
                        // Give blocking a high score (slightly less than winning)
                        score = maximizing ? Integer.MAX_VALUE - 20 : Integer.MIN_VALUE + 20;
                    } else {
                        // If not a winning or blocking move, use standard heuristic
                        priority = 0;
                        // Evaluate the state *after* the move is made
                        score = boardCopy.evaluate(aiPlayer); // Always evaluate from AI's perspective
                    }
                }
                // No undo needed as we used a copy
                scoredMoves.add(new MoveScore(move, score, priority));
            } else {
                // Should not happen if moves list is valid
                System.err.println("OrderMoves: makeMove failed on copy for move: " + move[0] + "," + move[1]);
                // Score it badly
                score = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                scoredMoves.add(new MoveScore(move, score, 0));
            }
        }

        // Sort: Primary key = priority (desc), Secondary key = score (desc for max, asc for min)
        Collections.sort(scoredMoves, new Comparator<MoveScore>() {
            @Override
            public int compare(MoveScore ms1, MoveScore ms2) {
                // Higher priority first
                int priorityCompare = Integer.compare(ms2.priority, ms1.priority);
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                // If priorities are equal, sort by score
                if (maximizing) { // AI wants higher scores first
                    return Integer.compare(ms2.score, ms1.score);
                } else { // Opponent (simulated) wants lower scores (from AI perspective) first
                    return Integer.compare(ms1.score, ms2.score);
                }
            }
        });

        // Extract ordered moves
        List<int[]> ordered = new ArrayList<>();
        for (MoveScore ms : scoredMoves) {
            ordered.add(ms.move);
        }
        return ordered;
    }


    // Helper class for sorting moves
    private static class MoveScore {
        int[] move;
        int score;
        int priority; // 0=normal, 1=block, 2=win

        MoveScore(int[] move, int score, int priority) {
            this.move = move;
            this.score = score;
            this.priority = priority;
        }
    }

    // Custom exception for time limit
    private static class TimeLimitExceededException extends Exception {
        public TimeLimitExceededException(String message) {
            super(message);
        }
    }

}