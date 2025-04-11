import java.util.ArrayList;

// A helper class to represent a move on the global board.
// A move consists of the coordinates of the small board (globalRow, globalCol)
// and the cell within that small board (localRow, localCol).
public class GlobalMove {
    private int globalRow;
    private int globalCol;
    private int localRow;
    private int localCol;

    public GlobalMove(int globalRow, int globalCol, int localRow, int localCol) {
        this.globalRow = globalRow;
        this.globalCol = globalCol;
        this.localRow = localRow;
        this.localCol = localCol;
    }

    public int getGlobalRow() {
        return globalRow;
    }

    public int getGlobalCol() {
        return globalCol;
    }

    public int getLocalRow() {
        return localRow;
    }

    public int getLocalCol() {
        return localCol;
    }

    @Override
    public String toString() {
        return "GlobalMove [Global=(" + globalRow + ", " + globalCol + "), Local=(" + localRow + ", " + localCol + ")]";
    }
}
