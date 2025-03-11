import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


class LocalBoardPanel extends JPanel {
    private JButton[] buttons = new JButton[9];
    private GiantBoardPanel parentBoard;
    private LocalBoard localBoard;

    
    
    public LocalBoardPanel(GiantBoardPanel parentBoard) {
        this.parentBoard = parentBoard;
        setLayout(new GridLayout(3, 3)); // Chaque petit plateau est aussi une grille 3x3
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Ajouter les cases
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("Arial", Font.BOLD, 24));
            buttons[i].addActionListener(new ButtonClickListener());
            add(buttons[i]);
        }

        
    }
    
    public LocalBoardPanel(LocalBoard localBoard) {
        this.localBoard = localBoard;
        setLayout(new GridLayout(3, 3)); // Chaque petit plateau est aussi une grille 3x3
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Ajouter les cases
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("Arial", Font.BOLD, 24));
            buttons[i].addActionListener(new ButtonClickListener());
            add(buttons[i]);
        }

        
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton clickedButton = (JButton) e.getSource();
            if (clickedButton.getText().equals("")) {
                clickedButton.setText(parentBoard.getCurrentPlayer().toString());
                
            }
            parentBoard.switchTurn();
            
        }
    }
    
    public void playCPU(Mark playerMark) {
        for (JButton button : buttons) {
            if (button.getText().equals("")) {
                button.setText(playerMark.toString());
                break;
            }
        }
    }

}    
/* 
// Panneau pour un petit plateau
public class LocalBoardPanel extends JPanel {
    
    private GiantBoardPanel parentBoard;
    private LocalBoard localBoard;
    private JButton[][] buttons;
    private int localRow, localCol;
    private GiantBoardPanel parentPanel;

    public LocalBoardPanel(int localRow, int localCol, GiantBoardPanel parentPanel) {
        this.localRow = localRow;
        this.localCol = localCol;
        this.parentPanel = parentPanel;
        this.setLayout(new GridLayout(3, 3));
        buttons = new JButton[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton(" ");
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                this.add(buttons[i][j]);
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        private int cellRow, cellCol;

        public ButtonClickListener(int cellRow, int cellCol) {
            this.cellRow = cellRow;
            this.cellCol = cellCol;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton clickedButton = (JButton) e.getSource();
            if (!clickedButton.getText().equals(" ")) return; // Case déjà occupée

            clickedButton.setText("X"); // Joueur humain (à améliorer pour gérer X et O dynamiquement)
            parentPanel.handleMove(localRow, localCol, cellRow, cellCol, 'X');
        }
    }

    
    public void playCPU_local() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals(" ")) { // Vérifie si la case est vide
                    buttons[i][j].setText("O"); // L'IA joue ici
                    return; // Sort de la méthode après avoir joué
                }
            }
        }
    }

}    

*/