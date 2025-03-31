// Board.java (Revised)
import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int EMPTY = 0;
    public static final int PLAYER_O = 2; // Represents O
    public static final int PLAYER_X = 4; // Represents X
    public static final int DRAW = 1;     // Represents a draw

    protected int[][] cells = new int[3][3];
    private int winner = EMPTY; // Store the winner state

    public Board() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = EMPTY;
            }
        }
        // Initial state doesn't require recalcWinner, winner is EMPTY
    }

    // Copy constructor (if needed, ensure deep copy and winner state)
    public Board(Board original) {
        for (int i = 0; i < 3; i++) {
            System.arraycopy(original.cells[i], 0, this.cells[i], 0, 3);
        }
        this.winner = original.winner; // Copy winner state too
    }

    public boolean isCellEmpty(int row, int col) {
        // Added bounds check for safety
        if (row < 0 || row > 2 || col < 0 || col > 2) return false;
        return cells[row][col] == EMPTY;
    }

    // Places a move AND updates the winner state
    public boolean placeMove(int row, int col, int player) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3 && isCellEmpty(row, col) && winner == EMPTY) {
            cells[row][col] = player;
            recalcWinner(); // Update winner status immediately
            return true;
        }
        return false;
    }

    // Undoes a move AND updates the winner state
    public void undoMove(int row, int col) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3 && cells[row][col] != EMPTY) {
            // Only undo if the cell wasn't empty
            cells[row][col] = EMPTY;
            recalcWinner(); // Update winner status immediately
        }
    }

    public int getCell(int row, int col) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            return cells[row][col];
        }
        return -1; // Indicate invalid coords
    }

    // Gets the current winner (state is updated by placeMove/undoMove)
    public int getWinner() {
        return winner;
    }

    // Recalculates and sets the winner status (called internally)
    private void recalcWinner() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (cells[i][0] != EMPTY && cells[i][0] == cells[i][1] && cells[i][1] == cells[i][2]) {
                winner = cells[i][0];
                return;
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (cells[0][i] != EMPTY && cells[0][i] == cells[1][i] && cells[1][i] == cells[2][i]) {
                winner = cells[0][i];
                return;
            }
        }
        // Check diagonals
        if (cells[0][0] != EMPTY && cells[0][0] == cells[1][1] && cells[1][1] == cells[2][2]) {
            winner = cells[0][0];
            return;
        }
        if (cells[0][2] != EMPTY && cells[0][2] == cells[1][1] && cells[1][1] == cells[2][0]) {
            winner = cells[0][2];
            return;
        }

        // Check for draw (no winner yet, but board is full)
        boolean full = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == EMPTY) {
                    full = false;
                    break;
                }
            }
            if (!full) break;
        }

        if (full) {
            winner = DRAW; // Board is full, no winner = Draw
            return;
        }

        // If not won and not full, it's ongoing
        winner = EMPTY;
    }

    // Checks if the board is full (used for draw condition in recalcWinner)
    public boolean isFull() {
        // This check is implicitly handled by recalcWinner setting DRAW.
        // Can still be useful for other logic if needed.
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == EMPTY) return false;
            }
        }
        return true;
    }

    // Checks if the game on this local board is over (won or drawn)
    public boolean isTerminal() {
        return winner != EMPTY;
    }

    // Gets available moves ONLY if the board is not terminal
    public List<int[]> getAvailableMoves() {
        List<int[]> moves = new ArrayList<>();
        if (isTerminal()) { // No moves if board is finished
            return moves;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == EMPTY) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }

    // Basic evaluation for a local board
    public int evaluate(int player) {
        int currentWinner = getWinner();
        int opponent = (player == PLAYER_X) ? PLAYER_O : PLAYER_X;

        if (currentWinner == player) return 100;    // Win
        if (currentWinner == opponent) return -100; // Loss
        if (currentWinner == DRAW) return 0;      // Draw

        // Heuristic score for non-terminal boards
        int score = 0;
        score += evaluateLine(0, 0, 0, 1, 0, 2, player); // Rows
        score += evaluateLine(1, 0, 1, 1, 1, 2, player);
        score += evaluateLine(2, 0, 2, 1, 2, 2, player);
        score += evaluateLine(0, 0, 1, 0, 2, 0, player); // Cols
        score += evaluateLine(0, 1, 1, 1, 2, 1, player);
        score += evaluateLine(0, 2, 1, 2, 2, 2, player);
        score += evaluateLine(0, 0, 1, 1, 2, 2, player); // Diags
        score += evaluateLine(0, 2, 1, 1, 2, 0, player);
        return score;
    }

    // Heuristic for a single line (3 cells)
    private int evaluateLine(int r1, int c1, int r2, int c2, int r3, int c3, int player) {
        int score = 0;
        int opponent = (player == PLAYER_X) ? PLAYER_O : PLAYER_X;

        // Count player's and opponent's pieces in the line
        int playerCount = 0;
        int opponentCount = 0;
        if (cells[r1][c1] == player) playerCount++; else if (cells[r1][c1] == opponent) opponentCount++;
        if (cells[r2][c2] == player) playerCount++; else if (cells[r2][c2] == opponent) opponentCount++;
        if (cells[r3][c3] == player) playerCount++; else if (cells[r3][c3] == opponent) opponentCount++;

        // Assign score based on counts
        if (playerCount == 3) score = 100; // Should be caught by getWinner, but keep for heuristic consistency
        else if (playerCount == 2 && opponentCount == 0) score = 10;  // Two in a row, empty third
        else if (playerCount == 1 && opponentCount == 0) score = 1;   // One in a row, two empty
        else if (opponentCount == 3) score = -100;// Should be caught by getWinner
        else if (opponentCount == 2 && playerCount == 0) score = -10; // Opponent has two in a row
        else if (opponentCount == 1 && playerCount == 0) score = -1;  // Opponent has one in a row
        // If line has both X and O, score is 0

        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == PLAYER_X) sb.append("X");
                else if (cells[i][j] == PLAYER_O) sb.append("O");
                else if (cells[i][j] == DRAW) sb.append("D"); // Should not happen in cell, but for winner
                else sb.append(".");
            }
            if (i < 2) sb.append("\n"); // Newline except for last row
        }
        return sb.toString();
    }
}