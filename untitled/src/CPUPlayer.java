import java.util.ArrayList;
import java.util.Collections;

/**
 * CPU PLAYER
 * 
 * Classe qui sert à trouver le meilleur coup à jouer.
 */


public class CPUPlayer {

/*
 * Variabkes
 * */    
    private int aiMark; 
    private int opponentMark;
    private long startTime;
    private final long timeLimitMillis = 2900; // Voir la classe Client. Il faut se garder un peu de temps pour capter les erreurs.

/*
 * Constructor
 *  */    
    public CPUPlayer(int aiMark) {
        this.aiMark = aiMark;
        this.opponentMark = (aiMark == LocalBoard.X) ? LocalBoard.O : LocalBoard.X;
    }


/*
 * Methods
 * */    
    // Trouver le meilleur coup à jouer.
    public GlobalMove findBestMove(GlobalBoard board, GlobalMove lastOpponentMove) {
        this.startTime = System.currentTimeMillis();
        GlobalMove bestMoveFound = null;

        System.out.println("AI (" + (aiMark == LocalBoard.X ? "X" : "O") + ") thinking...");

        //Si on peut gagner on gagne.
        ArrayList<GlobalMove> possibleMoves = board.getPossibleMoves(lastOpponentMove);
        for (GlobalMove immediateMove : possibleMoves) {
            GlobalBoard testBoard = new GlobalBoard(board);
            if (testBoard.play(immediateMove, this.aiMark)) {
                if (testBoard.checkGlobalWinner() == this.aiMark) {
                    System.out.println("Found immediate winning move: " + moveToString(immediateMove));
                    return immediateMove;
                }
            }
        }

        // Sinon on analyse le board.
        int maxDepth = 1;
        try {
            while (System.currentTimeMillis() - startTime < timeLimitMillis) {
                System.out.println("  Trying depth: " + maxDepth); // Print la profondeur

                // Créer un move pour calculer le minimax.
                // S'assurer que le board n'est pas le board réel mais le board copié.
                MoveScore currentBest = minimaxAlphaBeta(new GlobalBoard(board), 
                        lastOpponentMove, 
                        maxDepth,
                        Integer.MIN_VALUE, 
                        Integer.MAX_VALUE, 
                        true);

                // ...(rest of the iterative deepening loop as before)...
                if (currentBest != null && currentBest.move != null) {
                    bestMoveFound = currentBest.move;

                    //indiquer les donnees de la profondeur
                    System.out.println(
                        "  Depth " + 
                        maxDepth + 
                        " found move: " + 
                        moveToString(bestMoveFound) + 
                        " with score: " + 
                        currentBest.score);
                } else {

                    //Arrive rarement plus un error handling
                    System.out.println(
                    "  Depth " + 
                    maxDepth + 
                    " found no better move or timed out partially.");
                    if (currentBest == null)
                        break; 
                }

                // Valide si il reste du temps.
                if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
                    System.out.println("  Time limit reached after completing depth " + maxDepth);
                    break;
                }

                maxDepth++;

            } // While se conlue ici
        } catch (TimeoutException e) {
            System.out.println("  Search timed out during exploration.");
        }


        //Si aucune best move n'est trouvé ==> Prendre un coup hasard dans les coups possibles
        if (bestMoveFound == null) {
            System.err.println(
                    "WARNING: AI could not find a move (timeout or no valid moves?). Returning fallback move.");
            if (!possibleMoves.isEmpty()) {
                Collections.shuffle(possibleMoves);
                bestMoveFound = possibleMoves.get(0);
            } else {
                System.err.println("CRITICAL WARNING: No possible moves available for AI!");
                return null;
            }
        }

