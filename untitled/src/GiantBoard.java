import java.util.ArrayList;
import java.util.HashMap;

public class GiantBoard {
    private int[][] board;
    private int activeLocalRow;
    private int activeLocalCol;
    private HashMap <String, LocalBoard> giantBoard_list;
    private LocalBoard miniBLocalBoard;
    protected static int compteur_board_lettre = 0;
    protected static int compteur_board_number =0;
    private int id_localBoard = 0;

    public GiantBoard() {
        board = new int[9][9];
        activeLocalRow = -1;
        activeLocalCol = -1;
        this.giantBoard_list = new HashMap<>();
        
            
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

    public void setminiBoardTac(String[] letters, String[] numbers){
        
    // Initialisation des variables pour les coordonnées des sous-plateaux
    int ligne = 0;
    int colonne = 0;
    
    // La boucle qui va créer 9 sous-plateaux (3x3)
    for (int i = 0; i < 9; i++) {
        
        // Création de l'ID pour chaque LocalBoard en utilisant la ligne et la colonne
        // On concatène "ligne" et "colonne" pour former un identifiant comme "00", "01", etc.
        String formattedID = Integer.toString(ligne) + Integer.toString(colonne);
        
        // Création d'un nouveau LocalBoard avec les lettres et les numéros passés en paramètres
        LocalBoard board = new LocalBoard(letters, numbers);
        
        // Ajout du LocalBoard créé dans la liste géante (giantBoard_list)
        giantBoard_list.put(formattedID, board);  
        
        // Incrémentation de la colonne pour passer à la suivante A,B,C,...G,H,I
        colonne++;

        // Cette partie remet à zéro le compteur des lettres => On recommence à A,B,C..
        if (compteur_board_lettre % 9 == 0) {
            compteur_board_lettre = 0;
        }
        
        // Lorsqu'on a atteint trois sous-plateaux (en largeur), on passe à la ligne suivante (d'ou le +3)
        //
        // On incrémente "ligne" et réinitialise "colonne" à 0 pour commencer un nouveau plateau
        if (giantBoard_list.size() % 3 == 0) {
            compteur_board_number += 3;
            ligne++;
            colonne = 0;
        }
    }

        /* 
        for (int i = 0; i < giantBoard_list.size(); i++) {
            for (int j = 0; j < giantBoard_list.size(); j++) {
                //board[i][j] = new LocalBoard(letters[j],numbers[i]);
                //this.board = new LocalBoard(letters[j],numbers[i]);
                miniBLocalBoard = new LocalBoard(letters[j],numbers[i]);
                this.giantBoard_list.put(compteur_board,miniBLocalBoard);
            }
        }

            for (int ligne = 0; ligne < 3; ligne++) {  // Trois lignes de mini-plateaux
            for (int colonne = 0; colonne < 3; colonne++) {  // Trois colonnes de mini-plateaux
                String formattedID = String.format("%02d", ligne * 3 + colonne);  // ID formaté en "00", "01", ...
                LocalBoard board = new LocalBoard(letters, numbers, ligne, colonne);
                giantBoard_list.put(formattedID, board);  // Ajouter chaque LocalBoard dans le HashMap
            }
        }

            */


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

    public void setCaseHuman(String caase){
        
    }

    public HashMap getPlateaux(){
        return giantBoard_list;
    }

    public void afficherTiLocalBoard() {
        
        System.out.println("GIANTBOARD TIC TAC TOE");
        System.out.println("====================================");
        
        // Parcourir chaque LocalBoard dans giantBoard_list
        for (String id : giantBoard_list.keySet()) {
            LocalBoard localBoard = giantBoard_list.get(id);
    
            if (localBoard != null) {
                System.err.println("Plateau ID: " + id);
    
                // Récupérer toutes les cases du LocalBoard
                Case[][] cases = localBoard.getBoardCases(); 
    
                // Parcourir les cases et afficher leurs informations
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        Case c = cases[i][j];
                        System.out.println("    Case ID: " + c.getCase_id() + " | Mark: " + c.getCMark());
                    }
                }
            }
        }
    }
    
    public void getMiniTiLocalBoard(String data, int piece) {

        // Parcourir chaque LocalBoard dans giantBoard_list
        for (String id : giantBoard_list.keySet()) {
            LocalBoard localBoard = giantBoard_list.get(id);
    
            if (localBoard != null) {
    
                // Récupérer toutes les cases du LocalBoard
                Case[][] cases = localBoard.getBoardCases(); 
    
                // Parcourir les cases et afficher leurs informations
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        
                        if (cases[i][j].getCase_id().equals(data.trim())){
                            cases[i][j].setCMark(Mark.X);
                            return;
                        }
                    }
                }


            }
        }
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
