import java.util.ArrayList;
import java.util.Arrays;

// Represents a single 3x3 Tic-Tac-Toe board (Local Board)
class LocalBoard {
    private Mark[][] board;
    private Mark winner; // Tracks if X or O won this board, or if it's a DRAW or ONGOING

    // Enum to represent the state of this local board
    public enum BoardState {
        X_WON, O_WON, DRAW, ONGOING
    }
    
    private BoardState state;

    // Constructor
    public LocalBoard() {
        board = new Mark[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }
        state = BoardState.ONGOING; // Initially ongoing
    }

    // Copy Constructor
    public LocalBoard(LocalBoard other) {
        this.board = new Mark[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.board[i][j] = other.board[i][j];
            }
        }
        this.state = other.state;
    }

    // Getters
    public Mark getMark(int r, int c) {
        if (r >= 0 && r < 3 && c >= 0 && c < 3) {
            return board[r][c];
        }
        return null; // Or throw exception for invalid coords
    }

    public BoardState getState() {
        return state;
    }

    // Check if a move is valid within this local board
    public boolean isMoveValid(int r, int c) {
         return r >= 0 && r < 3 && c >= 0 && c < 3 && board[r][c] == Mark.EMPTY && state == BoardState.ONGOING;
    }

    // Place a mark and update the board's state
    public boolean play(int r, int c, Mark mark) {
        if (isMoveValid(r,c)) {
            board[r][c] = mark;
            updateState(mark); // Check if this move ended the local game
            return true;
        }
        return false;
    }

     // Check if the board is full
    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    return false;
                }
            }
        }
        return true; // No empty cells found
    }


    // Updates the state (X_WON, O_WON, DRAW, ONGOING) after a move
    private void updateState(Mark lastPlayer) {
        if (state != BoardState.ONGOING) return; // Already decided

        // Check rows, columns, and diagonals for a win
        Mark winner = checkWin();

        if (winner != Mark.EMPTY) {
            state = (winner == Mark.X) ? BoardState.X_WON : BoardState.O_WON;
        } else if (isFull()) {
            state = BoardState.DRAW;
        }
        // Otherwise, remains ONGOING
    }

    // Helper to check for a win
    private Mark checkWin() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != Mark.EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0];
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != Mark.EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i];
            }
        }
        // Check diagonals
        if (board[0][0] != Mark.EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0];
        }
        if (board[0][2] != Mark.EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2];
        }
        return Mark.EMPTY; // No winner
    }

     // Simple evaluation for the AI (can be expanded)
    public int evaluate(Mark playerPerspective) {
        switch(state) {
            case X_WON: return (playerPerspective == Mark.X) ? 100 : -100;
            case O_WON: return (playerPerspective == Mark.O) ? 100 : -100;
            case DRAW: return 0;
            case ONGOING:
                // Basic heuristic: count potential lines? (Optional, can be simple 0 for ongoing)
                 return 0; // Placeholder
            default: return 0;
        }
    }

     // Get empty cells for possible moves within this board
    public ArrayList<Move> getPossibleLocalMoves(int globalRow, int globalCol) {
        ArrayList<Move> moves = new ArrayList<>();
         if (state == BoardState.ONGOING) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c] == Mark.EMPTY) {
                        moves.add(new Move(globalRow, globalCol, r, c));
                    }
                }
            }
         }
        return moves;
    }

    @Override
    public String toString() {
        // Basic representation
        StringBuilder sb = new StringBuilder();
        for(int r=0; r<3; r++) {
            for (int c=0; c<3; c++) {
                sb.append(board[r][c] == Mark.EMPTY ? "." : board[r][c].toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
