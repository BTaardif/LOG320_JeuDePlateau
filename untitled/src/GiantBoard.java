// GiantBoard.java (Revised)
import java.util.ArrayList;
import java.util.List;

public class GiantBoard {
    public Board[][] localBoards = new Board[3][3];
    private int globalWinner = Board.EMPTY;
    private int nextLocalBoardRow = -1; // -1 means play anywhere
    private int nextLocalBoardCol = -1;

    // --- State history for robust undo ---
    private int previousLocalBoardWinner = Board.EMPTY; // Winner of the board *before* the last move was undone
    private int previousGlobalWinner = Board.EMPTY; // Global winner *before* the last move was undone


    public GiantBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                localBoards[i][j] = new Board();
            }
        }
        // Initial state: globalWinner = EMPTY, nextLocalBoard = -1
    }

    // Copy constructor for simulation (ensure deep copy)
    public GiantBoard(GiantBoard original) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.localBoards[i][j] = new Board(original.localBoards[i][j]);
            }
        }
        this.globalWinner = original.globalWinner;
        this.nextLocalBoardRow = original.nextLocalBoardRow;
        this.nextLocalBoardCol = original.nextLocalBoardCol;
        // History fields don't need deep copy as they are primitives overwritten on undo
        this.previousGlobalWinner = original.previousGlobalWinner;
        this.previousLocalBoardWinner = original.previousLocalBoardWinner;
    }


    // Update board state from server representation
    public void updateBoard(int[][] flatBoard) {
        if (flatBoard == null || flatBoard.length != 9 || flatBoard[0].length != 9) {
            System.err.println("Error: Invalid flatBoard dimensions received in updateBoard.");
            // Handle error state, maybe reset board?
            return;
        }
        for (int lbRow = 0; lbRow < 3; lbRow++) {
            for (int lbCol = 0; lbCol < 3; lbCol++) {
                // Create a fresh board, place moves, let Board handle recalcWinner
                localBoards[lbRow][lbCol] = new Board(); // Reset board
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int globalRow = lbRow * 3 + i;
                        int globalCol = lbCol * 3 + j;
                        int value = flatBoard[globalRow][globalCol];
                        if (value == Board.PLAYER_X || value == Board.PLAYER_O) {
                            // Use placeMove - it handles setting cell AND recalcWinner
                            // Ignore return value here, just setting initial state
                            localBoards[lbRow][lbCol].placeMove(i, j, value);
                        }
                    }
                }
                // Winner status is now correctly set within the local board by placeMove
            }
        }
        // After updating all local boards, recalculate the global status
        recalcGlobalWinner();
        // nextLocalBoardRow/Col needs to be set by the client based on opponent's last move
        // Resetting it here might be incorrect if called mid-game? Consider context.
        // For initial setup (cmd 1/2), this is fine. For updates after opponent move (cmd 3),
        // the client should call setNextBoardFromLastMove *after* this update.
        // Assuming this is primarily for initial setup or state sync:
        // nextLocalBoardRow = -1; // Or determine based on game rules if update implies constraint
        // nextLocalBoardCol = -1;
    }

    // Attempts to make a move
    public boolean makeMove(int globalRow, int globalCol, int player) {
        if (!isValidMove(globalRow, globalCol)) {
            // System.out.println("DEBUG: Invalid move attempted: " + globalRow + "," + globalCol + " NextBoard: " + nextLocalBoardRow + "," + nextLocalBoardCol);
            // System.out.println("DEBUG: Current board state:\n" + this);
            return false;
        }

        int localBoardRow = globalRow / 3;
        int localBoardCol = globalCol / 3;
        int cellRow = globalRow % 3;
        int cellCol = globalCol % 3;

        Board targetBoard = localBoards[localBoardRow][localBoardCol];

        // Store state for potential undo
        // Store BEFORE making the move
        previousLocalBoardWinner = targetBoard.getWinner();
        previousGlobalWinner = this.globalWinner;

        boolean moved = targetBoard.placeMove(cellRow, cellCol, player);

        if (moved) {
            // Update global winner based on potential change in local board winner
            recalcGlobalWinner();

            // Determine next board constraint
            Board nextTargetLocalBoard = localBoards[cellRow][cellCol];
            if (nextTargetLocalBoard.isTerminal()) {
                nextLocalBoardRow = -1; // Play anywhere
                nextLocalBoardCol = -1;
            } else {
                nextLocalBoardRow = cellRow;
                nextLocalBoardCol = cellCol;
            }
            return true;
        } else {
            // Should not happen if isValidMove is correct, but log if it does
            System.err.println("Error: makeMove failed for a supposedly valid move: " + globalRow + "," + globalCol);
            // Restore pre-move state just in case something partial happened (though Board.placeMove should be atomic)
            this.globalWinner = previousGlobalWinner;
            // Undoing the potential partial move in local board might be needed if placeMove implementation changes
            // targetBoard.undoMove(cellRow, cellCol); // But current Board.placeMove doesn't modify on failure
            return false;
        }
    }


    // Undoes the last move
    public void undoMove(int globalRow, int globalCol, int prevNextRow, int prevNextCol) {
        if (globalRow < 0 || globalRow > 8 || globalCol < 0 || globalCol > 8) {
            System.err.println("UndoMove Error: Invalid global coordinates " + globalRow + "," + globalCol);
            return; // Bounds check
        }

        int localRow = globalRow / 3;
        int localCol = globalCol / 3;
        int cellRow = globalRow % 3;
        int cellCol = globalCol % 3;

        if (localRow >= 0 && localRow < 3 && localCol >= 0 && localCol < 3) {
            Board targetBoard = localBoards[localRow][localCol];

            // Undo the move on the local board. Board.undoMove calls recalcWinner internally.
            targetBoard.undoMove(cellRow, cellCol);

            // *** Crucial: Restore the winner state of the local board AS IT WAS BEFORE the move was made ***
            // This handles cases where undoing doesn't perfectly revert state via recalc alone (e.g., complex draw scenarios)
            // We achieve this implicitly because Board.undoMove->recalcWinner should correctly calculate the state
            // resulting from removing the piece. If Board.recalcWinner is perfect, explicit restoration isn't needed.
            // Let's trust Board.recalcWinner for now.

            // Restore the next board constraint
            nextLocalBoardRow = prevNextRow;
            nextLocalBoardCol = prevNextCol;

            // Restore the global winner state AS IT WAS BEFORE the move was made
            this.globalWinner = previousGlobalWinner;

            // We might need to recalculate global winner if the local board's status *truly* changed state upon undo
            // E.g. undoing a move caused a local win/draw to revert to ongoing.
            // Recalculating is safer.
            recalcGlobalWinner();

        } else {
            System.err.println("UndoMove Error: Invalid local board coordinates derived from global " + globalRow + "," + globalCol);
        }
    }

    // Checks if a move is valid according to game rules
    public boolean isValidMove(int globalRow, int globalCol) {
        if (globalRow < 0 || globalRow > 8 || globalCol < 0 || globalCol > 8) return false; // Basic bounds
        if (globalWinner != Board.EMPTY) return false; // Game already over

        int localBoardRow = globalRow / 3;
        int localBoardCol = globalCol / 3;
        int cellRow = globalRow % 3;
        int cellCol = globalCol % 3;

        Board targetBoard = localBoards[localBoardRow][localBoardCol];

        // Check if the target local board is already finished
        if (targetBoard.isTerminal()) return false;

        // Check if the cell within the local board is empty
        if (!targetBoard.isCellEmpty(cellRow, cellCol)) return false;

        // Check if the move is in the required local board (or if play anywhere is allowed)
        if (nextLocalBoardRow != -1) { // Must play in a specific board
            if (localBoardRow != nextLocalBoardRow || localBoardCol != nextLocalBoardCol) {
                return false; // Tried to play in the wrong board
            }
        }

        return true; // All checks passed
    }

    // Recalculates the global winner based on local board winners
    private void recalcGlobalWinner() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            // Check only if first cell is a player win (ignore EMPTY/DRAW for win lines)
            int firstWinner = localBoards[i][0].getWinner();
            if (firstWinner != Board.EMPTY && firstWinner != Board.DRAW) {
                if (firstWinner == localBoards[i][1].getWinner() && firstWinner == localBoards[i][2].getWinner()) {
                    globalWinner = firstWinner;
                    return;
                }
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            int firstWinner = localBoards[0][i].getWinner();
            if (firstWinner != Board.EMPTY && firstWinner != Board.DRAW) {
                if (firstWinner == localBoards[1][i].getWinner() && firstWinner == localBoards[2][i].getWinner()) {
                    globalWinner = firstWinner;
                    return;
                }
            }
        }
        // Check diagonals
        int diag1Winner = localBoards[0][0].getWinner();
        if (diag1Winner != Board.EMPTY && diag1Winner != Board.DRAW) {
            if (diag1Winner == localBoards[1][1].getWinner() && diag1Winner == localBoards[2][2].getWinner()) {
                globalWinner = diag1Winner;
                return;
            }
        }
        int diag2Winner = localBoards[0][2].getWinner();
        if (diag2Winner != Board.EMPTY && diag2Winner != Board.DRAW) {
            if (diag2Winner == localBoards[1][1].getWinner() && diag2Winner == localBoards[2][0].getWinner()) {
                globalWinner = diag2Winner;
                return;
            }
        }

        // No player won, check for draw (all local boards are terminal)
        boolean allBoardsTerminal = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (!localBoards[i][j].isTerminal()) {
                    allBoardsTerminal = false;
                    break;
                }
            }
            if (!allBoardsTerminal) break;
        }

        if (allBoardsTerminal) {
            globalWinner = Board.DRAW; // All boards finished, no winner = Draw
            return;
        }

        // If not won and not draw, game is ongoing
        globalWinner = Board.EMPTY;
    }


    public int getGlobalWinner() {
        // Consider recalculating here if you suspect state issues, but should be ok if make/undo call it
        // recalcGlobalWinner();
        return globalWinner;
    }

    public boolean isGameOver() {
        // Recalculate just to be absolutely sure before checking
        // This might be slightly inefficient but safer
        recalcGlobalWinner();
        // Game is over if there's a winner/draw OR no moves left
        return globalWinner != Board.EMPTY || getAvailableMoves().isEmpty();
    }

    // Generates all valid moves for the current player
    public List<int[]> getAvailableMoves() {
        List<int[]> moves = new ArrayList<>();
        if (globalWinner != Board.EMPTY) {
            return moves; // Game finished
        }

        if (nextLocalBoardRow == -1) {
            // Play anywhere is allowed
            for (int br = 0; br < 3; br++) {
                for (int bc = 0; bc < 3; bc++) {
                    Board currentLocalBoard = localBoards[br][bc];
                    if (!currentLocalBoard.isTerminal()) {
                        List<int[]> localMoves = currentLocalBoard.getAvailableMoves();
                        for (int[] m : localMoves) {
                            // Convert local coords (m[0], m[1]) to global
                            moves.add(new int[]{br * 3 + m[0], bc * 3 + m[1]});
                        }
                    }
                }
            }
        } else {
            // Must play in a specific board
            if (nextLocalBoardRow >= 0 && nextLocalBoardRow < 3 && nextLocalBoardCol >= 0 && nextLocalBoardCol < 3) {
                Board targetLocalBoard = localBoards[nextLocalBoardRow][nextLocalBoardCol];
                // It's possible the target board *just* became terminal due to the opponent's move
                // If it IS terminal, the player should be allowed to play anywhere.
                if (targetLocalBoard.isTerminal()) {
                    // Fallback to play anywhere
                    // System.out.println("DEBUG: Target board ("+nextLocalBoardRow+","+nextLocalBoardCol+") is terminal, falling back to play anywhere.");
                    for (int br = 0; br < 3; br++) {
                        for (int bc = 0; bc < 3; bc++) {
                            Board currentLocalBoard = localBoards[br][bc];
                            if (!currentLocalBoard.isTerminal()) {
                                List<int[]> localMoves = currentLocalBoard.getAvailableMoves();
                                for (int[] m : localMoves) {
                                    moves.add(new int[]{br * 3 + m[0], bc * 3 + m[1]});
                                }
                            }
                        }
                    }
                } else {
                    // Play in the target board
                    List<int[]> localMoves = targetLocalBoard.getAvailableMoves();
                    for (int[] m : localMoves) {
                        moves.add(new int[]{nextLocalBoardRow * 3 + m[0], nextLocalBoardCol * 3 + m[1]});
                    }
                }
            } else {
                // This case should ideally not happen if nextLocalBoardRow/Col are managed correctly
                System.err.println("Warning: Invalid nextLocalBoard coords ("+nextLocalBoardRow+","+nextLocalBoardCol+"). Defaulting to play anywhere.");
                for (int br = 0; br < 3; br++) { // Play anywhere logic duplicated
                    for (int bc = 0; bc < 3; bc++) {
                        Board currentLocalBoard = localBoards[br][bc];
                        if (!currentLocalBoard.isTerminal()) {
                            List<int[]> localMoves = currentLocalBoard.getAvailableMoves();
                            for (int[] m : localMoves) {
                                moves.add(new int[]{br * 3 + m[0], bc * 3 + m[1]});
                            }
                        }
                    }
                }
            }
        }

        // Debugging: Log if no moves are found but game isn't over
        // if (moves.isEmpty() && globalWinner == Board.EMPTY) {
        //     System.out.println("*******************************************");
        //     System.out.println("WARNING: getAvailableMoves is empty but game not over!");
        //     System.out.println("Next Board: " + nextLocalBoardRow + "," + nextLocalBoardCol);
        //     System.out.println("Board state:\n" + this);
        //     System.out.println("*******************************************");
        // }
        return moves;
    }


    // Sets the next required board based on the opponent's last move's local coords
    public void setNextBoardFromLastMove(int opponentMoveLocalRow, int opponentMoveLocalCol) {
        if (opponentMoveLocalRow < 0 || opponentMoveLocalRow > 2 || opponentMoveLocalCol < 0 || opponentMoveLocalCol > 2) {
            // Invalid coords likely means play anywhere (e.g., first move)
            nextLocalBoardRow = -1;
            nextLocalBoardCol = -1;
        } else {
            Board target = localBoards[opponentMoveLocalRow][opponentMoveLocalCol];
            if (target.isTerminal()) { // If the target board is finished
                nextLocalBoardRow = -1; // Play anywhere
                nextLocalBoardCol = -1;
            } else {
                nextLocalBoardRow = opponentMoveLocalRow; // Must play in this board
                nextLocalBoardCol = opponentMoveLocalCol;
            }
        }
    }

    // --- Evaluation ---
    public int evaluate(int player) {
        // Recalculate winner just in case state is messy during search
        recalcGlobalWinner();
        int opponent = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;

        // 1. Check for immediate win/loss/draw globally
        if (globalWinner == player) return 100000; // Strong weight for global win
        if (globalWinner == opponent) return -100000; // Strong weight for global loss
        if (globalWinner == Board.DRAW) return 0;      // Neutral for draw

        int totalScore = 0;

        // 2. Evaluate global board based on local winners (heuristic)
        totalScore += evaluateGlobalBoardHeuristic(player) * 100; // Weight global patterns

        // 3. Evaluate individual local boards
        int localBoardTotalScore = 0;
        int playerLocalWins = 0;
        int opponentLocalWins = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int localEval = localBoards[i][j].evaluate(player);
                localBoardTotalScore += localEval; // Sum local evaluations

                // Bonus for controlling center local board
                if (i == 1 && j == 1 && localEval > 0) {
                    totalScore += 10; // Small bonus for center control heuristic
                }

                // Count local wins for heuristic
                int localWinner = localBoards[i][j].getWinner();
                if (localWinner == player) playerLocalWins++;
                else if (localWinner == opponent) opponentLocalWins++;
            }
        }
        totalScore += localBoardTotalScore; // Add sum of local scores

        // 4. Add bonus for number of local boards won vs lost
        totalScore += (playerLocalWins - opponentLocalWins) * 50; // Bonus for each net local win

        // 5. Consider the next board constraint (optional but potentially strong)
        // If nextLocalBoardRow != -1, evaluate the state of that specific board
        // Add bonus if it's advantageous for player, penalty if advantageous for opponent.
        // This requires looking ahead slightly within the evaluation.
        // Example: if(nextLocalBoardRow != -1) { totalScore += localBoards[nextLocalBoardRow][nextLocalBoardCol].evaluate(player) * 0.5; }


        return totalScore;
    }

    // Heuristic evaluation of the global 3x3 grid based on local winners
    private int evaluateGlobalBoardHeuristic(int player) {
        int score = 0;
        int opponent = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;
        int[][] winnersGrid = new int[3][3];
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                winnersGrid[i][j] = localBoards[i][j].getWinner();
            }
        }

        // Evaluate lines based on who won the local boards
        score += evaluateGlobalLine(winnersGrid, 0, 0, 0, 1, 0, 2, player); // Rows
        score += evaluateGlobalLine(winnersGrid, 1, 0, 1, 1, 1, 2, player);
        score += evaluateGlobalLine(winnersGrid, 2, 0, 2, 1, 2, 2, player);
        score += evaluateGlobalLine(winnersGrid, 0, 0, 1, 0, 2, 0, player); // Cols
        score += evaluateGlobalLine(winnersGrid, 0, 1, 1, 1, 2, 1, player);
        score += evaluateGlobalLine(winnersGrid, 0, 2, 1, 2, 2, 2, player);
        score += evaluateGlobalLine(winnersGrid, 0, 0, 1, 1, 2, 2, player); // Diags
        score += evaluateGlobalLine(winnersGrid, 0, 2, 1, 1, 2, 0, player);
        return score;
    }

    // Helper for evaluateGlobalBoardHeuristic
    private int evaluateGlobalLine(int[][] winnersGrid, int r1, int c1, int r2, int c2, int r3, int c3, int player) {
        int score = 0;
        int opponent = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;

        int playerCount = 0;
        int opponentCount = 0;
        // Check cell 1
        if (winnersGrid[r1][c1] == player) playerCount++;
        else if (winnersGrid[r1][c1] == opponent) opponentCount++;
        // Check cell 2
        if (winnersGrid[r2][c2] == player) playerCount++;
        else if (winnersGrid[r2][c2] == opponent) opponentCount++;
        // Check cell 3
        if (winnersGrid[r3][c3] == player) playerCount++;
        else if (winnersGrid[r3][c3] == opponent) opponentCount++;

        // Give score based on line composition (ignoring DRAW/EMPTY unless checking for blocks)
        if (playerCount == 3) score = 1000; // Should be caught by globalWin, but good heuristic
        else if (playerCount == 2 && opponentCount == 0) score = 100; // Two local wins in a line
        else if (playerCount == 1 && opponentCount == 0) score = 10;  // One local win, potential
        else if (opponentCount == 3) score = -1000;
        else if (opponentCount == 2 && playerCount == 0) score = -100;
        else if (opponentCount == 1 && playerCount == 0) score = -10;
        return score;
    }


    // Getters
    public int getNextLocalBoardRow() { return nextLocalBoardRow; }
    public int getNextLocalBoardCol() { return nextLocalBoardCol; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Giant Board (Next: " + (nextLocalBoardRow == -1 ? "Any" : nextLocalBoardRow + "," + nextLocalBoardCol) + " Winner: " + globalWinner + ")\n");
        for (int big_r = 0; big_r < 3; big_r++) {
            for (int r = 0; r < 3; r++) {
                for (int big_c = 0; big_c < 3; big_c++) {
                    sb.append(localBoards[big_r][big_c].toString().split("\n")[r]); // Append row 'r' of local board
                    sb.append(big_c < 2 ? " | " : "");
                }
                sb.append("\n");
            }
            if (big_r < 2) sb.append("---------------------\n");
        }
        return sb.toString();
    }
}