import java.util.ArrayList;

public class LocalBoard {
    // Constantes représentant les pièces.
    public static final int EMPTY = 0; // Case vide
    public static final int O = 2; // Pièce O
    public static final int X = 4; // Pièce X

    private int[][] board; // Le plateau de jeu 3x3

    // Constructeur par défaut initialise un plateau 3x3 avec toutes les cases
    // vides.
    public LocalBoard() {
        board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    // Constructeur de copie pour créer une copie profonde du plateau.
    public LocalBoard(LocalBoard other) {
        board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = other.board[i][j];
            }
        }
    }

    // Joue un coup sur le plateau.
    public void play(LocalMove m, int piece) {
        if (isValidMove(m)) {
            board[m.getRow()][m.getCol()] = piece;
        }
    }

    // Vérifie si un coup est valide (dans les limites et sur une case vide).
    public boolean isValidMove(LocalMove m) {
        int r = m.getRow();
        int c = m.getCol();
        return r >= 0 && r < 3 && c >= 0 && c < 3 && board[r][c] == EMPTY;
    }

    // Retourne la pièce à une case donnée.
    public int getCell(int r, int c) {
        return board[r][c];
    }

    // Retourne 100 pour une victoire, -100 pour une défaite, 0 pour nul/en cours.
    public int evaluate(int piece) {
        int opponent = (piece == X) ? O : X;

        // Vérifie les lignes, colonnes et diagonales pour une victoire/défaite
        if (checkWinner(piece))
            return 100;
        if (checkWinner(opponent))
            return -100;

        return 0;
    }

    // Attribue des points en fonction des lignes potentiellement gagnantes.
    public int heuristicEvaluate(int piece) {
        int score = 0;
        int opponent = (piece == X) ? O : X;

        if (checkWinner(piece))
            return 1000; // Score élevé pour gagner le plateau local
        if (checkWinner(opponent))
            return -1000; // Pénalité élevée pour la perte

        // Évalue les lignes (lignes, colonnes, diagonales)
        score += evaluateLine(0, 0, 0, 1, 0, 2, piece); // Ligne 0
        score += evaluateLine(1, 0, 1, 1, 1, 2, piece); // Ligne 1
        score += evaluateLine(2, 0, 2, 1, 2, 2, piece); // Ligne 2
        score += evaluateLine(0, 0, 1, 0, 2, 0, piece); // Colonne 0
        score += evaluateLine(0, 1, 1, 1, 2, 1, piece); // Colonne 1
        score += evaluateLine(0, 2, 1, 2, 2, 2, piece); // Colonne 2
        score += evaluateLine(0, 0, 1, 1, 2, 2, piece); // Diagonale 1
        score += evaluateLine(0, 2, 1, 1, 2, 0, piece); // Diagonale 2

        // Ajoute un petit bonus pour le contrôle du centre
        if (board[1][1] == piece)
            score += 1;
        else if (board[1][1] == opponent)
            score -= 1;

        // Vérifie un match nul si le plateau est plein et personne n'a gagné localement
        if (isFull() && !checkWinner(piece) && !checkWinner(opponent))
            return 0; // Match nul local

        return score;
    }

    // Aide pour heuristicEvaluate : évalue une seule ligne (ligne, col, ou diag).
    private int evaluateLine(int r1, int c1, int r2, int c2, int r3, int c3, int piece) {
        int score = 0;
        int opponent = (piece == X) ? O : X;

        // Récupère les pièces dans la ligne
        int p1 = board[r1][c1];
        int p2 = board[r2][c2];
        int p3 = board[r3][c3];

        // Vérifie les opportunités du joueur (2 pièces alignées avec une case vide)
        if ((p1 == piece && p2 == piece && p3 == EMPTY) ||
                (p1 == piece && p2 == EMPTY && p3 == piece) ||
                (p1 == EMPTY && p2 == piece && p3 == piece)) {
            score = 10;
        }
        // Vérifie les opportunités du joueur (1 pièce alignée avec deux cases vides)
        else if ((p1 == piece && p2 == EMPTY && p3 == EMPTY) ||
                (p1 == EMPTY && p2 == piece && p3 == EMPTY) ||
                (p1 == EMPTY && p2 == EMPTY && p3 == piece)) {
            score = 1;
        }

        // Vérifie les opportunités de l'adversaire (2 pièces alignées avec une case
        // vide) - score négatif
        if ((p1 == opponent && p2 == opponent && p3 == EMPTY) ||
                (p1 == opponent && p2 == EMPTY && p3 == opponent) ||
                (p1 == EMPTY && p2 == opponent && p3 == opponent)) {
            score = -10; // Priorité plus élevée pour bloquer l'adversaire que pour avancer soi-même
        }
        // Vérifie les opportunités de l'adversaire (1 pièce alignée avec deux cases
        // vides) - score négatif
        else if ((p1 == opponent && p2 == EMPTY && p3 == EMPTY) ||
                (p1 == EMPTY && p2 == opponent && p3 == EMPTY) ||
                (p1 == EMPTY && p2 == EMPTY && p3 == opponent)) {
            // Seulement si le score n'est pas déjà positif (ne pas écraser une opportunité
            // pour nous)
            if (score <= 0)
                score = -1;
        }

        return score;
    }

    // Vérifie si la pièce donnée a gagné.
    public boolean checkWinner(int piece) {
        // Vérifie les lignes et les colonnes.
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == piece && board[i][1] == piece && board[i][2] == piece)
                return true;
            if (board[0][i] == piece && board[1][i] == piece && board[2][i] == piece)
                return true;
        }
        // Vérifie les diagonales.
        if (board[0][0] == piece && board[1][1] == piece && board[2][2] == piece)
            return true;
        if (board[0][2] == piece && board[1][1] == piece && board[2][0] == piece)
            return true;

        return false; // Aucun gagnant
    }

    // Vérifie si le plateau est plein.
    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    return false; // Trouvé une case vide
                }
            }
        }
        return true; // Aucune case vide trouvée
    }

    // Retourne une liste de tous les coups possibles (cases vides).
    public ArrayList<LocalMove> getPossibleMoves() {
        ArrayList<LocalMove> moves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    moves.add(new LocalMove(i, j));
                }
            }
        }
        return moves;
    }
}