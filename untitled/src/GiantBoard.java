
// Represents the main Ultimate Tic-Tac-Toe board
public class GiantBoard {

    private LocalBoard[][] localBoards; // 3x3 grid of LocalBoards
    private LocalBoard globalBoard;    // Represents the overall 3x3 win state
    private int nextLocalBoardRow = -1; // Row index (0-2) of the required next local board (-1 means any)
    private int nextLocalBoardCol = -1; // Col index (0-2) of the required next local board (-1 means any)


    // Constructor
    public GiantBoard() {
        localBoards = new LocalBoard[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                localBoards[i][j] = new LocalBoard();
            }
        }
        // Global board tracks wins in local boards. Use EMPTY initially.
        // We'll update its "marks" based on local board wins.
        globalBoard = new LocalBoard();
        nextLocalBoardRow = -1; // Player 1 (X) can start anywhere
        nextLocalBoardCol = -1;
    }

    // Copy Constructor
    public GiantBoard(GiantBoard other) {
        this.localBoards = new LocalBoard[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.localBoards[i][j] = new LocalBoard(other.localBoards[i][j]);
            }
        }
        this.globalBoard = new LocalBoard(other.globalBoard);
        this.nextLocalBoardRow = other.nextLocalBoardRow;
        this.nextLocalBoardCol = other.nextLocalBoardCol;
    }

     // Getters
    public LocalBoard getLocalBoard(int r, int c) {
        if (r >= 0 && r < 3 && c >= 0 && c < 3) {
            return localBoards[r][c];
        }
        return null; // Or throw exception
    }

    public LocalBoard getGlobalBoard() {
        return globalBoard;
    }

    public int getNextLocalBoardRow() {
        return nextLocalBoardRow;
    }

     public int getNextLocalBoardCol() {
        return nextLocalBoardCol;
    }

    // Check if the overall game has been won/drawn
    public LocalBoard.BoardState getOverallGameState() {
        return globalBoard.getState();
    }


    // --- Core Game Logic ---

    // Place a mark according to Ultimate Tic-Tac-Toe rules
    public boolean play(Move move, Mark mark) {
        int gR = move.getGlobalRow();
        int gC = move.getGlobalCol();
        int lR = move.getLocalRow();
        int lC = move.getLocalCol();

        // 1. Check if the move is in the allowed local board
        if (!isMoveInRequiredLocalBoard(gR, gC)) {
            System.err.println("Error: Move (" + gR + "," + gC + ") not in required board (" + nextLocalBoardRow + "," + nextLocalBoardCol + ")");
            return false; // Not in the right local board
        }

        // 2. Play the move on the specified local board
        LocalBoard targetBoard = localBoards[gR][gC];
        if (!targetBoard.play(lR, lC, mark)) {
             System.err.println("Error: Invalid local move (" + lR + "," + lC + ") on board (" + gR + "," + gC + ")");
            return false; // Invalid move within the local board (e.g., cell taken, board finished)
        }

        // 3. Update global board state if the local board was just won/drawn
        updateGlobalBoard(gR, gC, targetBoard.getState());

        // 4. Determine the next required local board
        updateNextLocalBoard(lR, lC); // The *local* coordinates determine the next board

        return true;
    }

    // Helper to check if the move's global coordinates match the required board
    private boolean isMoveInRequiredLocalBoard(int globalRow, int globalCol) {
        if (nextLocalBoardRow == -1) {
            return true; // Any board is allowed
        }
        return globalRow == nextLocalBoardRow && globalCol == nextLocalBoardCol;
    }


    // Update the global board representation after a local board is finished
    private void updateGlobalBoard(int gR, int gC, LocalBoard.BoardState localState) {
        Mark globalMark = Mark.EMPTY;
        if (localState == LocalBoard.BoardState.X_WON) {
            globalMark = Mark.X;
        } else if (localState == LocalBoard.BoardState.O_WON) {
            globalMark = Mark.O;
        } else if (localState == LocalBoard.BoardState.DRAW) {
            // How to represent a draw on the global board?
            // Option 1: Use a special marker (if Mark enum allows)
            // Option 2: Leave it EMPTY but know it's unplayable (handled by checking local state)
            // Option 3: Treat draw as blocking, play EMPTY (simplest for win check)
            globalMark = Mark.EMPTY; // Simplest for win check logic reuse
        }

        // Only update if the local board finished and global board cell is empty
        if (localState != LocalBoard.BoardState.ONGOING && globalBoard.getMark(gR, gC) == Mark.EMPTY) {
            // Use the LocalBoard's play method to also trigger its win check logic
            globalBoard.play(gR, gC, globalMark);
             // Force state update if play didn't (e.g. playing EMPTY for a draw)
             if (globalMark == Mark.EMPTY && localState == LocalBoard.BoardState.DRAW) {
                 globalBoard.updateState(Mark.EMPTY); // Pass dummy player
             }
        }
    }

    // Determine the next required local board based on the last local move
    private void updateNextLocalBoard(int localRow, int localCol) {
        LocalBoard nextBoard = localBoards[localRow][localCol]; // Potential next board

        // If the board determined by the move (localRow, localCol) is finished,
        // the next player can play anywhere that's not finished.
        if (nextBoard.getState() != LocalBoard.BoardState.ONGOING) {
            nextLocalBoardRow = -1;
            nextLocalBoardCol = -1;
        } else {
            nextLocalBoardRow = localRow;
            nextLocalBoardCol = localCol;
        }
    }

    // --- Move Generation and Evaluation ---

    // Generates all valid moves for the current player
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        if (getOverallGameState() != LocalBoard.BoardState.ONGOING) {
            return possibleMoves; // Game over
        }

        if (nextLocalBoardRow != -1) {
            // Forced move into a specific local board
            LocalBoard targetBoard = localBoards[nextLocalBoardRow][nextLocalBoardCol];
            if (targetBoard.getState() == LocalBoard.BoardState.ONGOING) {
                 possibleMoves.addAll(targetBoard.getPossibleLocalMoves(nextLocalBoardRow, nextLocalBoardCol));
            } else {
                 // This case should theoretically not happen if updateNextLocalBoard is correct
                 // If it does, it means we were sent to a finished board -> play anywhere
                 addAllMovesFromOpenBoards(possibleMoves);
            }
        } else {
            // Play allowed in any open local board
            addAllMovesFromOpenBoards(possibleMoves);
        }

        return possibleMoves;
    }

     // Helper to add all valid moves from all non-finished local boards
    private void addAllMovesFromOpenBoards(ArrayList<Move> moves) {
        for (int gR = 0; gR < 3; gR++) {
            for (int gC = 0; gC < 3; gC++) {
                if (localBoards[gR][gC].getState() == LocalBoard.BoardState.ONGOING) {
                    moves.addAll(localBoards[gR][gC].getPossibleLocalMoves(gR, gC));
                }
            }
        }
    }

    // Evaluate the overall board state for the AI (needs refinement)
    public int evaluate(Mark playerPerspective) {
         // 1. Check for immediate global win/loss
        int globalScore = globalBoard.evaluate(playerPerspective); // Uses the local board eval on global state
        if (globalScore == 100 || globalScore == -100) {
            return globalScore * 10; // Make global win more important
        }

        // 2. Sum weighted evaluations of local boards
        int localScores = 0;
        for (int gR = 0; gR < 3; gR++) {
            for (int gC = 0; gC < 3; gC++) {
                // Weight center board higher? Corner boards?
                int weight = 1;
                 if (gR == 1 && gC == 1) weight = 2; // Example: weight center board more

                localScores += localBoards[gR][gC].evaluate(playerPerspective) * weight;
            }
        }

        // 3. Add heuristics (e.g., number of potential global lines, etc.) - TO BE ADDED

        return globalScore + localScores; // Combine scores
    }

    // --- Utility ---

    @Override
    public String toString() {
         // Basic text representation of the ultimate board
        StringBuilder sb = new StringBuilder();
        for (int bigRow = 0; bigRow < 3; bigRow++) {
             if (bigRow > 0) sb.append("---+---+---\n");
            for (int smallRow = 0; smallRow < 3; smallRow++) {
                for (int bigCol = 0; bigCol < 3; bigCol++) {
                     if (bigCol > 0) sb.append("|");
                     LocalBoard lb = localBoards[bigRow][bigCol];
                    for (int smallCol = 0; smallCol < 3; smallCol++) {
                        Mark m = lb.getMark(smallRow, smallCol);
                        sb.append(m == Mark.EMPTY ? "." : m.toString());
                    }
                }
                sb.append("\n");
            }
        }
         sb.append("Global State: ").append(globalBoard.getState()).append("\n");
         sb.append("Next Board: (").append(nextLocalBoardRow).append(",").append(nextLocalBoardCol).append(")\n");
        return sb.toString();
    }
}    