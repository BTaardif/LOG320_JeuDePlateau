import java.util.HashMap;

public class test {
    private static final int TAILLE = 3;
    private Mark[][] board;  // Matrice de taille 3x3
    private String position;

    // Enumération des valeurs possibles pour un Mark
    public enum Mark {
        EMPTY, X, O
    }

    // Constructeur pour initialiser le board
    public test(String[] letters, String[] numbers) {
        board = new Mark[TAILLE][TAILLE];
        
        // Parcours de chaque ligne
        for (int i = 0; i < TAILLE; i++) {
            String caseLettre = letters[i];  // Lettre de la ligne (A, B, C, etc.)
            
            // Parcours de chaque colonne
            for (int j = 0; j < TAILLE; j++) {
                // Initialise la case à "EMPTY"
                board[i][j] = Mark.EMPTY;

                // Forme la position sous la forme : lettre + numéro (A1, A2, ..., I9)
                this.position = caseLettre + numbers[j];

                // Pour l'exemple, affichons la position pour chaque case
                System.out.println("Position (" + i + "," + j + ") : " + this.position);
            }
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

    public static void main(String[] args) {
        // Lettres de A à I et chiffres de 9 à 1
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};

        // Créer un HashMap pour stocker les boards
        HashMap<Integer, test> giantBoardList = new HashMap<>();

        // Crée plusieurs LocalBoard et les ajoute à giantBoardList
        for (int i = 0; i < 9; i++) {
            test board = new test(letters, numbers);
            giantBoardList.put(i, board);  // Ajouter chaque LocalBoard dans le HashMap
        }

        // Exemple d'affichage du HashMap avec un board
        giantBoardList.get(0).afficherBoard(); // Affiche le premier LocalBoard
    }
}

