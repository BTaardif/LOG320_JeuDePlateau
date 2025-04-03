import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
public class LocalBoard {
    private Mark[][] board;
    private final int TAILLE = 3;
    private String localID;
    private Case [][] boardCases;
    
    

    //CONSTRUCTEUR
    // Initialise le plateau
    // Ne pas changer la signature de cette méthode
    public LocalBoard() {
        board = new Mark[TAILLE][TAILLE];
        boardCases = new Case[TAILLE][TAILLE];
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = Mark.EMPTY;
            }

        }
    }

    public LocalBoard(String[] lettre, String[] nombre, String formattedID) {
        // Initialisation du tableau de cases 3x3 (taille définie par TAILLE)
        boardCases = new Case[TAILLE][TAILLE];
        this.localID = formattedID;
        
        // Parcours de chaque ligne de la grille 3x3
        for (int i = 0; i < TAILLE; i++) {
            
            // Récupère la lettre associée à la ligne courante
            String caseLettre = lettre[GiantBoard.compteur_board_lettre];
            
            // Parcours de chaque colonne de la grille 3x3 pour cette ligne
            for (int j = 0; j < TAILLE; j++) {
                
                // Crée l'identifiant de la case en combinant la lettre (ligne) et le numéro (colonne)
                // Exemple : A1, A2, A3, B1, B2, B3, etc.
                // Le numéro de la colonne est ajusté en utilisant `GiantBoard.compteur_board_number + j`
                // Affecte la nouvelle case à la position (i, j) dans le tableau 3x3

                boardCases[i][j] = new Case(caseLettre + nombre[GiantBoard.compteur_board_number + j], Mark.EMPTY,i,j);
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

    public List<Move> getAvailableMoves(Mark mark, String LocalID) {
    List<Move> availableMoves = new ArrayList<>();

    for (int i = 0; i < TAILLE; i++) {
        for (int j = 0; j < TAILLE; j++) {
            if (boardCases[i][j].getCMark() == Mark.EMPTY) {
                availableMoves.add(new Move(i, j, mark,LocalID,boardCases[i][j].getCase_id())); 
            }
        }
    }
    return availableMoves;
    }

    /* 
    @Override
    public LocalBoard clone() {
        LocalBoard copy = new LocalBoard();
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                copy.boardCases[i][j] = new Case(this.boardCases[i][j]); // Copie chaque case
            }
        }
        return copy;
    }
    */

    
    @Override
    public LocalBoard clone() {
        LocalBoard clonedBoard = new LocalBoard();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                clonedBoard.boardCases[i][j] = new Case(this.boardCases[i][j]);  // Clonage case par case
                //clonedBoard.setLocalID(this.localID);
            }
        }

        return clonedBoard;
    }

    public void applyMove(Move move) {
        int row = move.getRow();
        int col = move.getCol();
        if (boardCases[row][col].getCMark() == Mark.EMPTY) { // Vérifier si la case est vide
            boardCases[row][col].setCMark(move.getMark()); // Appliquer le coup
        }
    }

    public void setBoardCase(){
        
    }

    public String getLocalID() {
        return this.localID;
    }

    public boolean isFull() {
        for (int i = 0; i < TAILLE; i++) {
            for (int j = 0; j < TAILLE; j++) {
                if (boardCases[i][j].getCMark() == Mark.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isWon(Mark mark) {
        // Vérification des lignes et des colonnes
        for (int i = 0; i < TAILLE; i++) {
            if (boardCases[i][0].getCMark() == mark && boardCases[i][1].getCMark() == mark && boardCases[i][2].getCMark() == mark){
                System.out.println("Le joueur X a gagné ce plateau !");
                return true;
            }
                
            if (boardCases[0][i].getCMark() == mark && boardCases[1][i].getCMark() == mark && boardCases[2][i].getCMark() == mark){
                System.out.println("Le joueur X a gagné ce plateau !");
                return true;
            }
                
        }
    
        // Vérification des diagonales
        if (boardCases[0][0].getCMark() == mark && boardCases[1][1].getCMark() == mark && boardCases[2][2].getCMark() == mark){
            System.out.println("Le joueur X a gagné ce plateau !");
            return true;
        }
            
        if (boardCases[0][2].getCMark() == mark && boardCases[1][1].getCMark() == mark && boardCases[2][0].getCMark() == mark){
            System.out.println("Le joueur X a gagné ce plateau !");
            return true;
        }
            
    
        return false;
    }
    
    public Mark getWinner() {
        // Vérifie les lignes et colonnes
        for (int i = 0; i < 3; i++) {
            if (boardCases[i][0].getCMark() != Mark.EMPTY && boardCases[i][0].getCMark() == boardCases[i][1].getCMark() && boardCases[i][1].getCMark() == boardCases[i][2].getCMark()) {
                return boardCases[i][0].getCMark();  // Ligne gagnante
            }
            if (boardCases[0][i].getCMark() != Mark.EMPTY && boardCases[0][i].getCMark() == boardCases[1][i].getCMark() && boardCases[1][i].getCMark() == boardCases[2][i].getCMark()) {
                return boardCases[0][i].getCMark();  // Colonne gagnante
            }
        }
    
        // Vérifie les diagonales
        if (boardCases[0][0].getCMark() != Mark.EMPTY && boardCases[0][0].getCMark() == boardCases[1][1].getCMark() && boardCases[1][1].getCMark() == boardCases[2][2].getCMark()) {
            return boardCases[0][0].getCMark();  // Diagonale \
        }
        if (boardCases[0][2].getCMark() != Mark.EMPTY && boardCases[0][2].getCMark() == boardCases[1][1].getCMark() && boardCases[1][1].getCMark() == boardCases[2][0].getCMark()) {
            return boardCases[0][2].getCMark();  // Diagonale /
        }
    
        return Mark.EMPTY; // Personne n'a encore gagné ce `LocalBoard`
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