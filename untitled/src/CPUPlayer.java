import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class CPUPlayer {

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;

    protected Mark CPU_AI;
    protected static Mark CPU_Player = Mark.EMPTY;




    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu){
        CPU_AI = cpu;
    }

    // Ne pas changer cette méthode
    public int  getNumOfExploredNodes(){

        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(LocalBoard board) {
        ArrayList<Move> bestMoves = new ArrayList<>();
        numExploredNodes = 0;

        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board.getCell(i, j) == Mark.EMPTY) {
                    Move move = new Move(i, j);
                    board.play(move, CPU_AI); // Coup temporaire
                    int score = minimax(board, false); // Qui retourne une évaluation du plateau après ce coup
                    board.undoMove(move); // Annuler le coup


                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear();
                        bestMoves.add(move);
                    } else if (score == bestScore) {
                        bestMoves.add(move);
                    }
                }
            }
        }

        return bestMoves;
    }


    private int minimax(LocalBoard board, boolean isMaximizing) {
        numExploredNodes++;
        //System.out.println("Noeuds explorés avec Minimax : " + numExploredNodes);


        int score = board.evaluate(CPU_AI);
        if (score == 100 || score == -100 || board.isFull()) {
            return score;
        }

        //Si c'est le tour du joueur MAX (l'IA) :
        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.getCell(i, j) == Mark.EMPTY) {
                        Move move = new Move(i, j);
                        board.play(move, CPU_AI);
                        bestScore = Math.max(bestScore, minimax(board, false));
                        board.undoMove(move); // Annuler le coup

                    }
                }
            }
            return bestScore;

            //Si c'est le tour du joueur MIN (l'adversaire) :
        } else {
            int bestScore = Integer.MAX_VALUE;
            Mark opponent = (CPU_AI == Mark.X) ? Mark.O : Mark.X;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.getCell(i, j) == Mark.EMPTY) {
                        Move move = new Move(i, j);
                        board.play(move, opponent);
                        bestScore = Math.min(bestScore, minimax(board, true));
                        board.undoMove(move); // Annuler le coup

                    }
                }
            }
            return bestScore;
        }
    }


    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    private int getNextMoveAB(LocalBoard board, boolean isMaximizing, int alpha, int beta) {
        numExploredNodes++;

        int score = board.evaluate(CPU_AI);
        if (score == 100 || score == -100 || board.isFull()) {
            return score;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.getCell(i, j) == Mark.EMPTY) {
                        Move move = new Move(i, j);
                        board.play(move, CPU_AI);
                        bestScore = Math.max(bestScore, getNextMoveAB(board, false, alpha, beta));
                        board.undoMove(move);
                        alpha = Math.max(alpha, bestScore);
                        if (alpha >= beta) {
                            return bestScore;
                        }
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            Mark opponent = (CPU_AI == Mark.X) ? Mark.O : Mark.X;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.getCell(i, j) == Mark.EMPTY) {
                        Move move = new Move(i, j);
                        board.play(move, opponent);
                        bestScore = Math.min(bestScore, getNextMoveAB(board, true, alpha, beta));
                        board.undoMove(move);
                        beta = Math.min(beta, bestScore);
                        if (alpha >= beta) {
                            return bestScore;
                        }
                    }
                }
            }
            return bestScore;
        }
    }
    public ArrayList<Move> getNextMoveAB(LocalBoard board) {
        ArrayList<Move> bestMoves = new ArrayList<>();
        numExploredNodes = 0;
    
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
    
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board.getCell(i, j) == Mark.EMPTY) {
                    Move move = new Move(i, j);
                    board.play(move, CPU_AI); // Coup temporaire
                    int score = getNextMoveAB(board, false, alpha, beta); // Appel récursif avec Alpha-Bêta
                    board.undoMove(move); // Annuler le coup
    
                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear();
                        bestMoves.add(move);
                    } else if (score == bestScore) {
                        bestMoves.add(move);
                    }
                }
            }
        }
    
        return bestMoves;
    }
    
    public Move getBestMove(GiantBoard board, int depth) {
        ArrayList<Move> bestMoves = getNextMoveAB(board.getBestLocalBoard()); // Appelle la fonction Alpha-Bêta
        if (!bestMoves.isEmpty()) {
            return bestMoves.get(0); // Choisit le premier coup parmi les meilleurs
        }
        return null; // Aucun coup possible
    }
    

    public Mark getMark() {
        return this.CPU_AI;
    }
}
