
import java.util.*;

public class CPUPlayer2 {

    private Mark myMark, opponentMark;
    private static final long TIME_LIMIT_MS = 2900;
    private long startTime;
    private boolean timeUp;
    private Move bestMoveOverall;
    private List<Move> validMoves;

    public CPUPlayer2(int piece) {
        this.myMark = (piece == 2) ? Mark.O : Mark.X;
        this.opponentMark = (piece == 2) ? Mark.X : Mark.O;
    }

    public Move play(GiantBoard board, String lastOpponentMove) {
        startTime = System.currentTimeMillis();
        timeUp = false;
        bestMoveOverall = null;

        String targetBoardIndex = determineTargetBoard(board, lastOpponentMove);
        validMoves = getValidMoves(board, targetBoardIndex, myMark);

        if (!validMoves.isEmpty()) {
            bestMoveOverall = validMoves.get(validMoves.size() - 1); // Initial quick selection
        }

        // Search for the best move with Minimax and Alpha-Beta pruning
        for (int depth = 1; !timeUp; depth++) {
            Move bestMoveAtThisDepth = findBestMove(board, targetBoardIndex, depth);
            if (!timeUp && bestMoveAtThisDepth != null) {
                bestMoveOverall = bestMoveAtThisDepth;
            }
            if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
                timeUp = true;
            }
        }
        return bestMoveOverall;
    }

    private String determineTargetBoard(GiantBoard board, String lastOpponentMove) {
        if (lastOpponentMove == null) return "-1";

        String targetString = board.rechercheMiniBoard(lastOpponentMove);
        LocalBoard targetLocalBoard = board.getPlateaux().get(targetString);

        return (targetLocalBoard == null || targetLocalBoard.isFull() || 
                targetLocalBoard.isWon(myMark) || targetLocalBoard.isWon(opponentMark)) ? "-1" : targetString;
    }

    private List<Move> getValidMoves(GiantBoard board, String targetBoardIndex, Mark currentMark) {
        List<Move> moves = new ArrayList<>();
        if (targetBoardIndex.equals("-1")) {  
            board.getPlateaux().values().forEach(lb -> {
                if (!lb.isFull() && lb.getWinner() == Mark.EMPTY) {
                    moves.addAll(lb.getAvailableMoves(currentMark, lb.getLocalID()));
                }
            });
        } else {
            LocalBoard lb = board.getPlateaux().get(targetBoardIndex);
            if (lb != null && !lb.isFull() && lb.getWinner() == Mark.EMPTY) {
                moves.addAll(lb.getAvailableMoves(currentMark, lb.getLocalID()));
            }
        }
        return moves;
    }

    private Move findBestMove(GiantBoard board, String targetBoardIndex, int maxDepth) {
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY, beta = Double.POSITIVE_INFINITY;

        for (Move move : validMoves) {
            if (timeUp) break;
            GiantBoard nextBoard = board.clone();
            nextBoard.applyMove(nextBoard, move);
            double score = minimax(nextBoard, maxDepth - 1, alpha, beta, false, Integer.toString(move.getLocalBoardY() + move.getLocalBoardX()));
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
            nextBoard.applyMove(nextBoard, move);
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

    private double evaluateBoard(GiantBoard board) {
        double score = 0;

        for (LocalBoard lb : board.getPlateaux().values()) {
            Mark winner = lb.getWinner();
            if (winner == myMark) {
                score += 100; // A won `LocalBoard` adds +100
            } else if (winner == opponentMark) {
                score -= 100; // A lost `LocalBoard` subtracts -100
            } else {
                score += evaluateLocalBoard(lb);
            }
        }

        return score;
    }

    private double evaluateLocalBoard(LocalBoard lb) {
        double score = 0;
        Case[][] board = lb.getBoardCases(); // Assuming `getBoard()` returns a 3x3 grid

        for (int i = 0; i < 3; i++) {
            score += evaluateLine(board[i][0].getCMark(), board[i][1].getCMark(), board[i][2].getCMark()); // Row
            score += evaluateLine(board[0][i].getCMark(), board[1][i].getCMark(), board[2][i].getCMark()); // Column
        }
        score += evaluateLine(board[0][0].getCMark(), board[1][1].getCMark(), board[2][2].getCMark()); // Diagonal \
        score += evaluateLine(board[0][2].getCMark(), board[1][1].getCMark(), board[2][0].getCMark()); // Diagonal /

        return score;
    }

    private double evaluateLine(Mark a, Mark b, Mark c) {
        int cpuCount = 0, opponentCount = 0;

        if (a == myMark) cpuCount++;
        if (b == myMark) cpuCount++;
        if (c == myMark) cpuCount++;

        if (a == opponentMark) opponentCount++;
        if (b == opponentMark) opponentCount++;
        if (c == opponentMark) opponentCount++;

        if (cpuCount == 3) return 100; // Win
        if (opponentCount == 3) return -100; // Loss
        if (cpuCount == 2 && opponentCount == 0) return 10; // Two marks aligned
        if (opponentCount == 2 && cpuCount == 0) return -10; // Imminent danger

        return 0;
    }
}