import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Scanner;



// Fenêtre principale
public class MainFrame extends JFrame {
    private CPUPlayer cpuPlayer;
    private GiantBoardPanel giantBoardPanel;
    protected static Mark humanPlayer;
    
    public MainFrame() {
        setTitle("Tic-Tac-Toe Géant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        
        Scanner lecture = new Scanner(System.in);
        humanPlayer = Bienvenue(lecture);
        lecture.close(); // Fermer le scanner après l'utilisation

        if (humanPlayer == Mark.X){
            cpuPlayer = new CPUPlayer(Mark.O);
        }
        else{
            cpuPlayer = new CPUPlayer(Mark.X);
        }
        
        giantBoardPanel = new GiantBoardPanel(cpuPlayer, humanPlayer);
        add(giantBoardPanel);
    }
    
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static Mark Bienvenue(Scanner lecture) {
        System.out.println("Bienvenue dans le jeu de Tic-Tac-Toe !");
        System.out.print("Choisissez votre pièce (X ou O) : ");
        String choix = lecture.nextLine().toUpperCase();

        if (choix.equals("X")) {
            return Mark.X;
        } else {
            return Mark.O;
        }
    }


}
