import java.util.ArrayList;

public class GlobalBoard {
    public Board[][] boards;
    private int[][] winners; // Stores Board.X, Board.O, or Board.EMPTY

    // --- Constants for Heuristics ---
    private static final int GLOBAL_WIN_SCORE = 100000; // Score for winning the global game
    private static final int LOCAL_WIN_SCORE = 100; // Base score for winning a local board
    private static final int GLOBAL_TWO_IN_ROW_SCORE = 500; // Score for having 2 won local boards in a line
    // Weights for strategic importance of local boards (Center > Corner > Side)
    private static final int[][] BOARD_WEIGHTS = {
            { 3, 2, 3 }, // Weights for row 0 (Corner, Side, Corner)
            { 2, 4, 2 }, // Weights for row 1 (Side, Center, Side)
            { 3, 2, 3 } // Weights for row 2 (Corner, Side, Corner)
    };

    public GlobalBoard() {
        boards = new Board[3][3];
        winners = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boards[i][j] = new Board();
                winners[i][j] = Board.EMPTY;
            }
        }
    }

    // Copy constructor
    public GlobalBoard(GlobalBoard other) {
        boards = new Board[3][3];
        winners = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boards[i][j] = new Board(other.boards[i][j]);
                winners[i][j] = other.winners[i][j];
            }
        }
    }

    // Plays a move on the specified small board.
    public boolean play(GlobalMove move, int mark) {
        int gRow = move.getGlobalRow();
        int gCol = move.getGlobalCol();
        int lRow = move.getLocalRow();
        int lCol = move.getLocalCol();

        // Check if the targeted small board is already closed or move is invalid
        // locally
        if (winners[gRow][gCol] != Board.EMPTY || !boards[gRow][gCol].isValidMove(new Move(lRow, lCol))) {
            System.err.println("Invalid move attempted: " + move + " on board status: " + winners[gRow][gCol]);
            printGlobalStatus(); // Print status for debugging
            // Optionally print the specific local board
            // boards[gRow][gCol].printBoard();
            return false;
        }

        // Play the move on the small board.
        Board localBoard = boards[gRow][gCol];
        localBoard.play(new Move(lRow, lCol), mark);

        // Update the winner for the local board if it's now won or drawn.
        if (localBoard.checkWinner(mark)) {
            winners[gRow][gCol] = mark;
        } else if (localBoard.checkWinner((mark == Board.X) ? Board.O : Board.X)) {
            winners[gRow][gCol] = (mark == Board.X) ? Board.O : Board.X; // Opponent won
        } else if (localBoard.isFull()) {
            // Keep as EMPTY for a draw on the local board, or introduce a DRAW constant if
            // needed
            winners[gRow][gCol] = Board.EMPTY; // Explicitly mark drawn boards if needed, maybe a different constant?
                                               // For now, EMPTY signifies not won by X or O.
        }
        // Else, the board is still ongoing, winner remains EMPTY.

        return true;
    }

    // Returns true if the small board at (gRow, gCol) is closed (won by X or O, or
    // full and drawn).
    public boolean isLocalBoardClosed(int gRow, int gCol) {
        // A board is closed if it has a winner OR if it's full (no more moves possible)
        return winners[gRow][gCol] != Board.EMPTY || boards[gRow][gCol].isFull();
    }

    // Checks for a global win by examining the winners array.
    public int checkGlobalWinner() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (winners[i][0] != Board.EMPTY && winners[i][0] == winners[i][1] && winners[i][1] == winners[i][2])
                return winners[i][0];
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (winners[0][i] != Board.EMPTY && winners[0][i] == winners[1][i] && winners[1][i] == winners[2][i])
                return winners[0][i];
        }
        // Check diagonals
        if (winners[0][0] != Board.EMPTY && winners[0][0] == winners[1][1] && winners[1][1] == winners[2][2])
            return winners[0][0];
        if (winners[0][2] != Board.EMPTY && winners[0][2] == winners[1][1] && winners[1][1] == winners[2][0])
            return winners[0][2];

        return Board.EMPTY; // No global winner yet
    }

    // Generates possible moves based on the last move played.
    // lastMove determines the required board (forcedGlobalRow, forcedGlobalCol).
    public ArrayList<GlobalMove> getPossibleMoves(GlobalMove lastMove) {
        ArrayList<GlobalMove> moves = new ArrayList<>();
        int forcedGlobalRow = -1;
        int forcedGlobalCol = -1;

        // Determine the required board based on the opponent's last move's local
        // position
        if (lastMove != null) {
            forcedGlobalRow = lastMove.getLocalRow();
            forcedGlobalCol = lastMove.getLocalCol();
        }

        // Case 1: First move of the game (lastMove is null) OR forced board is closed
        if (lastMove == null || isLocalBoardClosed(forcedGlobalRow, forcedGlobalCol)) {
            // Allow moves in ANY non-closed board
            for (int gRow = 0; gRow < 3; gRow++) {
                for (int gCol = 0; gCol < 3; gCol++) {
                    if (!isLocalBoardClosed(gRow, gCol)) {
                        Board localBoard = boards[gRow][gCol];
                        for (Move m : localBoard.getPossibleMoves()) {
                            moves.add(new GlobalMove(gRow, gCol, m.getRow(), m.getCol()));
                        }
                    }
                }
            }
        }
        // Case 2: Forced board is open
        else {
            Board localBoard = boards[forcedGlobalRow][forcedGlobalCol];
            for (Move m : localBoard.getPossibleMoves()) {
                moves.add(new GlobalMove(forcedGlobalRow, forcedGlobalCol, m.getRow(), m.getCol()));
            }
        }
        return moves;
    }

    // Enhanced global evaluation function incorporating local heuristics and global
    // strategy.
    public int evaluateGlobal(int mark) {
        int adversary = (mark == Board.X) ? Board.O : Board.X;
        int globalScore = 0;

        // 1. Check for immediate Global Win/Loss (highest priority)
        int globalWinner = checkGlobalWinner();
        if (globalWinner == mark)
            return GLOBAL_WIN_SCORE;
        if (globalWinner == adversary)
            return -GLOBAL_WIN_SCORE;

        // 2. Evaluate based on local board states and global positioning
        for (int gRow = 0; gRow < 3; gRow++) {
            for (int gCol = 0; gCol < 3; gCol++) {
                int weight = BOARD_WEIGHTS[gRow][gCol]; // Strategic weight of this board
                if (winners[gRow][gCol] == mark) {
                    globalScore += LOCAL_WIN_SCORE * weight;
                } else if (winners[gRow][gCol] == adversary) {
                    globalScore -= LOCAL_WIN_SCORE * weight;
                } else {
                    // If board is not closed, add its heuristic value, scaled by strategic weight
                    if (!isLocalBoardClosed(gRow, gCol)) {
                        // Use the detailed heuristic evaluation for ongoing boards
                        globalScore += boards[gRow][gCol].heuristicEvaluate(mark) * weight;
                    }
                    // Optionally add a small penalty if the local board is drawn (closed but not
                    // won)
                    // else if (boards[gRow][gCol].isFull()) { globalScore -= 1 * weight; }
                }
            }
        }

        // 3. Add bonus/penalty for potential global wins (2 won boards in a row)
        globalScore += checkGlobalTwoInRow(mark) * GLOBAL_TWO_IN_ROW_SCORE;
        globalScore -= checkGlobalTwoInRow(adversary) * GLOBAL_TWO_IN_ROW_SCORE;

        // 4. Check if the entire game is a draw (all boards closed, no global winner)
        if (isGlobalBoardFull() && globalWinner == Board.EMPTY) {
            return 0; // Global Draw
        }

        return globalScore;
    }

    // Helper to check if the global board is full (all local boards are closed).
    public boolean isGlobalBoardFull() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (!isLocalBoardClosed(r, c)) {
                    return false;
                }
            }
        }
        return true; // All boards are closed
    }

    // Helper function to count potential global wins (2 won boards in a
    // row/col/diag with the 3rd open).
    private int checkGlobalTwoInRow(int piece) {
        int count = 0;
        // Check rows
        for (int r = 0; r < 3; r++) {
            if (winners[r][0] == piece && winners[r][1] == piece && !isLocalBoardClosed(r, 2)
                    && winners[r][2] == Board.EMPTY)
                count++;
            if (winners[r][0] == piece && winners[r][2] == piece && !isLocalBoardClosed(r, 1)
                    && winners[r][1] == Board.EMPTY)
                count++;
            if (winners[r][1] == piece && winners[r][2] == piece && !isLocalBoardClosed(r, 0)
                    && winners[r][0] == Board.EMPTY)
                count++;
        }
        // Check columns
        for (int c = 0; c < 3; c++) {
            if (winners[0][c] == piece && winners[1][c] == piece && !isLocalBoardClosed(2, c)
                    && winners[2][c] == Board.EMPTY)
                count++;
            if (winners[0][c] == piece && winners[2][c] == piece && !isLocalBoardClosed(1, c)
                    && winners[1][c] == Board.EMPTY)
                count++;
            if (winners[1][c] == piece && winners[2][c] == piece && !isLocalBoardClosed(0, c)
                    && winners[0][c] == Board.EMPTY)
                count++;
        }
        // Check diagonals
        if (winners[0][0] == piece && winners[1][1] == piece && !isLocalBoardClosed(2, 2)
                && winners[2][2] == Board.EMPTY)
            count++;
        if (winners[0][0] == piece && winners[2][2] == piece && !isLocalBoardClosed(1, 1)
                && winners[1][1] == Board.EMPTY)
            count++;
        if (winners[1][1] == piece && winners[2][2] == piece && !isLocalBoardClosed(0, 0)
                && winners[0][0] == Board.EMPTY)
            count++;

        if (winners[0][2] == piece && winners[1][1] == piece && !isLocalBoardClosed(2, 0)
                && winners[2][0] == Board.EMPTY)
            count++;
        if (winners[0][2] == piece && winners[2][0] == piece && !isLocalBoardClosed(1, 1)
                && winners[1][1] == Board.EMPTY)
            count++;
        if (winners[1][1] == piece && winners[2][0] == piece && !isLocalBoardClosed(0, 2)
                && winners[0][2] == Board.EMPTY)
            count++;

        return count;
    }

    public void updateAllLocalWinners() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (winners[r][c] == Board.EMPTY) { // Only update if not already decided
                    if (boards[r][c].checkWinner(Board.X)) {
                        winners[r][c] = Board.X;
                    } else if (boards[r][c].checkWinner(Board.O)) {
                        winners[r][c] = Board.O;
                    } else if (boards[r][c].isFull()) {
                        // Keep as EMPTY for draw, or use a DRAW constant
                        // winners[r][c] = Board.DRAW; // If you add a DRAW constant
                    }
                }
            }
        }
        System.out.println("Winners array updated after board parse:");
        printGlobalStatus();
    }

    // Debugging method: Prints the status of winners array.
    public void printGlobalStatus() {
        System.out.println("--- Global Board Status ---");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char symbol = '.';
                if (winners[i][j] == Board.X)
                    symbol = 'X';
                else if (winners[i][j] == Board.O)
                    symbol = 'O';
                else if (isLocalBoardClosed(i, j))
                    symbol = 'D'; // Indicate Drawn/Full board
                System.out.print(symbol + "\t");
            }
            System.out.println();
        }
        System.out.println("-------------------------");
    }

    // Debugging method: Prints the entire global board state.
    public void printDetailedGlobalBoard() {
        System.out.println("======= DETAILED GLOBAL BOARD STATE =======");
        for (int gRow = 0; gRow < 3; gRow++) {
            for (int lRow = 0; lRow < 3; lRow++) { // Print row by row for each local board line
                for (int gCol = 0; gCol < 3; gCol++) {
                    for (int lCol = 0; lCol < 3; lCol++) {
                        int piece = boards[gRow][gCol].getCell(lRow, lCol);
                        char symbol = '.';
                        if (piece == Board.X)
                            symbol = 'X';
                        else if (piece == Board.O)
                            symbol = 'O';
                        System.out.print(symbol);
                    }
                    if (gCol < 2)
                        System.out.print(" | "); // Separator between local boards horizontally
                }
                System.out.println(); // Newline after printing a full line of local board rows
            }
            if (gRow < 2)
                System.out.println("-----------+-----------+-----------"); // Separator between local boards vertically
        }
        System.out.println("==========================================");
        printGlobalStatus(); // Also show the winners overview
    }
}