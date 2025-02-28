import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board
{
    private Mark[][] board;

    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Mark[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }
    }

    public Board(Board other) {  // Copy constructor
        this.board = new Mark[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.board[i][j] = other.board[i][j];  // Copy each cell
            }
        }
    }

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    //
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark){
        if (board[m.getRow()][m.getCol()] == Mark.EMPTY) {
            board[m.getRow()][m.getCol()] = mark;
        }
    }


    // retourne  100 pour une victoire
    //          -100 pour une défaite
    //           0   pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark){
        Mark adversaire = (mark == Mark.X) ? Mark.O : Mark.X;

        // Verification des lignes et colonnes
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                if (board[i][0] == mark) return 100;
                if (board[i][0] == adversaire) return -100;
            }
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                if (board[0][i] == mark) return 100;
                if (board[0][i] == adversaire) return -100;
            }
        }

        // Verification des diagonales
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == mark) return 100;
            if (board[0][0] == adversaire) return -100;
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == mark) return 100;
            if (board[0][2] == adversaire) return -100;
        }

        // Verifications d'autres coups ou si la partie n'est pas terminée
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[i][j] == Mark.EMPTY)
                    return 0;

        return 0; // Match nul
    }

    // Retourne les coups possibles
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[i][j] == Mark.EMPTY)
                    moves.add(new Move(i, j));
        return moves;
    }
}