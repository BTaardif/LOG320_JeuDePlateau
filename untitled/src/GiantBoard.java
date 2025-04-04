import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GiantBoard {
    private int[][] board;
    private int activeLocalRow;
    private int activeLocalCol;
    private HashMap <String, LocalBoard> giantBoard_list;
    private LocalBoard miniBLocalBoard;
    protected static int compteur_board_lettre = 0;
    protected static int compteur_board_number =0;
    private int id_localBoard = 0;
    private GiantBoard copyGiantBoard;

    public GiantBoard() {
        this.giantBoard_list = new HashMap<>();
        
            
    }

    public void setminiBoardTac(String[] letters, String[] numbers){
        
        // Initialisation des variables pour les coordonnées des sous-plateaux
        int ligne = 0;
        int colonne = 0;
        
        // La boucle qui va créer 9 sous-plateaux (3x3)
        for (int i = 0; i < 9; i++) {
            
            // Création de l'ID pour chaque LocalBoard en utilisant la ligne et la colonne
            // On concatène "ligne" et "colonne" pour former un identifiant comme "00", "01", etc.
            String formattedID = Integer.toString(colonne) + Integer.toString(ligne);
            
            // Création d'un nouveau LocalBoard avec les lettres et les numéros passés en paramètres
            LocalBoard board = new LocalBoard(letters, numbers, formattedID);
            
            // Ajout du LocalBoard créé dans la liste géante (giantBoard_list)
            giantBoard_list.put(formattedID, board);  
            
            // Incrémentation de la colonne pour passer à la suivante A,B,C,...G,H,I
            colonne++;

            // Cette partie remet à zéro le compteur des lettres => On recommence à A,B,C..
            if (compteur_board_lettre % 9 == 0) {
                compteur_board_lettre = 0;
            }
            
            // Lorsqu'on a atteint trois sous-plateaux (en largeur), on passe à la ligne suivante (d'ou le +3)
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

    

    public HashMap<String, LocalBoard> getPlateaux(){
        return giantBoard_list;
    }

    /* 
    @Override
    public GiantBoard clone() {
        copyGiantBoard = new GiantBoard();  // Crée un nouveau plateau géant
        for (String key : this.giantBoard_list.keySet()) {  
            copyGiantBoard.giantBoard_list.put(key, this.giantBoard_list.get(key));  // Clone chaque LocalBoard
        }
        return copyGiantBoard;
    }
    */

    @Override
    public GiantBoard clone() {
        GiantBoard clonedGiantBoard = new GiantBoard(); // Nouveau plateau géant
        clonedGiantBoard.giantBoard_list = new HashMap<>();

        // Copier tous les LocalBoards
        for (Map.Entry<String, LocalBoard> entry : this.getPlateaux().entrySet()) {
            LocalBoard originalLocalBoard = entry.getValue();
            LocalBoard clonedLocalBoard = originalLocalBoard.clone();
            clonedGiantBoard.getPlateaux().put(entry.getKey(), clonedLocalBoard);
        }

        return clonedGiantBoard;
    }


    public void applyMove(GiantBoard nexBoard, Move move) {
        LocalBoard targetBoard = this.getPlateaux().get(move.getLocalBoardId()); // Trouver le LocalBoard
        if (targetBoard != null) {
            targetBoard.applyMove(move);  // Appliquer le coup sur ce plateau
        }
    }
    
    public HashMap<String, LocalBoard> HashMapclone() {
        GiantBoard copy = new GiantBoard();  // Crée un nouveau plateau géant
        for (String key : this.giantBoard_list.keySet()) {  
            copy.giantBoard_list.put(key, this.giantBoard_list.get(key));  // Clone chaque LocalBoard
        }
        return copy.giantBoard_list;
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
    
    public void setGiantBoard(String data,int piece) {

        // Parcourir chaque LocalBoard dans giantBoard_list
        for (String id : giantBoard_list.keySet()) {
            LocalBoard localBoard = giantBoard_list.get(id);
    
            if (localBoard != null) {
    
                // Récupérer toutes les cases du LocalBoard
                Case[][] cases = localBoard.getBoardCases(); 
    
                // Parcourir les cases et placer la bonne marque dans la bonne case
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        
                        if (cases[i][j].getCase_id().equals(data.trim()) && piece ==4){
                            cases[i][j].setCMark(Mark.X);
                            return;
                        }

                        if (cases[i][j].getCase_id().equals(data.trim()) && piece ==2){
                            cases[i][j].setCMark(Mark.O);
                            return;
                        }
                    }
                }


            }
        }
    }

    public String rechercheMiniBoard(String data) {

        // Parcourir chaque LocalBoard dans giantBoard_list
        for (String id : giantBoard_list.keySet()) {
            LocalBoard localBoard = giantBoard_list.get(id);
    
            if (localBoard != null) {
    
                // Récupérer toutes les cases du LocalBoard
                Case[][] cases = localBoard.getBoardCases(); 
    
                // Parcourir les cases et placer la bonne marque dans la bonne case
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        
                        if (cases[i][j].getCase_id().equals(data.trim())){
                            return Integer.toString(cases[i][j].getIndex_X()) + Integer.toString(cases[i][j].getIndex_Y());
                        }
                    }
                }


            }
        }

        return null;
    }



  public boolean checkWin(Mark player) {
        // Vérifie si un joueur a gagné le GiantBoard en ayant gagné 3 LocalBoards alignés
        String[][] grid = new String[3][3]; // Stocke l'état de chaque LocalBoard

        for (Map.Entry<String, LocalBoard> entry : giantBoard_list.entrySet()) {
            int boardX = Character.getNumericValue(entry.getKey().charAt(1));
            int boardY = Character.getNumericValue(entry.getKey().charAt(0));
            Mark winner = entry.getValue().getWinner(); // Suppose que LocalBoard a getWinner()
            
            if (winner == player) {
                grid[boardX][boardY] = player.toString();
            } else {
                grid[boardX][boardY] = "-"; // Aucun gagnant dans ce LocalBoard
            }
        }

        // Vérifie les lignes, colonnes et diagonales
        for (int i = 0; i < 3; i++) {
            if (grid[i][0].equals(grid[i][1]) && grid[i][1].equals(grid[i][2]) && !grid[i][0].equals("-")) return true;
            if (grid[0][i].equals(grid[1][i]) && grid[1][i].equals(grid[2][i]) && !grid[0][i].equals("-")) return true;
        }
        if (grid[0][0].equals(grid[1][1]) && grid[1][1].equals(grid[2][2]) && !grid[0][0].equals("-")) return true;
        if (grid[0][2].equals(grid[1][1]) && grid[1][1].equals(grid[2][0]) && !grid[0][2].equals("-")) return true;

        return false;
    }

    public boolean isFull() {
        for (LocalBoard lb : giantBoard_list.values()) {
            if (!lb.isFull()) {  // Suppose que LocalBoard a isFull()
                return false;
            }
    }
        return true;
    }
}
