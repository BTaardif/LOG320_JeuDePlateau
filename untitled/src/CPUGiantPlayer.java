import java.util.ArrayList;

public class CPUGiantPlayer {
    private int myPiece;
    private int depth;

    public CPUGiantPlayer(int piece, int d) {
        myPiece = piece;
        depth = d;
    }

    public Move getMove(GiantBoard giantBoard) {
        ArrayList<Move> moves = giantBoard.getPossibleMoves();
        int bestVal = Integer.MIN_VALUE;
        Move bestMove = null;
        for(Move m : moves){
            int[][] backup = giantBoard.copyBoard();
            int row = giantBoard.getActiveLocalRow();
            int col = giantBoard.getActiveLocalCol();
            giantBoard.playMove(m.getRow(), m.getCol(), myPiece);
            int val = alphaBeta(giantBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            giantBoard.restoreBoard(backup, row, col);
            if(val > bestVal){
                bestVal = val;
                bestMove = m;
            }
        }
        return bestMove;
    }

    private int alphaBeta(GiantBoard giantBoard, int d, int alpha, int beta, boolean isMax) {
        if(d == 0 || giantBoard.isGameOver()) {
            return giantBoard.evaluate(myPiece);
        }
        ArrayList<Move> moves = giantBoard.getPossibleMoves();
        if(moves.isEmpty()){
            return giantBoard.evaluate(myPiece);
        }
        if(isMax){
            int value = Integer.MIN_VALUE;
            for(Move m : moves){
                int[][] backup = giantBoard.copyBoard();
                int row = giantBoard.getActiveLocalRow();
                int col = giantBoard.getActiveLocalCol();
                giantBoard.playMove(m.getRow(), m.getCol(), myPiece);
                value = Math.max(value, alphaBeta(giantBoard, d - 1, alpha, beta, false));
                giantBoard.restoreBoard(backup, row, col);
                alpha = Math.max(alpha, value);
                if(alpha >= beta){
                    break;
                }
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            int opp = (myPiece == 4 ? 2 : 4);
            for(Move m : moves){
                int[][] backup = giantBoard.copyBoard();
                int row = giantBoard.getActiveLocalRow();
                int col = giantBoard.getActiveLocalCol();
                giantBoard.playMove(m.getRow(), m.getCol(), opp);
                value = Math.min(value, alphaBeta(giantBoard, d - 1, alpha, beta, true));
                giantBoard.restoreBoard(backup, row, col);
                beta = Math.min(beta, value);
                if(alpha >= beta){
                    break;
                }
            }
            return value;
        }
    }
}
