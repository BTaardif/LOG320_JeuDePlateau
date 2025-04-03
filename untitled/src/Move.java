public class Move {
    private int row;
    private int col;
    private Mark mark;
    private String localBoardId; // ID du LocalBoard sur lequel on joue
    private String piece;


    public Move(int r, int c) {
        row = r;
        col = c;
    }

    public Move(int r, int c, Mark mark, String localBoardId,String case_id) {
        this.row = r;
        this.col = c;
        this.mark = mark;
        this.localBoardId = localBoardId;
        this.piece = case_id;
    }
    

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Mark getMark(){
        return mark;
    }

    public String getLocalBoardId(){
        return this.localBoardId;
    }

    public String getCase_id(){
        return this.piece;
    }

    public int getLocalBoardX() {
        return row / 3;  // Donne l'index X du LocalBoard (0,1,2)
    }
    
    public int getLocalBoardY() {
        return col / 3;  // Donne l'index Y du LocalBoard (0,1,2)
    }
    
}
