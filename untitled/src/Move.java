

class Move
{
    private int row;
    private int col;

    public Move(){
        row = -1;
        col = -1;
    }

    public Move(int r, int c){
        row = r;
        col = c;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public void setRow(int r){
        row = r;
    }

    public void setCol(int c){
        col = c;
    }
}
