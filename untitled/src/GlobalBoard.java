import java.util.ArrayList;

public class GlobalBoard {
    // Rendre `boards` public comme demandé par l'accès direct dans Client.java
    public LocalBoard[][] boards;
    private int[][] winners; // Stocke LocalBoard.X, LocalBoard.O, ou LocalBoard.EMPTY

    // --- Constantes pour l'heuristique ---
    private static final int GLOBAL_WIN_SCORE = 100000; // Score pour une victoire globale
    private static final int LOCAL_WIN_SCORE = 100; // Score de base pour gagner un plateau local
    private static final int GLOBAL_TWO_IN_ROW_SCORE = 500; // Score pour 2 plateaux locaux gagnés alignés globalement

    // Poids stratégiques pour chaque plateau local (centre > coins > côtés)
    private static final int[][] BOARD_WEIGHTS = {
            { 3, 2, 3 },
            { 2, 4, 2 },
            { 3, 2, 3 }
    };

    public GlobalBoard() {
        boards = new LocalBoard[3][3];
        winners = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boards[i][j] = new LocalBoard();
                winners[i][j] = LocalBoard.EMPTY;
            }
        }
    }

    // Constructeur de copie
    public GlobalBoard(GlobalBoard other) {
        boards = new LocalBoard[3][3];
        winners = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boards[i][j] = new LocalBoard(other.boards[i][j]);
                winners[i][j] = other.winners[i][j];
            }
        }
    }

    // Joue un coup sur le plateau local spécifié.
    public boolean play(GlobalMove move, int mark) {
        int gRow = move.getGlobalRow();
        int gCol = move.getGlobalCol();
        int lRow = move.getLocalRow();
        int lCol = move.getLocalCol();

        // Vérifie si le plateau local ciblé est déjà fermé ou si le coup y est invalide
        if (winners[gRow][gCol] != LocalBoard.EMPTY || !boards[gRow][gCol].isValidMove(new LocalMove(lRow, lCol))) {
            return false;
        }

        // Joue le coup sur le plateau local.
        LocalBoard localBoard = boards[gRow][gCol];
        localBoard.play(new LocalMove(lRow, lCol), mark);

        // Met à jour le gagnant du plateau local si nécessaire.
        if (localBoard.checkWinner(mark)) {
            winners[gRow][gCol] = mark;
        } else if (localBoard.checkWinner((mark == LocalBoard.X) ? LocalBoard.O : LocalBoard.X)) {
            winners[gRow][gCol] = (mark == LocalBoard.X) ? LocalBoard.O : LocalBoard.X; // L'adversaire a gagné
        } else if (localBoard.isFull()) {
            winners[gRow][gCol] = LocalBoard.EMPTY; // Marque comme nul (non gagné) si plein sans vainqueur
        }
        // Sinon, le plateau local est toujours en cours, le gagnant reste EMPTY.

        return true;
    }

    // Renvoie true si le plateau local à (gRow, gCol) est fermé (gagné ou
    // plein/nul).
    public boolean isLocalBoardClosed(int gRow, int gCol) {
        // Un plateau est fermé s'il a un gagnant OU s'il est plein (plus de coups
        // possibles)
        return winners[gRow][gCol] != LocalBoard.EMPTY || boards[gRow][gCol].isFull();
    }

    // Vérifie une victoire globale en examinant le tableau des gagnants locaux.
    public int checkGlobalWinner() {
        // Vérifie les lignes
        for (int i = 0; i < 3; i++) {
            if (winners[i][0] != LocalBoard.EMPTY && winners[i][0] == winners[i][1] && winners[i][1] == winners[i][2])
                return winners[i][0];
        }
        // Vérifie les colonnes
        for (int i = 0; i < 3; i++) {
            if (winners[0][i] != LocalBoard.EMPTY && winners[0][i] == winners[1][i] && winners[1][i] == winners[2][i])
                return winners[0][i];
        }
        // Vérifie les diagonales
        if (winners[0][0] != LocalBoard.EMPTY && winners[0][0] == winners[1][1] && winners[1][1] == winners[2][2])
            return winners[0][0];
        if (winners[0][2] != LocalBoard.EMPTY && winners[0][2] == winners[1][1] && winners[1][1] == winners[2][0])
            return winners[0][2];

        return LocalBoard.EMPTY; // Pas encore de gagnant global
    }

    // Génère les coups possibles en fonction du dernier coup joué.
    // `lastMove` détermine le plateau requis (forcedGlobalRow, forcedGlobalCol).
    public ArrayList<GlobalMove> getPossibleMoves(GlobalMove lastMove) {
        ArrayList<GlobalMove> moves = new ArrayList<>();
        int forcedGlobalRow = -1;
        int forcedGlobalCol = -1;

        // Détermine le plateau requis basé sur la position locale du dernier coup
        // adverse
        if (lastMove != null) {
            forcedGlobalRow = lastMove.getLocalRow();
            forcedGlobalCol = lastMove.getLocalCol();
        }

        // Cas 1 : Premier coup de la partie (lastMove est null) OU le plateau forcé est
        // fermé
        if (lastMove == null || isLocalBoardClosed(forcedGlobalRow, forcedGlobalCol)) {
            // Autorise les coups dans N'IMPORTE QUEL plateau non fermé
            for (int gRow = 0; gRow < 3; gRow++) {
                for (int gCol = 0; gCol < 3; gCol++) {
                    if (!isLocalBoardClosed(gRow, gCol)) {
                        LocalBoard localBoard = boards[gRow][gCol];
                        for (LocalMove m : localBoard.getPossibleMoves()) {
                            moves.add(new GlobalMove(gRow, gCol, m.getRow(), m.getCol()));
                        }
                    }
                }
            }
        }
        // Cas 2 : Le plateau forcé est ouvert
        else {
            LocalBoard localBoard = boards[forcedGlobalRow][forcedGlobalCol];
            for (LocalMove m : localBoard.getPossibleMoves()) {
                moves.add(new GlobalMove(forcedGlobalRow, forcedGlobalCol, m.getRow(), m.getCol()));
            }
        }
        return moves;
    }

    // Fonction d'évaluation globale améliorée intégrant l'heuristique locale et la
    // stratégie globale.
    public int evaluateGlobal(int mark) {
        int adversary = (mark == LocalBoard.X) ? LocalBoard.O : LocalBoard.X;
        int globalScore = 0;

        // 1. Vérifier Victoire/Défaite Globale immédiate (plus haute priorité)
        int globalWinner = checkGlobalWinner();
        if (globalWinner == mark)
            return GLOBAL_WIN_SCORE;
        if (globalWinner == adversary)
            return -GLOBAL_WIN_SCORE;

        // 2. Évaluer basé sur l'état des plateaux locaux et le positionnement global
        for (int gRow = 0; gRow < 3; gRow++) {
            for (int gCol = 0; gCol < 3; gCol++) {
                int weight = BOARD_WEIGHTS[gRow][gCol]; // Poids stratégique de ce plateau
                if (winners[gRow][gCol] == mark) {
                    globalScore += LOCAL_WIN_SCORE * weight;
                } else if (winners[gRow][gCol] == adversary) {
                    globalScore -= LOCAL_WIN_SCORE * weight;
                } else {
                    // Si le plateau n'est pas fermé, ajouter sa valeur heuristique, pondérée
                    if (!isLocalBoardClosed(gRow, gCol)) {
                        // Utiliser l'évaluation heuristique détaillée pour les plateaux en cours
                        globalScore += boards[gRow][gCol].heuristicEvaluate(mark) * weight;
                    }
                }
            }
        }

        // 3. Ajouter bonus/pénalité pour les victoires globales potentielles (2
        // plateaux gagnés alignés)
        globalScore += checkGlobalTwoInRow(mark) * GLOBAL_TWO_IN_ROW_SCORE;
        globalScore -= checkGlobalTwoInRow(adversary) * GLOBAL_TWO_IN_ROW_SCORE;

        // 4. Vérifier si la partie entière est nulle (tous plateaux fermés, pas de
        // gagnant global)
        if (isGlobalBoardFull() && globalWinner == LocalBoard.EMPTY) {
            return 0; // Égalité Globale
        }

        return globalScore;
    }

    // Vérifie si le plateau global est plein (tous les plateaux locaux sont
    // fermés).
    public boolean isGlobalBoardFull() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (!isLocalBoardClosed(r, c)) {
                    return false;
                }
            }
        }
        return true; // Tous les plateaux sont fermés
    }

    // Compte les victoires globales potentielles (2 plateaux gagnés alignés avec le
    // 3ème ouvert).
    private int checkGlobalTwoInRow(int piece) {
        int count = 0;
        // Vérifie les lignes
        for (int r = 0; r < 3; r++) {
            if (winners[r][0] == piece && winners[r][1] == piece && !isLocalBoardClosed(r, 2)
                    && winners[r][2] == LocalBoard.EMPTY)
                count++;
            if (winners[r][0] == piece && winners[r][2] == piece && !isLocalBoardClosed(r, 1)
                    && winners[r][1] == LocalBoard.EMPTY)
                count++;
            if (winners[r][1] == piece && winners[r][2] == piece && !isLocalBoardClosed(r, 0)
                    && winners[r][0] == LocalBoard.EMPTY)
                count++;
        }
        // Vérifie les colonnes
        for (int c = 0; c < 3; c++) {
            if (winners[0][c] == piece && winners[1][c] == piece && !isLocalBoardClosed(2, c)
                    && winners[2][c] == LocalBoard.EMPTY)
                count++;
            if (winners[0][c] == piece && winners[2][c] == piece && !isLocalBoardClosed(1, c)
                    && winners[1][c] == LocalBoard.EMPTY)
                count++;
            if (winners[1][c] == piece && winners[2][c] == piece && !isLocalBoardClosed(0, c)
                    && winners[0][c] == LocalBoard.EMPTY)
                count++;
        }
        // Vérifie les diagonales
        if (winners[0][0] == piece && winners[1][1] == piece && !isLocalBoardClosed(2, 2)
                && winners[2][2] == LocalBoard.EMPTY)
            count++;
        if (winners[0][0] == piece && winners[2][2] == piece && !isLocalBoardClosed(1, 1)
                && winners[1][1] == LocalBoard.EMPTY)
            count++;
        if (winners[1][1] == piece && winners[2][2] == piece && !isLocalBoardClosed(0, 0)
                && winners[0][0] == LocalBoard.EMPTY)
            count++;

        if (winners[0][2] == piece && winners[1][1] == piece && !isLocalBoardClosed(2, 0)
                && winners[2][0] == LocalBoard.EMPTY)
            count++;
        if (winners[0][2] == piece && winners[2][0] == piece && !isLocalBoardClosed(1, 1)
                && winners[1][1] == LocalBoard.EMPTY)
            count++;
        if (winners[1][1] == piece && winners[2][0] == piece && !isLocalBoardClosed(0, 2)
                && winners[0][2] == LocalBoard.EMPTY)
            count++;

        return count;
    }

    // Met à jour le tableau des gagnants locaux après l'analyse complète du plateau
    // depuis le serveur.
    public void updateAllLocalWinners() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (winners[r][c] == LocalBoard.EMPTY) { // Mettre à jour seulement si non déjà décidé
                    if (boards[r][c].checkWinner(LocalBoard.X)) {
                        winners[r][c] = LocalBoard.X;
                    } else if (boards[r][c].checkWinner(LocalBoard.O)) {
                        winners[r][c] = LocalBoard.O;
                    }
                }
            }
        }
    }

    /** Renvoie la pièce sur une case spécifique d'un plateau local. */
    public int getLocalBoardPiece(int globalRow, int globalCol, int localRow, int localCol) {
        // Vérification des limites pourrait être ajoutée ici
        return boards[globalRow][globalCol].getCell(localRow, localCol);
    }

    /** Renvoie le gagnant (X, O, ou EMPTY) du plateau local spécifié. */
    public int getLocalWinner(int globalRow, int globalCol) {
        // Vérification des limites pourrait être ajoutée ici
        return winners[globalRow][globalCol];
    }

}