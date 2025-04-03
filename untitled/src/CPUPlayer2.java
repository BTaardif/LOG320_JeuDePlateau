import java.util.*;

public class CPUPlayer2 {
    private Mark myMark, opponentMark;
    private static final long TIME_LIMIT_MS = 2900;
    private long startTime;
    private Move bestMoveOverall;
    private boolean timeUp;
    private List<Move> validMoves;

    public CPUPlayer2(int piece) {
        this.myMark = (piece == 2) ? Mark.O : Mark.X;
        this.opponentMark = (piece == 2) ? Mark.X : Mark.O;
    }

    private double evaluateQuickMove(GiantBoard board, Move move) {
        GiantBoard nextBoard = board.clone();
        nextBoard.applyMove(nextBoard, move);
        return evaluateBoard(nextBoard);
    }
    
    
    public Move play(GiantBoard board, String lastOpponentMove) {
        this.startTime = System.currentTimeMillis();
        this.timeUp = false;
        this.bestMoveOverall = null;
        
        String targetBoardIndex = determineTargetBoard(board, lastOpponentMove);
        validMoves = getValidMoves(board, targetBoardIndex, myMark);
        
        if (!validMoves.isEmpty()) {
            validMoves.sort(Comparator.comparingDouble(move -> evaluateQuickMove(board, move)));
            bestMoveOverall = validMoves.get(validMoves.size() - 1);
        }
        

        for (int depth = 1; !timeUp; depth++) {
            try {
                Move bestMoveAtThisDepth = findBestMove(board, targetBoardIndex, depth);
                if (!timeUp && bestMoveAtThisDepth != null) {
                    bestMoveOverall = bestMoveAtThisDepth;
                }
                if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
                    timeUp = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return bestMoveOverall;
    }





    private String determineTargetBoard(GiantBoard board, String lastOpponentMove) {
        if (lastOpponentMove == null) return "-1";
        //LocalBoard targetLocalBoard =  board.getPlateaux().get(lastOpponentMove.trim());
        String targetString =  board.rechercheMiniBoard(lastOpponentMove);
        LocalBoard targetLocalBoard = board.getPlateaux().get(targetString);
        
        return (targetLocalBoard == null || 
        targetLocalBoard.isFull() || 
        targetLocalBoard.isWon(myMark) || 
        targetLocalBoard.isWon(opponentMark)) ? "-1" : targetString;
    }

    Move findBestMove(GiantBoard board, String targetBoardIndex, int maxDepth) {
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY, beta = Double.POSITIVE_INFINITY;
        
        for (Move move : validMoves) {
            if (timeUp) break;
            GiantBoard nextBoard = board.clone();
            nextBoard.applyMove(nextBoard,move);
            double score = minimax(nextBoard, maxDepth - 1, alpha, beta, false, Integer.toString(move.getLocalBoardX() * 3 + move.getLocalBoardY()));
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return bestMove;
    }

    private double minimax(GiantBoard board, int depth, double alpha, double beta, boolean isMaximizing, String targetBoardIndex) {
        if (depth == 0 || board.checkWin(myMark) || board.checkWin(opponentMark) || board.isFull() || timeUp) {
            return evaluateBoard(board);
        }

        List<Move> validMoves = getValidMoves(board, targetBoardIndex, isMaximizing ? myMark : opponentMark);
        double bestScore = isMaximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Move move : validMoves) {
            if (timeUp) break;
            GiantBoard nextBoard = board.clone();
            nextBoard.applyMove(nextBoard,move);
            double score = minimax(nextBoard, depth - 1, alpha, beta, !isMaximizing, Integer.toString(move.getLocalBoardX() * 3 + move.getLocalBoardY()));
            if (isMaximizing) {
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);
            } else {
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);
            }
            if (beta <= alpha) break;
        }
        return bestScore;
    }

    //private double evaluateBoard(GiantBoard board) {
      //  return Math.random(); // Remplace avec une vraie heuristique
    //}

