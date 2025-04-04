public class Move {
    private int row, col;

    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // Getters
     public int getLocalRow() { return row; }
     public int getLocalCol() { return col; }
}