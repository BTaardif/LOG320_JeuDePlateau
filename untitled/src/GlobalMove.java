public class GlobalMove {
    private int localBoardRow, localBoardCol; // Which local board (0-2, 0-2)
    private int cellRow, cellCol;             // The cell within that local board (0-2, 0-2)

    public GlobalMove(int localBoardRow, int localBoardCol, int cellRow, int cellCol) {
        this.localBoardRow = localBoardRow;
        this.localBoardCol = localBoardCol;
        this.cellRow = cellRow;
        this.cellCol = cellCol;
    }

    // Getters
    public int getLocalBoardRow() { return localBoardRow; }
    public int getLocalBoardCol() { return localBoardCol; }
    public int getCellRow() { return cellRow; }
    public int getCellCol() { return cellCol; }

    @Override
    public String toString() {
        return "LocalBoard(" + localBoardRow + "," + localBoardCol + ") Cell(" + cellRow + "," + cellCol + ")";
    }
}