    private double evaluateBoard(GiantBoard board) {
        double score = 0;
    
        for (LocalBoard lb : board.getPlateaux().values()) {
            Mark winner = lb.getWinner();
            if (winner == myMark) {
                score += 100; // Un `LocalBoard` gagné vaut +100
            } else if (winner == opponentMark) {
                score -= 100; // Un `LocalBoard` perdu vaut -100
            } else {
                score += evaluateLocalBoard(lb);
            }
        }
        
        return score;
    }
    
    // Évaluation d’un `LocalBoard` non terminé
    private double evaluateLocalBoard(LocalBoard lb) {
        double localScore = 0;
        Case[][] board = lb.getBoardCases(); // Suppose que `getBoard()` retourne un tableau 3x3
    
        for (int i = 0; i < 3; i++) {
            localScore += evaluateLine(board[i][0].getCMark(), board[i][1].getCMark(), board[i][2].getCMark()); // Ligne
            localScore += evaluateLine(board[0][i].getCMark(), board[1][i].getCMark(), board[2][i].getCMark()); // Colonne
        }
        localScore += evaluateLine(board[0][0].getCMark(), board[1][1].getCMark(), board[2][2].getCMark()); // Diagonale \
        localScore += evaluateLine(board[0][2].getCMark(), board[1][1].getCMark(), board[2][0].getCMark()); // Diagonale /
    
        return localScore;
    }
    
    // Évalue une ligne, colonne ou diagonale
    
    private double evaluateLine(Mark a, Mark b, Mark c) {
        int cpuCount = 0, opponentCount = 0;
    
        if (a == myMark) cpuCount++;
        if (b == myMark) cpuCount++;
        if (c == myMark) cpuCount++;
    
        if (a == opponentMark) opponentCount++;
        if (b == opponentMark) opponentCount++;
        if (c == opponentMark) opponentCount++;
    
        if (cpuCount == 3) return 100; // Victoire
        if (opponentCount == 3) return -100; // Défaite
        if (cpuCount == 2 && opponentCount == 0) return 10; // Deux symboles alignés
        if (opponentCount == 2 && cpuCount == 0) return -10; // Danger imminent
    
        return 0;
    }
    

    /* 
    private double evaluateLine(Mark a, Mark b, Mark c) {
        int cpuCount = 0, opponentCount = 0;
        int emptyCount = 0;
    
        Mark[] marks = {a, b, c};
        for (Mark mark : marks) {
            if (mark == myMark) cpuCount++;
            else if (mark == opponentMark) opponentCount++;
            else emptyCount++;
        }
    
        if (cpuCount == 3) return 1000; // Victoire garantie
        if (opponentCount == 3) return -1000; // Défaite immédiate
        if (cpuCount == 2 && emptyCount == 1) return 50; // Menace de victoire
        if (opponentCount == 2 && emptyCount == 1) return -50; // Danger imminent
    
        return cpuCount - opponentCount; // Léger avantage
    }
    */

        private List<Move> getValidMoves(GiantBoard board, String targetBoardIndex, Mark myMark) {
            List<Move> moves = new ArrayList<>();
        
            if (targetBoardIndex.equals("-1")) {  // Vérifier avec .equals() pour les Strings
                for (LocalBoard lb : board.getPlateaux().values()) {
                    if (!lb.isFull() && lb.getWinner() == Mark.EMPTY) {  // Vérifier si le plateau est jouable
                        moves.addAll(lb.getAvailableMoves(myMark, lb.getLocalID()));
                    }
                }
            } else {
                LocalBoard lb = board.getPlateaux().get(targetBoardIndex);
                if (lb != null && !lb.isFull() && lb.getWinner() == Mark.EMPTY) {  // Vérifier si le plateau est valide
                    moves.addAll(lb.getAvailableMoves(myMark, lb.getLocalID()));
                }
            }
            return moves;
        }
    }
    