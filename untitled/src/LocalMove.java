public class LocalMove {

    
    private int row; // Ligne dans le sous-plateau (0 à 2)
    private int col; // Colonne dans le sous-plateau (0 à 2)

    /**
     * Constructeur pour créer un mouvement local
     * @param row la ligne dans le sous-plateau (0 = haut, 2 = bas)
     * @param col la colonne dans le sous-plateau (0 = gauche, 2 = droite)
     */
    public LocalMove(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return la ligne du mouvement dans le sous-plateau
     */
    public int getRow() {
        return row;
    }

    /**
     * @return la colonne du mouvement dans le sous-plateau
     */
    public int getCol() {
        return col;
    }
}