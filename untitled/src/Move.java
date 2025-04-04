public class Move {
    private int globalRow, globalCol, localRow, localCol;

    // Constructor might need adjustment based on how you handle moves
    public Move(int gR, int gC, int lR, int lC) {
        this.globalRow = gR;
        this.globalCol = gC;
        this.localRow = lR;
        this.localCol = lC;
    }

    // Getters...
     public int getGlobalRow() { return globalRow; }
     public int getGlobalCol() { return globalCol; }
     public int getLocalRow() { return localRow; }
     public int getLocalCol() { return localCol; }

     @Override
     public String toString() {
         // Example format, adjust as needed for server
         return "Move[g(" + globalRow + "," + globalCol + "), l(" + localRow + "," + localCol +")]";
     }
}