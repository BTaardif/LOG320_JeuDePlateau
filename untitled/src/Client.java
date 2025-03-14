import java.io.*;
import java.net.*;


class Client {
    public static void main(String[] args) {
        Socket MyClient;
        BufferedInputStream input;
        BufferedOutputStream output;
        int[][] board = new int[9][9];
        String[] letters = {"A", "B", "C", "D", "E","F","G","H", "I"};
        String[] numbers = {"1", "2", "3", "4" , "5", "6" , "7", "8", "9"};

        GiantBoard giantBoard = new GiantBoard();
        CPUGiantPlayer cpu = null;
        CPUPlayer2 CPUPlayer2 = null;
        int piece = 4;

        try {
            MyClient = new Socket("localhost", 8888);
            input    = new BufferedInputStream(MyClient.getInputStream());
            output   = new BufferedOutputStream(MyClient.getOutputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            giantBoard.setminiBoardTac(letters,numbers);

            while(true){
                char cmd = 0;

                cmd = (char)input.read();
                System.out.println(cmd);

                // Debut de la partie en joueur blanc (0)
                if(cmd == '1'){
                    byte[] aBuffer = new byte[1024];

                    int size = input.available();
                    //System.out.println("size " + size);
                    input.read(aBuffer,0,size);
                    String s = new String(aBuffer).trim();
                    giantBoard.updateBoard(s);
                    piece = 4;
                    cpu = new CPUGiantPlayer(piece, 2);
                    System.out.println(s);
                    String[] boardValues;
                    boardValues = s.split(" ");
                    int x=0,y=0;
                    for(int i=0; i<boardValues.length;i++){
                        board[x][y] = Integer.parseInt(boardValues[i]);
                        x++;
                        if(x == 9){
                            x = 0;
                            y++;
                        }
                    }

                    System.out.println("Nouvelle partie! Vous jouer blanc, entrez votre premier coup : ");
                    String move = pickMove(giantBoard, cpu, piece);
                    System.out.println(move);
                    output.write(move.getBytes(),0,move.length());
                    output.flush();
                }

                // Debut de la partie en joueur Noir (O)
                if(cmd == '2'){
                    System.out.println("Nouvelle partie! Vous jouer noir, attendez le coup des blancs");
                    byte[] aBuffer = new byte[1024];

                    int size = input.available();
                    //System.out.println("size " + size);
                    input.read(aBuffer,0,size);
                    String s = new String(aBuffer).trim();
                    System.out.println(s);
                    String[] boardValues;
                    boardValues = s.split(" ");
                    int x=0,y=0;
                    for(int i=0; i<boardValues.length;i++){
                        board[x][y] = Integer.parseInt(boardValues[i]);
                        x++;
                        if(x == 9){
                            x = 0;
                            y++;
                        }
                    }

                    giantBoard.updateBoard(s);
                    piece = 2;
                    cpu = new CPUGiantPlayer(piece, 2);
                }


                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue.
                // Tour du programme de jouer
                if(cmd == '3'){
                    byte[] aBuffer = new byte[16];

                    int size = input.available();
                    System.out.println("size :" + size);
                    input.read(aBuffer,0,size);

                    String s = new String(aBuffer);
                    System.out.println("Dernier coup :"+ s);
                    System.out.println("Entrez votre coup : ");

                    giantBoard.afficherTiLocalBoard();
                    humanPlay(giantBoard, s, piece);
                    giantBoard.afficherTiLocalBoard();
                    //play(giantBoard, s, piece);
                    
                    //play() -> cpu
                    String move = pickMove(giantBoard, cpu, piece);
                    output.write(move.getBytes(),0,move.length());
                    output.flush();

                }
                // Le dernier coup est invalide
                if(cmd == '4'){
                    System.out.println("Coup invalide, entrez un nouveau coup : ");
                    String move = pickMove(giantBoard, cpu, piece);
                    output.write(move.getBytes(),0,move.length());
                    output.flush();

                }
                // La partie est terminée
                if(cmd == '5'){
                    byte[] aBuffer = new byte[16];
                    int size = input.available();
                    input.read(aBuffer,0,size);
                    String s = new String(aBuffer).trim();
                    System.out.println("Partie Terminé. Le dernier coup joué est: "+s);
                    String move = null;
                    move = console.readLine();
                    output.write(move.getBytes(),0,move.length());
                    output.flush();
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void humanPlay(GiantBoard giantBoard, String data, int piece){
        giantBoard.getMiniTiLocalBoard(data, piece);

    }

    private static void play(GiantBoard giantBoard, String data, int piece) {
        if(data.length() < 2) {
            return;
        }
        int row = data.charAt(1) - 'A';
        int col = data.charAt(2) - '1';
        int opp = (piece == 4 ? 2 : 4);
        if(row >= 0 && row < 9 && col >= 0 && col < 9) {
            giantBoard.playMove(row, col, opp);
        }
    }

    private static String pickMove(GiantBoard giantBoard, CPUGiantPlayer cpu, int piece) {
        Move move = cpu.getMove(giantBoard);
        if(move == null) {
            return "A0";
        }
        giantBoard.playMove(move.getRow(), move.getCol(), piece);
        return convertMove(move.getRow(), move.getCol());
    }

    private static String convertMove(int row, int col) {
        String letters = "ABCDEFGHI";
        String digits = "123456789";
        if(row < 0 || row > 8 || col < 0 || col > 8) {
            return "A0";
        }
        return letters.charAt(row) + "" + digits.charAt(col);
    }
}