        System.out.println("AI chose move: " + moveToString(bestMoveFound));
        return bestMoveFound;
    }



    /**
     * ALGORITHME ALPHA BETA
     * Prend l'heuristic du globalBoard et calcul le score du coup 
     * 
     * Cette méthode est basé sur le laboratoire 1 de Ahmed Sherif
     * 
     */
    private MoveScore minimaxAlphaBeta(GlobalBoard currentBoard, GlobalMove lastMoveMade, int depth, int alpha,
            int beta, boolean isMaximizingPlayer) throws TimeoutException {

        
        // Verifie s'il reste du temps
        if (System.currentTimeMillis() - startTime >= timeLimitMillis) {
            throw new TimeoutException();
        }

        // Vérifie s'il n'y a toujours pas de gagnan.
        int globalWinner = currentBoard.checkGlobalWinner();
        if (globalWinner != LocalBoard.EMPTY || depth == 0 || currentBoard.isGlobalBoardFull()) {
            return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
        }

        ArrayList<GlobalMove> possibleNextMoves = currentBoard.getPossibleMoves(lastMoveMade);

        // Nous pourrions travailler sur une façon d'organiser où commencer au lieu d'y aller de manière aléatoire.
        Collections.shuffle(possibleNextMoves);

        GlobalMove bestMoveForThisNode = null;


        //Regarder si AlphaBeta calcul le max ou le mi
        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (GlobalMove move : possibleNextMoves) {
                // S'assurer de copier le board pour le calculer
                GlobalBoard nextBoard = new GlobalBoard(currentBoard);
                boolean played = nextBoard.play(move, this.aiMark);
                if (!played)
                    continue;

                //Récurrence
                MoveScore evalResult = minimaxAlphaBeta(nextBoard, move, depth - 1, alpha, beta, false);
                if (evalResult == null)
                    continue;

                if (evalResult.score > maxEval) {
                    maxEval = evalResult.score;
                    bestMoveForThisNode = move;
                }

                //Pruning
                alpha = Math.max(alpha, evalResult.score);
                if (beta <= alpha) {
                    break;
                }
            }
            if (possibleNextMoves.isEmpty()) {
                return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
            }
            return new MoveScore(bestMoveForThisNode, maxEval);

        } else {
            int minEval = Integer.MAX_VALUE;

            // Regarer pour l'ensemble des coups possible pour le min.
            for (GlobalMove move : possibleNextMoves) {
                GlobalBoard nextBoard = new GlobalBoard(currentBoard);
                boolean played = nextBoard.play(move, this.opponentMark);
                if (!played)
                    continue;

                //Récurrence
                MoveScore evalResult = minimaxAlphaBeta(nextBoard, move, depth - 1, alpha, beta, true); // AI's turn
                if (evalResult == null)
                    continue;
   
                if (evalResult.score < minEval) {
                    minEval = evalResult.score;
                    bestMoveForThisNode = move;
                }

                //Pruning
                beta = Math.min(beta, evalResult.score);
                if (beta <= alpha) {
                    break;
                }
            }

            //Retourne qqch si rien ne peut être joué
            if (possibleNextMoves.isEmpty()) {
                return new MoveScore(null, currentBoard.evaluateGlobal(this.aiMark));
            }
            return new MoveScore(bestMoveForThisNode, minEval);
        }
    }

    /**
     * 
     * UTILS SECTION
     */


    //Classe qui peremet d'enregistrer le coup a jouer et le score;
    private static class MoveScore {
        GlobalMove move;
        int score;

        MoveScore(GlobalMove move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    private static class TimeoutException extends Exception {
        public TimeoutException() {
            super("Search time limit exceeded.");
        }
    }

    // Convertion d'un move en string
    public static String moveToString(GlobalMove move) {
        if (move == null)
            return "";

        int overallCol = move.getGlobalCol() * 3 + move.getLocalCol(); // 0-8
        int overallRow = move.getGlobalRow() * 3 + move.getLocalRow(); // 0-8

        char colChar = (char) ('A' + overallCol);
        char rowChar = (char) ('1' + (8 - overallRow));
        rowChar = (char) ('1' + overallRow);

        return "" + colChar + rowChar;
    }
}