import java.util.ArrayList;

// Represents a single 3x3 Tic-Tac-Toe board (Local Board)
class LocalBoard {
    private int[][] board;
    
    private static final int PLAYER_EMPTY = 0;
    private static final int PLAYER_O = 2;
    private static final int PLAYER_X = 4;
    
    private boolean isFull = false;
    private int localWinner = PLAYER_EMPTY;

    // Enum to represent the state of this local board
    public enum BoardState {
        X_WON, O_WON, DRAW, ONGOING
    }
    
    private BoardState state;

    // Constructor
    public LocalBoard() {
        board = new int[3][3];
        // Initialize board cells to empty
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = PLAYER_EMPTY;
            }
        }
        // Set initial state
        state = BoardState.ONGOING;
    }

    // Copy Constructor
    public LocalBoard(LocalBoard other) {
        this.board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, 3);
        }
        this.state = other.state;
        this.isFull = other.isFull;
        this.localWinner = other.localWinner;
    }

    // Getters
    public int getMark(int r, int c) {
        return board[r][c];
    }

    public BoardState getState() {
        return state;
    }

    public int getLocalWinner() {
        return localWinner;
    }

    private void setLocalWinner(int winner) {
        localWinner = winner;
    }

    // Check if a move is valid within this local board
    public boolean isMoveValid(int r, int c) {
        return r >= 0 && r < 3 && c >= 0 && c < 3 && board[r][c] == PLAYER_EMPTY && state == BoardState.ONGOING;
    }

    // Place a mark and update the board's state
    public boolean play(int r, int c, int mark) {
        if (isMoveValid(r, c)) {
            board[r][c] = mark;
            updateState();
            return true;
        }
        return false;
    }

    // Evaluates the board state from the perspective of 'player'
    // Returns 100 for a win, -100 for a loss, 0 otherwise.
    public int evaluate(int player) {
        int opponent = (player == PLAYER_X) ? PLAYER_O : PLAYER_X;

        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            // Row check
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != PLAYER_EMPTY) {
                if (board[i][0] == player) return 100;
                if (board[i][0] == opponent) return -100;
            }
            // Column check
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != PLAYER_EMPTY) {
                if (board[0][i] == player) return 100;
                if (board[0][i] == opponent) return -100;
            }
        }

        // Check diagonals
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != PLAYER_EMPTY) {
            if (board[0][0] == player) return 100;
            if (board[0][0] == opponent) return -100;
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != PLAYER_EMPTY) {
            if (board[0][2] == player) return 100;
            if (board[0][2] == opponent) return -100;
        }

        // If no win/loss, return 0. (You can later enhance this to consider non-terminal heuristics.)
        return 0;
    }

    // Update the state of the board (X_WON, O_WON, DRAW, or ONGOING)
    private void updateState() {
        // Check rows and columns for a win
        for (int i = 0; i < 3; i++) {
            // Row check
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != PLAYER_EMPTY) {
                setLocalWinner(board[i][0]);
                state = (board[i][0] == PLAYER_X) ? BoardState.X_WON : BoardState.O_WON;
                isFull = true;
                return;
            }
            // Column check
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != PLAYER_EMPTY) {
                setLocalWinner(board[0][i]);
                state = (board[0][i] == PLAYER_X) ? BoardState.X_WON : BoardState.O_WON;
                isFull = true;
                return;
            }
        }
        
        // Check diagonals
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != PLAYER_EMPTY) {
            setLocalWinner(board[0][0]);
            state = (board[0][0] == PLAYER_X) ? BoardState.X_WON : BoardState.O_WON;
            isFull = true;
            return;
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != PLAYER_EMPTY) {
            setLocalWinner(board[0][2]);
            state = (board[0][2] == PLAYER_X) ? BoardState.X_WON : BoardState.O_WON;
            isFull = true;
            return;
        }
        
        // Check if board is full (i.e., no empty cells)
        boolean full = true;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == PLAYER_EMPTY) {
                    full = false;
                    break;
                }
            }
        }
        if (full) {
            isFull = true;
            state = BoardState.DRAW;
        }
    }

    // Returns a list of valid moves for this local board.
    public ArrayList<Move> getPossibleLocalMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        if (state == BoardState.ONGOING) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c] == PLAYER_EMPTY) {
                        moves.add(new Move(r, c));
                    }
                }
            }
        }
        return moves;
    }

    // For debugging: a simple string representation of the board.
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                sb.append(board[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
