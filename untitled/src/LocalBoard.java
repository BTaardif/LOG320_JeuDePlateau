import java.util.ArrayList;
import java.util.Arrays;


// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
public class LocalBoard {
    private Mark[][] board;
    private final int TAILLE = 3;
    private String position;
    private Case case_id;
    private Case [][] boardCases;
    
    

    //CONSTRUCTEUR
    // Initialise le plateau
    // Ne pas changer la signature de cette méthode
    public LocalBoard() {
        board = new Mark[TAILLE][TAILLE];
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = Mark.EMPTY;
            }

        }
    }

    public LocalBoard(String[] lettre, String[] nombre) {
        // Initialisation du tableau de cases 3x3 (taille définie par TAILLE)
        boardCases = new Case[TAILLE][TAILLE];
        
        // Parcours de chaque ligne de la grille 3x3
        for (int i = 0; i < TAILLE; i++) {
            
            // Récupère la lettre associée à la ligne courante
            String caseLettre = lettre[GiantBoard.compteur_board_lettre];
            
            // Parcours de chaque colonne de la grille 3x3 pour cette ligne
            for (int j = 0; j < TAILLE; j++) {
                
                // Crée l'identifiant de la case en combinant la lettre (ligne) et le numéro (colonne)
                // Exemple : A1, A2, A3, B1, B2, B3, etc.
                // Le numéro de la colonne est ajusté en utilisant `GiantBoard.compteur_board_number + j`
                this.case_id = new Case(caseLettre + nombre[GiantBoard.compteur_board_number + j], Mark.EMPTY);
                
                // Affecte la nouvelle case à la position (i, j) dans le tableau 3x3
                boardCases[i][j] = case_id;
            }
            
            // Incrémentation de `compteur_board_lettre` pour passer à la lettre suivante (pour la prochaine ligne)
            GiantBoard.compteur_board_lettre++;
        }
    }
    

    // Méthode pour afficher la matrice
    public void afficherBoard() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public Case[][] getBoardCases() {
        return boardCases;
    }

    public void setBoardCase(){
        
    }

    public LocalBoard(char[] flatBoard) {
        board = new Mark[3][3]; // Initialisation de la grille 3x3
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                char cell = flatBoard[i * 3 + j]; // Accéder aux cases 1D
                if (cell == 'X') {
                    board[i][j] = Mark.X;
                } else if (cell == 'O') {
                    board[i][j] = Mark.O;
                } else {
                    board[i][j] = Mark.EMPTY;
                }
            }
        }
    }

    public void printBoard(){
        System.out.println("\nPlateau actuel :");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(getSymbolMark(board[i][j]) + " ");
            }
            System.out.println();
        }
    }

    public String getPosition() {
        return position;
    }

    public Mark getCell(int row, int col) {
        return board[row][col];
    }

    public boolean isFull() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                if (board[i][j] == Mark.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public void humanPlay(String data){

    }

    public boolean isWon(Mark mark) {
        // Vérification des lignes et des colonnes
        for (int i = 0; i < TAILLE; i++) {
            if (board[i][0] == mark && board[i][1] == mark && board[i][2] == mark)
                return true;
            if (board[0][i] == mark && board[1][i] == mark && board[2][i] == mark)
                return true;
        }
    
        // Vérification des diagonales
        if (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark)
            return true;
        if (board[0][2] == mark && board[1][1] == mark && board[2][0] == mark)
            return true;
    
        return false;
    }
    

    public void undoMove(Move m) {
        board[m.getRow()][m.getCol()] = Mark.EMPTY;
    }



    private String getSymbolMark(Mark mark) {
        switch (mark){
            case X :
                return " X ";
            case O :
                return " O ";
            case EMPTY:
                return " . ";
        }
        return null;
    }

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    //
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark){

        if(board[m.getRow()][m.getCol()] == Mark.EMPTY){
            board[m.getRow()][m.getCol()] = mark;
        } else{
            System.out.println("La case est déjà pris");
        }

    }


    // retourne  100 pour une victoire
    //          -100 pour une défaite
    //           0   pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {

        // Vérification des lignes et colonnes
        for (int i = 0; i < TAILLE; i++) {
            if (board[i][0] == mark && board[i][1] == mark && board[i][2] == mark)
                return 100;
            if (board[0][i] == mark && board[1][i] == mark && board[2][i] == mark)
                return 100;
        }

        // Vérification des diagonales
        if (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark)
            return 100;
        if (board[0][2] == mark && board[1][1] == mark && board[2][0] == mark)
            return 100;

        // Vérifier si l'adversaire a gagné
        Mark opponent = (mark == Mark.X) ? Mark.O : Mark.X;
        for (int i = 0; i < TAILLE; i++) {
            if (board[i][0] == opponent && board[i][1] == opponent && board[i][2] == opponent)
                return -100;
            if (board[0][i] == opponent && board[1][i] == opponent && board[2][i] == opponent)
                return -100;
        }
        if (board[0][0] == opponent && board[1][1] == opponent && board[2][2] == opponent)
            return -100;
        if (board[0][2] == opponent && board[1][1] == opponent && board[2][0] == opponent)
            return -100;

        return 0; // Match nul ou jeu en cours
    }
}