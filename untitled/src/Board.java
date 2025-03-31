// Board.java
// Represents a single 3x3 Tic Tac Toe board.
import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int EMPTY = 0;
    public static final int PLAYER_O = 2; // As per PDF [cite: 36]
    public static final int PLAYER_X = 4; // As per PDF [cite: 36]

    private int[][] cells = new int[3][3];
    private int winner = EMPTY; // Stores the winner of this local board

    public Board() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j] = EMPTY;
            }
        }
    }

    // Copy constructor
    public Board(Board original) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.cells[i][j] = original.cells[i][j];
            }
        }
        this.winner = original.winner;
    }


    public boolean isCellEmpty(int row, int col) {
        return cells[row][col] == EMPTY;
    }

    public boolean placeMove(int row, int col, int player) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3 && isCellEmpty(row, col) && winner == EMPTY) {
            cells[row][col] = player;
            checkWin(); // Check if this move wins the local board
            return true;
        }
        return false;
    }

    public int getCell(int row, int col) {
        return cells[row][col];
    }

    public int getWinner() {
        if (winner == EMPTY) {
            checkWin(); // Ensure winner status is up-to-date
        }
        return winner;
    }

    public boolean isFull() {
        if (winner != EMPTY) return false; // A won board isn't considered full in the context of available moves
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true; // No empty cells left and no winner
    }

    public boolean isTerminal() {
        return getWinner() != EMPTY || isFull();
    }


    // Check for a win condition on this local board
    private void checkWin() {
        if (winner != EMPTY) return; // Already decided

        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (cells[i][0] != EMPTY && cells[i][0] == cells[i][1] && cells[i][1] == cells[i][2]) {
                winner = cells[i][0];
                return;
            }
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

        // Check for draw (implicitly handled by isFull alongside winner check)
    }

    // Get available moves (local coordinates 0-2)
    public List<int[]> getAvailableMoves() {
        List<int[]> moves = new ArrayList<>();
        if (winner != EMPTY) return moves; // No moves if board is won

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] == EMPTY) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }

    // Evaluate the state of this local board
    // Positive score favors PLAYER_X, negative favors PLAYER_O
    public int evaluate(int player) {
        checkWin(); // Ensure winner is up to date
        int opponent = (player == PLAYER_X) ? PLAYER_O : PLAYER_X;

        if (winner == player) {
            return 100; // Strong positive score for winning the board
        } else if (winner == opponent) {
            return -100; // Strong negative score for opponent winning
        } else if (isFull()) {
            return 0; // Neutral score for a draw
        } else {
            // Heuristic: Count potential winning lines
            int score = 0;
            score += evaluateLine(0, 0, 0, 1, 0, 2, player); // Row 1
            score += evaluateLine(1, 0, 1, 1, 1, 2, player); // Row 2
            score += evaluateLine(2, 0, 2, 1, 2, 2, player); // Row 3
            score += evaluateLine(0, 0, 1, 0, 2, 0, player); // Col 1
            score += evaluateLine(0, 1, 1, 1, 2, 1, player); // Col 2
            score += evaluateLine(0, 2, 1, 2, 2, 2, player); // Col 3
            score += evaluateLine(0, 0, 1, 1, 2, 2, player); // Diag 1
            score += evaluateLine(0, 2, 1, 1, 2, 0, player); // Diag 2
            return score;
        }
    }

    // Helper for evaluation: scores a single line (row, col, or diag)
    private int evaluateLine(int r1, int c1, int r2, int c2, int r3, int c3, int player) {
        int score = 0;
        int opponent = (player == PLAYER_X) ? PLAYER_O : PLAYER_X;

        // Cell values
        int cell1 = cells[r1][c1];
        int cell2 = cells[r2][c2];
        int cell3 = cells[r3][c3];

        // Count player's pieces and opponent's pieces
        int playerCount = 0;
        int opponentCount = 0;
        if (cell1 == player) playerCount++; else if (cell1 == opponent) opponentCount++;
        if (cell2 == player) playerCount++; else if (cell2 == opponent) opponentCount++;
        if (cell3 == player) playerCount++; else if (cell3 == opponent) opponentCount++;

        // Assign score based on line state
        if (playerCount == 3) {
            score = 100; // Should be caught by getWinner, but for safety
        } else if (playerCount == 2 && opponentCount == 0) {
            score = 10; // Two in a row, potential win
        } else if (playerCount == 1 && opponentCount == 0) {
            score = 1;  // One in a row
        } else if (opponentCount == 3) {
            score = -100; // Should be caught by getWinner
        } else if (opponentCount == 2 && playerCount == 0) {
            score = -10; // Opponent has two in a row
        } else if (opponentCount == 1 && playerCount == 0) {
            score = -1; // Opponent has one in a row
        }
        // If playerCount > 0 and opponentCount > 0, score is 0 (blocked line)

        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                switch (cells[i][j]) {
                    case PLAYER_X: sb.append("X"); break;
                    case PLAYER_O: sb.append("O"); break;
                    default:       sb.append("."); break;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}