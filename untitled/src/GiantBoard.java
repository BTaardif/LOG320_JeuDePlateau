import java.util.ArrayList;

public class GiantBoard {
    private int[][] board;
    private int activeLocalRow;
    private int activeLocalCol;

    public GiantBoard() {
        board = new int[9][9];
        activeLocalRow = -1;
        activeLocalCol = -1;
    }

    public void updateBoard(String data) {
        String[] tokens = data.split(" ");
        int index = 0;
        for(int y = 0; y < 9; y++){
            for(int x = 0; x < 9; x++){
                board[y][x] = Integer.parseInt(tokens[index]);
                index++;
            }
        }
    }

    public void playMove(int row, int col, int piece) {
        board[row][col] = piece;
        int r = row % 3;
        int c = col % 3;
        activeLocalRow = r;
        activeLocalCol = c;
    }

    public boolean isSubBoardClosed(int r, int c) {
        int sr = r * 3;
        int sc = c * 3;
        for(int i = sr; i < sr+3; i++){
            for(int j = sc; j < sc+3; j++){
                if(board[i][j] == 0){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isGameOver() {
        return false;
    }

    public int evaluate(int myPiece) {
        int score = 0;
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                if(board[i][j] == myPiece){
                    score++;
                } else if(board[i][j] != 0){
                    score--;
                }
            }
        }
        return score;
    }

    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        if(activeLocalRow != -1 && activeLocalCol != -1 && !isSubBoardClosed(activeLocalRow, activeLocalCol)){
            int sr = activeLocalRow * 3;
            int sc = activeLocalCol * 3;
            for(int i = sr; i < sr+3; i++){
                for(int j = sc; j < sc+3; j++){
                    if(board[i][j] == 0){
                        moves.add(new Move(i,j));
                    }
                }
            }
            if(!moves.isEmpty()){
                return moves;
            }
        }
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                if(board[i][j] == 0){
                    moves.add(new Move(i,j));
                }
            }
        }
        return moves;
    }

    public int[][] copyBoard() {
        int[][] copy = new int[9][9];
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }

    public void restoreBoard(int[][] data, int row, int col) {
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                board[i][j] = data[i][j];
            }
        }
        activeLocalRow = row;
        activeLocalCol = col;
    }

    public int getActiveLocalRow() {
        return activeLocalRow;
    }

    public int getActiveLocalCol() {
        return activeLocalCol;
    }
}
