// GiantBoard.java
// Represents the 9x9 main board, containing 9 Board objects.
import java.util.ArrayList;
import java.util.List;

public class GiantBoard {
    private Board[][] localBoards = new Board[3][3];
    private int globalWinner = Board.EMPTY;
    private int nextLocalBoardRow = -1; // -1 means play anywhere
    private int nextLocalBoardCol = -1;

    public GiantBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                localBoards[i][j] = new Board();
            }
        }
    }

    // Copy constructor for simulation
    public GiantBoard(GiantBoard original) {
        this.globalWinner = original.globalWinner;
        this.nextLocalBoardRow = original.nextLocalBoardRow;
        this.nextLocalBoardCol = original.nextLocalBoardCol;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.localBoards[i][j] = new Board(original.localBoards[i][j]);
            }
        }
    }

    // Update the board state from the server's flat array
    public void updateBoard(int[][] flatBoard) {
        for (int globalRow = 0; globalRow < 3; globalRow++) {
            for (int globalCol = 0; globalCol < 3; globalCol++) {
                // Check if the local board has a winner based on its state
                if (localBoards[globalRow][globalCol].getWinner() == Board.EMPTY && !localBoards[globalRow][globalCol].isTerminal()) {
                    for (int localRow = 0; localRow < 3; localRow++) {
                        for (int localCol = 0; localCol < 3; localCol++) {
                            int flatRow = globalRow * 3 + localRow;
                            int flatCol = globalCol * 3 + localCol;
                            int currentVal = localBoards[globalRow][globalCol].getCell(localRow, localCol);
                            int newVal = flatBoard[flatRow][flatCol];
                            if (currentVal == Board.EMPTY && newVal != Board.EMPTY) {
                                localBoards[globalRow][globalCol].placeMove(localRow, localCol, newVal);
                            } else if (currentVal != Board.EMPTY && newVal == Board.EMPTY){
                                // This case should ideally not happen if the server sends full state
                                // but good for robustness or potential resets. Consider logging.
                                // For now, we assume server state is correct and board updates reflect it.
                                // If implementing local resets/undo, clear the cell here.
                            } else if (currentVal != newVal) {
                                // Discrepancy detected, server state takes precedence
                                localBoards[globalRow][globalCol].placeMove(localRow, localCol, newVal); // Force update if possible
                            }
                        }
                    }
                    localBoards[globalRow][globalCol].getWinner(); // Recalculate winner status after update
                }
            }
        }
        checkGlobalWin(); // Update global winner status
    }


    // Make a move specified by global coordinates (0-8)
    public boolean makeMove(int globalRow, int globalCol, int player) {
        int localBoardRow = globalRow / 3;
        int localBoardCol = globalCol / 3;
        int localRow = globalRow % 3;
        int localCol = globalCol % 3;

        Board targetBoard = localBoards[localBoardRow][localBoardCol];

        // Check if the move is valid based on rules [cite: 15, 16, 18, 19]
        if (!isValidMove(globalRow, globalCol)) {
            System.err.println("Invalid move attempted at Global (" + globalRow + "," + globalCol + "). Target board playable: " + isPlayableBoard(localBoardRow, localBoardCol));
            return false;
        }


        if (targetBoard.placeMove(localRow, localCol, player)) {
            checkGlobalWin(); // Check if this move wins the global board

            // Determine the next required local board [cite: 15, 16]
            Board nextBoard = localBoards[localRow][localCol];
            if (nextBoard.isTerminal()) { // Target board is won or full [cite: 18, 19]
                nextLocalBoardRow = -1; // Player can play anywhere open
                nextLocalBoardCol = -1;
            } else {
                nextLocalBoardRow = localRow;
                nextLocalBoardCol = localCol;
            }
            return true;
        }
        return false;
    }

    // Check if a specific global cell is a valid move according to rules
    public boolean isValidMove(int globalRow, int globalCol) {
        if (globalRow < 0 || globalRow > 8 || globalCol < 0 || globalCol > 8 || globalWinner != Board.EMPTY) {
            return false; // Out of bounds or game already won
        }

        int localBoardRow = globalRow / 3;
        int localBoardCol = globalCol / 3;
        int localRow = globalRow % 3;
        int localCol = globalCol % 3;

        Board targetBoard = localBoards[localBoardRow][localBoardCol];

        // Check if the target local board is already won or full
        if (targetBoard.isTerminal()) {
            return false;
        }

        // Check if the cell itself is empty
        if (!targetBoard.isCellEmpty(localRow, localCol)) {
            return false;
        }

        // Check if the move is in the required local board [cite: 15, 16, 19]
        if (nextLocalBoardRow == -1) {
            return true; // Can play anywhere open
        } else {
            return localBoardRow == nextLocalBoardRow && localBoardCol == nextLocalBoardCol;
        }
    }

    // Helper to check if a specific local board is playable (not won or full)
    private boolean isPlayableBoard(int boardRow, int boardCol) {
        return !localBoards[boardRow][boardCol].isTerminal();
    }


    // Get all valid moves for the current player
    public List<int[]> getAvailableMoves() {
        List<int[]> moves = new ArrayList<>();
        if (globalWinner != Board.EMPTY) return moves; // No moves if game is over

        if (nextLocalBoardRow == -1) {
            // Play anywhere is allowed [cite: 19]
            for (int br = 0; br < 3; br++) {
                for (int bc = 0; bc < 3; bc++) {
                    if (isPlayableBoard(br, bc)) {
                        Board board = localBoards[br][bc];
                        List<int[]> localMoves = board.getAvailableMoves();
                        for(int[] localMove : localMoves) {
                            moves.add(new int[]{br * 3 + localMove[0], bc * 3 + localMove[1]});
                        }
                    }
                }
            }
        } else {
            // Must play in the specified board [cite: 15, 16]
            if (isPlayableBoard(nextLocalBoardRow, nextLocalBoardCol)) {
                Board board = localBoards[nextLocalBoardRow][nextLocalBoardCol];
                List<int[]> localMoves = board.getAvailableMoves();
                for(int[] localMove : localMoves) {
                    moves.add(new int[]{nextLocalBoardRow * 3 + localMove[0], nextLocalBoardCol * 3 + localMove[1]});
                }
            } else {
                // This case implies the required board became terminal *after* the opponent's move
                // which directed play here. The rule says play anywhere open. [cite: 19]
                // So, we fall back to the "play anywhere" logic.
                // Note: The `makeMove` logic already handles setting nextLocalBoardRow/Col to -1
                // if the *target* of the *next* move is terminal. This handles the case where
                // the required board is *already* terminal when getAvailableMoves is called.
                for (int br = 0; br < 3; br++) {
                    for (int bc = 0; bc < 3; bc++) {
                        if (isPlayableBoard(br, bc)) {
                            Board board = localBoards[br][bc];
                            List<int[]> localMoves = board.getAvailableMoves();
                            for(int[] localMove : localMoves) {
                                moves.add(new int[]{br * 3 + localMove[0], bc * 3 + localMove[1]});
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }


    // Check for a win condition on the global board [cite: 11]
    private void checkGlobalWin() {
        if (globalWinner != Board.EMPTY) return;

        int[][] globalCells = new int[3][3];
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                globalCells[i][j] = localBoards[i][j].getWinner();
            }
        }

        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (globalCells[i][0] != Board.EMPTY && globalCells[i][0] == globalCells[i][1] && globalCells[i][1] == globalCells[i][2]) {
                globalWinner = globalCells[i][0];
                return;
            }
            if (globalCells[0][i] != Board.EMPTY && globalCells[0][i] == globalCells[1][i] && globalCells[1][i] == globalCells[2][i]) {
                globalWinner = globalCells[0][i];
                return;
            }
        }

        // Check diagonals
        if (globalCells[0][0] != Board.EMPTY && globalCells[0][0] == globalCells[1][1] && globalCells[1][1] == globalCells[2][2]) {
            globalWinner = globalCells[0][0];
            return;
        }
        if (globalCells[0][2] != Board.EMPTY && globalCells[0][2] == globalCells[1][1] && globalCells[1][1] == globalCells[2][0]) {
            globalWinner = globalCells[0][2];
            return;
        }

        // Check for global draw (all local boards terminal, no global winner)
        boolean allTerminal = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (!localBoards[i][j].isTerminal()) {
                    allTerminal = false;
                    break;
                }
            }
            if(!allTerminal) break;
        }
        if(allTerminal && globalWinner == Board.EMPTY) {
            // Consider this a draw state for evaluation, maybe assign a specific DRAW indicator if needed
            // For now, no explicit globalWinner change, relies on evaluation recognizing stalemate.
        }

    }

    public int getGlobalWinner() {
        checkGlobalWin(); // Ensure status is up-to-date
        return globalWinner;
    }

    public boolean isGameOver() {
        return getGlobalWinner() != Board.EMPTY || getAvailableMoves().isEmpty();
    }


    // Evaluate the entire giant board state [cite: 46]
    // Positive favors X, negative favors O
    public int evaluate(int player) {
        checkGlobalWin();
        int opponent = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;

        if (globalWinner == player) {
            return 10000; // Very high score for winning globally
        }
        if (globalWinner == opponent) {
            return -10000; // Very low score for opponent winning globally
        }
        if (isGameOver() && globalWinner == Board.EMPTY) {
            return 0; // Draw
        }


        int totalScore = 0;

        // Evaluate based on global board potential (similar to local board eval but using local winners)
        int[][] globalCells = new int[3][3];
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                globalCells[i][j] = localBoards[i][j].getWinner();
            }
        }
        // Factor of 100 emphasizes global board wins over local heuristics
        totalScore += evaluateGlobalLine(globalCells, 0, 0, 0, 1, 0, 2, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 1, 0, 1, 1, 1, 2, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 2, 0, 2, 1, 2, 2, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 0, 0, 1, 0, 2, 0, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 0, 1, 1, 1, 2, 1, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 0, 2, 1, 2, 2, 2, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 0, 0, 1, 1, 2, 2, player) * 100;
        totalScore += evaluateGlobalLine(globalCells, 0, 2, 1, 1, 2, 0, player) * 100;


        // Add scores from individual local boards
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                totalScore += localBoards[i][j].evaluate(player);
            }
        }

        return totalScore;
    }

    // Helper for evaluating a line on the global board based on local winners
    private int evaluateGlobalLine(int[][] globalCells, int r1, int c1, int r2, int c2, int r3, int c3, int player) {
        int score = 0;
        int opponent = (player == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;

        int cell1 = globalCells[r1][c1];
        int cell2 = globalCells[r2][c2];
        int cell3 = globalCells[r3][c3];

        int playerCount = 0;
        int opponentCount = 0;
        if (cell1 == player) playerCount++; else if (cell1 == opponent) opponentCount++;
        if (cell2 == player) playerCount++; else if (cell2 == opponent) opponentCount++;
        if (cell3 == player) playerCount++; else if (cell3 == opponent) opponentCount++;

        // Assign score based on line state (higher magnitude than local)
        if (playerCount == 3) {
            score = 1000; // Should be caught by getGlobalWinner
        } else if (playerCount == 2 && opponentCount == 0) {
            score = 100; // Two local boards won, potential global win
        } else if (playerCount == 1 && opponentCount == 0) {
            score = 10;  // One local board won
        } else if (opponentCount == 3) {
            score = -1000; // Should be caught by getGlobalWinner
        } else if (opponentCount == 2 && playerCount == 0) {
            score = -100; // Opponent won two local boards
        } else if (opponentCount == 1 && playerCount == 0) {
            score = -10; // Opponent won one local board
        }

        return score;
    }

    // Sets the next board based on the last move's local coordinates
    public void setNextBoardFromLastMove(int localRow, int localCol) {
        if (localRow < 0 || localRow > 2 || localCol < 0 || localCol > 2) {
            // Invalid coordinates, implies start of game or error, allow play anywhere
            this.nextLocalBoardRow = -1;
            this.nextLocalBoardCol = -1;
        } else {
            Board target = localBoards[localRow][localCol];
            if (target.isTerminal()) {
                this.nextLocalBoardRow = -1;
                this.nextLocalBoardCol = -1;
            } else {
                this.nextLocalBoardRow = localRow;
                this.nextLocalBoardCol = localCol;
            }
        }
        System.out.println("Next board to play set to: " + (nextLocalBoardRow == -1 ? "Any" : nextLocalBoardRow + "," + nextLocalBoardCol));
    }

    public int getNextLocalBoardRow() { return nextLocalBoardRow; }
    public int getNextLocalBoardCol() { return nextLocalBoardCol; }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Giant Board (Next: " +
                (nextLocalBoardRow == -1 ? "Any" : nextLocalBoardRow + "," + nextLocalBoardCol) +
                " Winner: " + globalWinner + ")\n");
        for (int big_r = 0; big_r < 3; big_r++) {
            for (int r = 0; r < 3; r++) {
                for (int big_c = 0; big_c < 3; big_c++) {
                    for (int c = 0; c < 3; c++) {
                        int val = localBoards[big_r][big_c].getCell(r, c);
                        switch (val) {
                            case Board.PLAYER_X: sb.append("X"); break;
                            case Board.PLAYER_O: sb.append("O"); break;
                            default:       sb.append("."); break;
                        }
                    }
                    sb.append( (big_c < 2) ? " | " : ""); // Separator between local boards
                }
                sb.append("\n");
            }
            if (big_r < 2) {
                sb.append("---------------------\n"); // Separator between rows of local boards
            }
        }
        return sb.toString();
    }
}