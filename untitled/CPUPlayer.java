import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class CPUPlayer
{

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;
    private Mark cpu;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu){
        this.cpu = cpu;
    }

    // Ne pas changer cette méthode
    public int  getNumOfExploredNodes(){
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestValue = Integer.MIN_VALUE;

        for (Move move : board.getPossibleMoves()) {
            Board newBoard = new Board(board);
            newBoard.play(move, cpu);

            int moveValue = minimax(newBoard, false);

            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (moveValue == bestValue) {
                bestMoves.add(move);
            }
        }
        return bestMoves;
    }

    private int minimax(Board board, boolean isMax) {
        numExploredNodes++;
        int score = board.evaluate(cpu);

        if (score == 100 || score == -100 || board.getPossibleMoves().isEmpty()) {
            return score;
        }

        if (isMax) {  // le tour à max
            int best = Integer.MIN_VALUE;
            for (Move move : board.getPossibleMoves()) {
                Board newBoard = new Board(board);
                newBoard.play(move, cpu);
                best = Math.max(best, minimax(newBoard, false));
            }
            return best;
        } else {  // le tour à min
            int best = Integer.MAX_VALUE;
            for (Move move : board.getPossibleMoves()) {
                Board newBoard = new Board(board);
                newBoard.play(move, (cpu == Mark.X) ? Mark.O : Mark.X);
                best = Math.min(best, minimax(newBoard,true));
            }
            return best;
        }
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        int bestValue = Integer.MIN_VALUE;
        ArrayList<Move> bestMoves = new ArrayList<>();

        for (Move move : board.getPossibleMoves()) {
            Board newBoard = new Board(board);
            newBoard.play(move, cpu);

            int moveValue = alphabeta(newBoard, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (moveValue == bestValue) {
                bestMoves.add(move);
            }
        }
        return bestMoves;
    }

    private int alphabeta(Board board, int alpha, int beta, boolean isMax) {
        numExploredNodes++;
        int score = board.evaluate(cpu);

        if (score == 100 || score == -100 || board.getPossibleMoves().isEmpty()) {
            return score;
        }

        if (isMax) {  // le tour a max
            int best = Integer.MIN_VALUE;
            for (Move move : board.getPossibleMoves()) {
                Board newBoard = new Board(board);
                newBoard.play(move, cpu);
                best = Math.max(best, alphabeta(newBoard, alpha, beta, false));
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break;  // Alpha-Beta Pruning
            }
            return best;
        } else {  // le tour a min
            int best = Integer.MAX_VALUE;
            for (Move move : board.getPossibleMoves()) {
                Board newBoard = new Board(board);
                newBoard.play(move, (cpu == Mark.X) ? Mark.O : Mark.X);
                best = Math.min(best, alphabeta(newBoard, alpha, beta, true));
                beta = Math.min(beta, best);
                if (beta <= alpha) break;  // Alpha-Beta Pruning
            }
            return best;
        }
    }
}