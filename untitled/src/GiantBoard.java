import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


class GiantBoard {
    private char[][][] boards;
    private boolean[][] localBoardWon;
    private LocalBoard[][] board;
    private LocalBoard bboard;
    private int nextLocalBoard = -1;
    private ArrayList <LocalBoard> giantBoard_list;
    String[] letters = {"A", "B", "C", "D", "E","F","G","H", "I"};
    String[] numbers = {"9", "8", "7", "6" , "5", "4" , "3", "2", "1", "0"};
    int compteur_ticToe = 0;


    public GiantBoard() {
        //boards = new char[3][3][9]; // Chaque local board a 9 cases
        localBoardWon = new boolean[3][3];
        this.giantBoard_list = new ArrayList<>();

        
            for (int i = 0; i < giantBoard_list.size(); i++) {
                for (int j = 0; j < giantBoard_list.size(); j++) {
                    //board[i][j] = new LocalBoard(letters[j],numbers[i]);
                    this.bboard = new LocalBoard(letters[j],numbers[i]);
                    this.giantBoard_list.add(bboard);
                }
            }
            
        
        

    }


    // Trouver dans quel LocalBoard une case appartient
    public LocalBoard findLocalBoard(String position) {
        for (LocalBoard board : giantBoard_list) {
            if (board.getPosition().equals(position)) {
                return board;
            }
        }
        return null; // Retourne null si aucun LocalBoard ne correspond
    }

    public LocalBoard getLocalBoard(int row, int col) {
        return this.board[row][col];
    }

    public void play(int localRow, int localCol, int cellRow, int cellCol, char player) {
        int index = cellRow * 3 + cellCol;
        if (boards[localRow][localCol][index] == '\0') {
            boards[localRow][localCol][index] = player;
            if (checkLocalWin(localRow, localCol, player)) {
                localBoardWon[localRow][localCol] = true;
            }

            nextLocalBoard = getNextLocalBoard(cellRow, cellCol);
        }
    }


    
    @SuppressWarnings("unchecked")
    public void setTicTacToe(LocalBoard[][] localBoards) {
       //giantBoard_list.add(localBoards); // Ajouter un Tic-Tac-Toe à la liste
    }
    public int getNextLocalBoard(int row, int col) {
        int nextBoard = row * 3 + col;
        if (isLocalBoardWon(row, col) || isLocalBoardFull(row, col)) {
            return -1; // Joueur peut jouer n'importe où
        }
        return nextBoard;
    }

    public boolean isLocalBoardWon(int localRow, int localCol) {
        return localBoardWon[localRow][localCol];
    }

    private boolean isLocalBoardFull(int localRow, int localCol) {
        for (char cell : boards[localRow][localCol]) {
            if (cell == '\0') {
                return false;
            }
        }
        return true;
    }

    private boolean checkLocalWin(int localRow, int localCol, char player) {
        char[] board = boards[localRow][localCol];
        int[][] winPatterns = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, 
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, 
            {0, 4, 8}, {2, 4, 6}
        };
        for (int[] pattern : winPatterns) {
            if (board[pattern[0]] == player && board[pattern[1]] == player && board[pattern[2]] == player) {
                return true;
            }
        }
        return false;
    }

    public LocalBoard getBestLocalBoard() {
        if (nextLocalBoard == -1) {
            return null; // Le joueur peut jouer où il veut
        }
        int row = nextLocalBoard / 3;
        int col = nextLocalBoard % 3;
        return new LocalBoard(boards[row][col]); // Assure-toi que LocalBoard a un constructeur adapté
    }

    public void setTic() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                //board[i][j] = new LocalBoard(letters[j],numbers[i]);
                bboard = new LocalBoard(letters[j],numbers[i]);
                this.giantBoard_list.add(bboard);
            }
        }
    }
    
    
}
