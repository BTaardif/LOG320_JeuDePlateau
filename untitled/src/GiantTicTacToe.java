import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GiantTicTacToe extends JFrame {
    private JButton[][] buttons = new JButton[9][9];
    private char[][] board = new char[9][9];
    private boolean xTurn = true;

    public GiantTicTacToe() {
        setTitle("Giant Tic-Tac-Toe");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(9, 9, 2, 2)); // Espacement pour lignes noires
        getContentPane().setBackground(Color.BLACK);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                buttons[i][j].setBackground(Color.WHITE);
                add(buttons[i][j]);
            }
        }

        setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        int row, col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board[row][col] == '\0') { // Si case vide
                board[row][col] = xTurn ? 'X' : 'O';
                buttons[row][col].setText(String.valueOf(board[row][col]));
                xTurn = !xTurn;
            }
        }
    }

    public static void main(String[] args) {
        new GiantTicTacToe();
    }
}
