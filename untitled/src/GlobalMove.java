import java.util.ArrayList;

public class GlobalMove {

    // Coordonnées du sous-plateau dans le plateau principal (0 à 2)
    private int globalRow;
    private int globalCol;
    
    // Coordonnées de la case dans le sous-plateau (0 à 2)
    private int localRow;
    private int localCol;

    /**
     * Constructeur pour créer un mouvement global complet
     * @param globalRow La ligne du sous-plateau dans la grille principale
     * @param globalCol La colonne du sous-plateau dans la grille principale
     * @param localRow La ligne dans le sous-plateau sélectionné
     * @param localCol La colonne dans le sous-plateau sélectionné
     */
    public GlobalMove(int globalRow, int globalCol, int localRow, int localCol) {
        this.globalRow = globalRow;
        this.globalCol = globalCol;
        this.localRow = localRow;
        this.localCol = localCol;
    }

    /**
     * @return La ligne du sous-plateau dans la grille principale
     */
    public int getGlobalRow() {
        return globalRow;
    }

    /**
     * @return La colonne du sous-plateau dans la grille principale
     */
    public int getGlobalCol() {
        return globalCol;
    }

    /**
     * @return La ligne dans le sous-plateau sélectionné
     */
    public int getLocalRow() {
        return localRow;
    }

    /**
     * @return La colonne dans le sous-plateau sélectionné
     */
    public int getLocalCol() {
        return localCol;
    }

    /**
     * Représentation textuelle du mouvement
     * @return Une chaîne formatée montrant les coordonnées globales et locales
     */
    @Override
    public String toString() {
        return "GlobalMove [Global=(" + globalRow + ", " + globalCol + "), Local=(" + localRow + ", " + localCol + ")]";
    }
}