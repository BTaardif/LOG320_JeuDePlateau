import java.util.ArrayList;

public class GlobalBoard {
    public static final int SIZE = 3;  // Global board is a 3x3 grid of local boards
    private LocalBoard[][] boards;     // 3x3 array of local boards

    // Global board evaluation constants
    private static final int PLAYER_EMPTY = 0;
    private static final int PLAYER_O = 2;
    private static final int PLAYER_X = 4;

    // Enum for the state of the global board
    public enum GlobalBoardState { X_WON, O_WON, DRAW, ONGOING }
    private GlobalBoardState globalState;

    // Constructor: initializes all local boards
    public GlobalBoard() {
        boards = new LocalBoard[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boards[i][j] = new LocalBoard();
            }
        }
        globalState = GlobalBoardState.ONGOING;
    }

    // Copy constructor: deep copies each local board
    public GlobalBoard(GlobalBoard other) {
        boards = new LocalBoard[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boards[i][j] = new LocalBoard(other.boards[i][j]);
            }
        }
        globalState = other.globalState;
    }

    // Plays a move on the global board.
    // The move is provided as global coordinates: row and col in the overall 9x9 board.
    // They are mapped to a local board and a cell within that board.
    public boolean playMove(int globalRow, int globalCol, int mark) {
        int localBoardRow = globalRow / 3;
        int localBoardCol = globalCol / 3;
        int cellRow = globalRow % 3;
        int cellCol = globalCol % 3;
        LocalBoard localBoard = boards[localBoardRow][localBoardCol];

        boolean success = localBoard.play(cellRow, cellCol, mark);
        if (success) {
            updateGlobalState();
        }
        return success;
    }

    // Updates the global state by checking each local board's outcome.
    // A helper grid is built where each cell represents the winner (or empty) of the corresponding local board.
    private void updateGlobalState() {
        int[][] winGrid = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                LocalBoard lb = boards[i][j];
                if (lb.getState() == LocalBoard.BoardState.X_WON) {
                    winGrid[i][j] = PLAYER_X;
                } else if (lb.getState() == LocalBoard.BoardState.O_WON) {
                    winGrid[i][j] = PLAYER_O;
                } else {
                    winGrid[i][j] = PLAYER_EMPTY;
                }
            }
        }
        int winner = checkWinner(winGrid);
        if (winner == PLAYER_X) {
            globalState = GlobalBoardState.X_WON;
        } else if (winner == PLAYER_O) {
            globalState = GlobalBoardState.O_WON;
        } else if (isFull()) {
            globalState = GlobalBoardState.DRAW;
        } else {
            globalState = GlobalBoardState.ONGOING;
        }
    }

    // Helper method to check if there's a winner in the 3x3 grid of local boards.
    // Returns PLAYER_X, PLAYER_O, or PLAYER_EMPTY.
    private int checkWinner(int[][] grid) {
        // Check rows
        for (int i = 0; i < SIZE; i++) {
            if (grid[i][0] != PLAYER_EMPTY && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
                return grid[i][0];
            }
        }
        // Check columns
        for (int j = 0; j < SIZE; j++) {
            if (grid[0][j] != PLAYER_EMPTY && grid[0][j] == grid[1][j] && grid[1][j] == grid[2][j]) {
                return grid[0][j];
            }
        }
        // Check diagonals
        if (grid[0][0] != PLAYER_EMPTY && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            return grid[0][0];
        }
        if (grid[0][2] != PLAYER_EMPTY && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
            return grid[0][2];
        }
        return PLAYER_EMPTY;
    }

    // Checks if every local board is finished (won or drawn).
    public boolean isFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                // If a board is still ongoing, then the global board is not full.
                if (boards[i][j].getState() == LocalBoard.BoardState.ONGOING) {
                    return false;
                }
            }
        }
        return true;
    }

    // Returns the current state of the global board.
    public GlobalBoardState getGlobalState() {
        return globalState;
    }

    /**
     * Generates a list of possible global moves.
     * 
     * @param activeLocalRow the row index (0-2) of the active local board (or null if not forced)
     * @param activeLocalCol the column index (0-2) of the active local board (or null if not forced)
     * @return an ArrayList of GlobalMove objects representing legal moves.
     */
    public ArrayList<GlobalMove> getPossibleGlobalMoves(Integer activeLocalRow, Integer activeLocalCol) {
        ArrayList<GlobalMove> moves = new ArrayList<>();
        // If the active board is specified and still playable, generate moves only from that board.
        if (activeLocalRow != null && activeLocalCol != null) {
            if (boards[activeLocalRow][activeLocalCol].getState() == LocalBoard.BoardState.ONGOING) {
                for (Move localMove : boards[activeLocalRow][activeLocalCol].getPossibleLocalMoves()) {
                    moves.add(new GlobalMove(activeLocalRow, activeLocalCol, localMove.getLocalRow(), localMove.getLocalCol()));
                }
                return moves;
            }
        }
        // Otherwise, generate moves for all local boards that are still ongoing.
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (boards[i][j].getState() == LocalBoard.BoardState.ONGOING) {
                    for (Move localMove : boards[i][j].getPossibleLocalMoves()) {
                        moves.add(new GlobalMove(i, j, localMove.getLocalRow(), localMove.getLocalCol()));
                    }
                }
            }
        }
        return moves;
    }

    public LocalBoard getLocalBoard(int i, int j) {
        return boards[i][j];
    }

    // For debugging: returns a string representation of the global board.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sb.append("Local Board (").append(i).append(",").append(j).append("):\n");
                sb.append(boards[i][j].toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
