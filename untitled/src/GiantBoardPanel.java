
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

// Panneau pour le plateau géant
class GiantBoardPanel extends JPanel {
    private LocalBoardPanel[][] localBoards = new LocalBoardPanel[3][3];
    private LocalBoard[][] localTicTacToe = new LocalBoard[3][3];
    private CPUPlayer cpuPlayer;
    private Mark playerX = Mark.EMPTY;
    private GiantBoard giantBoard = new GiantBoard();
    


    public GiantBoardPanel(CPUPlayer cpuPlayer, Mark player) {
        this.cpuPlayer = cpuPlayer;
        this.playerX = player;
        giantBoard.setTic();
        

        setLayout(new GridLayout(3, 3)); // Grille 3x3
        
        // Ajouter les 9 petits plateaux
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                localBoards[i][j] = new LocalBoardPanel(this);
                add(localBoards[i][j]);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                localTicTacToe[i][j] = new LocalBoard();
                giantBoard.setTicTacToe(localTicTacToe);
            }
        }
    }
    
    public void playCPU() {
        ArrayList<Move> moves = cpuPlayer.getNextMoveMinMax(new LocalBoard()); // Remplacer par le vrai board
        if (!moves.isEmpty()) {
            Move move = moves.get(2);
            localBoards[move.getRow()][move.getCol()].playCPU(cpuPlayer.CPU_AI);
        }
    }
    
    public void switchTurn() {
        //playerX = (playerX == Mark.X) ? Mark.O : Mark.X;
        if (playerX != cpuPlayer.CPU_AI) {
            playCPU();
        }
    }

    public Mark getCurrentPlayer() {
        return playerX;
    }
}

/*
// Panneau pour le plateau géant
class GiantBoardPanel extends JPanel {
    private LocalBoardPanel[][] localBoards = new LocalBoardPanel[3][3];
    private CPUPlayer cpuPlayer;
    private Mark currentPlayer = Mark.X;
    private GiantBoard giantBoard; // Stocke la logique du jeu
    private static int nextLocalBoard = -1; // -1 signifie que le joueur peut jouer n'importe où
    
        public GiantBoardPanel(GiantBoard giantBoard) {
            this.giantBoard = giantBoard;
            this.setLayout(new GridLayout(3, 3));
            localBoards = new LocalBoardPanel[3][3];
    
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    localBoards[i][j] = new LocalBoardPanel(i, j, this);
                    this.add(localBoards[i][j]);
                }
            }
        }
    
        public void handleMove(int localRow, int localCol, int cellRow, int cellCol, char player) {
            giantBoard.play(localRow, localCol, cellRow, cellCol, player);
            nextLocalBoard = giantBoard.getNextLocalBoard(cellRow, cellCol);
            updateBoardState();
            switchTurn();
        }
    
        private void updateBoardState() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    boolean enabled = (nextLocalBoard == -1 || (i == nextLocalBoard / 3 && j == nextLocalBoard % 3));
                    localBoards[i][j].setEnabled(enabled && !giantBoard.isLocalBoardWon(i, j));
                }
            }
        }
    
    
    public void switchTurn() {
        currentPlayer = (currentPlayer == Mark.X) ? Mark.O : Mark.X;
        if (currentPlayer == Mark.O) {
            playCPU();
        }
    }

    public void playCPU() {
        if (cpuPlayer == null) return; // Vérifie que l'IA existe
    
        // 🔥 L'IA choisit un coup
        Move move_cpu = cpuPlayer.getBestMove(giantBoard, nextLocalBoard);
        if (move_cpu != null) {
            int localRow = move_cpu.getRow();
            int localCol = move_cpu.getCol();
            int cellRow = 0;
            int cellCol = 0;
    
            // Joue le coup de l'IA
            handleMove(localRow, localCol, cellRow, cellCol, 'O');
        }
        //handleMove(0, 0, 0, 0, 'O');
    }   
    
    public Mark getCurrentPlayer() {
        return currentPlayer;
    }
}

*/

