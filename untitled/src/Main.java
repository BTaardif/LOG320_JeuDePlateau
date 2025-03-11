import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {
    
   



    public static void main(String[] args) {
        Scanner lecture = new Scanner(System.in);
        //humanPlayer = Bienvenue(lecture);
        lecture.close(); // Fermer le scanner après l'utilisation

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
