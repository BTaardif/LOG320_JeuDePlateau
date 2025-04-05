import java.util.ArrayList;

public class Board {
    // Constants representing the pieces.
    public static final int EMPTY = 0;
    public static final int O = 2;
    public static final int X = 4;

    private int[][] board;

    // Default constructor initializes a 3x3 board with all cells empty.
    public Board() {
        board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    // Copy constructor for creating a deep copy of the board.
    public Board(Board other) {
        board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = other.board[i][j];
            }
        }
    }

    // Plays a move on the board.
    public void play(Move m, int piece) {
        if (isValidMove(m)) {
            board[m.getRow()][m.getCol()] = piece;
        }
    }

    // Checks if a move is valid (within bounds and on an empty cell).
    public boolean isValidMove(Move m) {
        int r = m.getRow();
        int c = m.getCol();
        return r >= 0 && r < 3 && c >= 0 && c < 3 && board[r][c] == EMPTY;
    }

    // Returns the piece at a given cell.
    public int getCell(int r, int c) {
        return board[r][c];
    }

    // Simple evaluation: returns 100 for win, -100 for loss, 0 for draw/ongoing.
    // Useful for checking if a board is definitively won/lost.
    public int evaluate(int piece) {
        int opponent = (piece == X) ? O : X;

        // Check rows, columns, and diagonals for a win/loss
        if (checkWinner(piece))
            return 100;
        if (checkWinner(opponent))
            return -100;

        // If no winner and board is full, it's a draw (return 0)
        // If no winner and board is not full, it's ongoing (return 0)
        return 0; // Covers both draw and ongoing for this simple evaluation
    }

    // More detailed heuristic evaluation for ongoing games.
    // Assigns points based on potential winning lines.
    public int heuristicEvaluate(int piece) {
        int score = 0;
        int opponent = (piece == X) ? O : X;

        // Check for immediate win/loss first
        if (checkWinner(piece))
            return 1000; // High score for winning the local board
        if (checkWinner(opponent))
            return -1000; // High penalty for losing

        // Evaluate lines (rows, columns, diagonals)
        score += evaluateLine(0, 0, 0, 1, 0, 2, piece); // Row 0
        score += evaluateLine(1, 0, 1, 1, 1, 2, piece); // Row 1
        score += evaluateLine(2, 0, 2, 1, 2, 2, piece); // Row 2
        score += evaluateLine(0, 0, 1, 0, 2, 0, piece); // Col 0
        score += evaluateLine(0, 1, 1, 1, 2, 1, piece); // Col 1
        score += evaluateLine(0, 2, 1, 2, 2, 2, piece); // Col 2
        score += evaluateLine(0, 0, 1, 1, 2, 2, piece); // Diag 1
        score += evaluateLine(0, 2, 1, 1, 2, 0, piece); // Diag 2

        // Add a small bonus for controlling the center
        if (board[1][1] == piece)
            score += 1;
        else if (board[1][1] == opponent)
            score -= 1;

        if (isFull() && score == 0)
            return 0; // Draw

        return score;
    }

    // Helper for heuristicEvaluate: evaluates a single line (row, col, or diag).
    private int evaluateLine(int r1, int c1, int r2, int c2, int r3, int c3, int piece) {
        int score = 0;
        int opponent = (piece == X) ? O : X;

        // Get pieces in the line
        int p1 = board[r1][c1];
        int p2 = board[r2][c2];
        int p3 = board[r3][c3];

        // Check for player's opportunities
        if (p1 == piece && p2 == piece && p3 == EMPTY)
            score = 10;
        else if (p1 == piece && p2 == EMPTY && p3 == piece)
            score = 10;
        else if (p1 == EMPTY && p2 == piece && p3 == piece)
            score = 10;
        else if (p1 == piece && p2 == EMPTY && p3 == EMPTY)
            score = 1;
        else if (p1 == EMPTY && p2 == piece && p3 == EMPTY)
            score = 1;
        else if (p1 == EMPTY && p2 == EMPTY && p3 == piece)
            score = 1;

        // Check for opponent's opportunities (negative score)
        if (p1 == opponent && p2 == opponent && p3 == EMPTY)
            score = -10;
        else if (p1 == opponent && p2 == EMPTY && p3 == opponent)
            score = -10;
        else if (p1 == EMPTY && p2 == opponent && p3 == opponent)
            score = -10;
        else if (p1 == opponent && p2 == EMPTY && p3 == EMPTY)
            score = -1;
        else if (p1 == EMPTY && p2 == opponent && p3 == EMPTY)
            score = -1;
        else if (p1 == EMPTY && p2 == EMPTY && p3 == opponent)
            score = -1;

        return score;
    }

    // Checks if the given piece has won.
    public boolean checkWinner(int piece) {
        // Check rows and columns.
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == piece && board[i][1] == piece && board[i][2] == piece)
                return true;
            if (board[0][i] == piece && board[1][i] == piece && board[2][i] == piece)
                return true;
        }
        // Check diagonals.
        if (board[0][0] == piece && board[1][1] == piece && board[2][2] == piece)
            return true;
        if (board[0][2] == piece && board[1][1] == piece && board[2][0] == piece)
            return true;

        return false;
    }

    // Checks if the board is full.
    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    // Returns a list of all possible moves (cells that are empty).
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    moves.add(new Move(i, j));
                }
            }
        }
        return moves;
    }

    // Prints the board to the console (useful for debugging).
    public void printBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char symbol = '.';
                if (board[i][j] == X)
                    symbol = 'X';
                else if (board[i][j] == O)
                    symbol = 'O';
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }
}