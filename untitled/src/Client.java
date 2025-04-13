import java.io.*;
import java.net.*;
import java.util.Arrays;

class Client {

    private static GlobalBoard gameBoard = new GlobalBoard();
    private static CPUPlayer aiPlayer;
    private static int myMark; // Board.X or Board.O
    private static int opponentMark;
    private static GlobalMove lastOpponentMove = null;

    public static void main(String[] args) {

        String serverAddress = "localhost"; // Default
        int serverPort = 8888; // Default

        // Configuration pour la connection au serveur
        if (args.length >= 1) {
            serverAddress = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number provided. Using default 8888.");
            }
        }

        System.out.println("Connecting to server " + serverAddress + ":" + serverPort + "...");

        Socket MyClient;
        BufferedInputStream input;
        BufferedOutputStream output;

        try {
            MyClient = new Socket(serverAddress, serverPort);
            System.out.println("Connected!");

            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());

            while (true) {
                System.out.println("\nWaiting for server command...");
                int command = input.read(); // Attente de commandes

                if (command == -1) {
                    System.out.println("Server disconnected.");
                    break;
                }

                char cmd = (char) command;
                System.out.println("Received command: " + cmd);

                // Command '1': Commencer le jeux en tante que joueur 1 (X)
                if (cmd == '1') {
                    myMark = LocalBoard.X;
                    opponentMark = LocalBoard.O;
                    aiPlayer = new CPUPlayer(myMark);
                    System.out.println("Commencer en tant que Player 1 (X).");
                    parseBoardState(input);

                    System.out.println("Making first move...");
                    GlobalMove aiMove = aiPlayer.findBestMove(gameBoard, null);
                    if (aiMove != null) {
                        boolean played = gameBoard.play(aiMove, myMark);
                        if (!played)
                            System.err.println("AI a fait un coup invalid"); // ca devrait jamais arriver

                        sendMoveToServer(output, aiMove);
                    } else {
                        System.err.println("AI peut pas faire un coup.");
                        break;
                    }
                    lastOpponentMove = null; // Reset le coup de l'adversaire

                }
                // Command '2': Commencer le jeux en tante que joueur 2 (X)
                else if (cmd == '2') {
                    myMark = LocalBoard.O;
                    opponentMark = LocalBoard.X;
                    aiPlayer = new CPUPlayer(myMark);
                    System.out.println("Commencer le jeux en tant que Joueur 2 (O). Attente pour le coup de Joueur x");
                    parseBoardState(input); // Lire l'état initial du plateau (sera mis à jour par le serveur)
                    
                    lastOpponentMove = null; // L'adversaire n'a pas encore joué
                    

                }
                // Commande '3' : Le serveur demande le prochain coup / fournit le dernier coup de l'adversaire
                else if (cmd == '3') {
                    String opponentMoveStr = parseOpponentMove(input); // Lire la chaîne représentant le coup de l'adversaire
                    System.out.println("Opponent played: " + opponentMoveStr);

                    lastOpponentMove = parseMoveString(opponentMoveStr); // Mettre à jour le dernier coup

                    if (lastOpponentMove != null) {
                        // Vérifier si le coup reçu est jouable sur notre plateau actuel
                        
                        if (gameBoard.isLocalBoardClosed(lastOpponentMove.getGlobalRow(),
                                lastOpponentMove.getGlobalCol()) ||
                                gameBoard.boards[lastOpponentMove.getGlobalRow()][lastOpponentMove.getGlobalCol()]
                                        .getCell(lastOpponentMove.getLocalRow(),
                                                lastOpponentMove.getLocalCol()) != LocalBoard.EMPTY) {
                            System.err.println("WARNING: Server sent an opponent move (" + opponentMoveStr
                                    + ") that seems invalid on our board!");
                        }

                        boolean played = gameBoard.play(lastOpponentMove, opponentMark);
                        if (!played) {
                            System.err.println("ERROR: Failed to apply valid opponent move " + opponentMoveStr
                                    + " to internal board!");
                            // gameBoard.printDetailedGlobalBoard(); //Afficher l'état du plateau pour le débogage

                            break; // Quitter
                        }
                        System.out.println("Board state after opponent's move:");
                        // gameBoard.printDetailedGlobalBoard(); //Afficher l'état du plateau pour le débogage
                            
                    } else if (!opponentMoveStr.equals("A0") && !opponentMoveStr.trim().isEmpty()) {
                        // A0 est attendu pour la toute première invite pour X, ignorer les autres formats invalides
                        System.err.println("Could not parse opponent move: " + opponentMoveStr);
                    }

                    // Maintenant, calculer et envoyer le coup de l'IA
                    System.out.println("Calculating AI move...");
                    GlobalMove aiMove = aiPlayer.findBestMove(gameBoard, lastOpponentMove);
                    if (aiMove != null) {
                        // Appliquer le coup de l'IA sur notre plateau interne
                        boolean played = gameBoard.play(aiMove, myMark);
                        if (!played) {
                            System.err.println(
                                    "CRITICAL: AI generated an invalid move: " + CPUPlayer.moveToString(aiMove));
                            // Tentative de récupération ou sortie
                            break; // Quitter
                        }

                        sendMoveToServer(output, aiMove);
                        System.out.println("Board state after AI's move:");
                        // gameBoard.printDetailedGlobalBoard();
                    } else {
                        System.err.println("AI could not determine a move.");
                        break;
                    }

                }
                // Commande '4' : Le dernier coup envoyé était invalide
                else if (cmd == '4') {
                    System.err.println("!!!! Server reported last move was invalid !!!!");

                    System.out.println("Attempting to recalculate move...");
                    // Réutiliser le *précédent* lastOpponentMove pour déterminer les coups valides maintenant
                    GlobalMove aiMove = aiPlayer.findBestMove(gameBoard, lastOpponentMove);
                    if (aiMove != null) {
                        
                        sendMoveToServer(output, aiMove);
                        // Nous supposons que le serveur acceptera celui-ci. Sinon, nous pourrions boucler.
                    } else {
                        System.err.println("AI could not determine a recovery move.");
                        break;
                    }

                } else if (cmd == '5') {
                    String finalMoveStr = parseOpponentMove(input); // Lire le tout dernier coup joué
                    System.out.println("Game Over! Last move played was: " + finalMoveStr);

                    // Appliquer le dernier coup pour voir l'état final
                    GlobalMove finalMove = parseMoveString(finalMoveStr);
                    if (finalMove != null) {
                       // Déterminer qui a joué en dernier en fonction de qui *aurait dû* jouer
                        int lastPlayer = (aiPlayer.findBestMove(gameBoard, finalMove) == null) ? myMark : opponentMark;
                    }

                    int winner = gameBoard.checkGlobalWinner();
                    if (winner == myMark)
                        System.out.println("AI WINS!");
                    else if (winner == opponentMark)
                        System.out.println("Opponent Wins.");
                    else
                        System.out.println("It's a DRAW!");

                    break; // Quitter
                } else {
                    System.out.println("Unknown command from server: " + cmd);
                    if (input.available() > 0) {
                        byte[] buffer = new byte[input.available()];
                        input.read(buffer, 0, buffer.length);
                        System.out.println("  Skipped extra data: " + new String(buffer));
                    }
                }
            } // fin de la boucle while

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyer les ressources (bien que la fermeture du socket ne soit pas explicitement montrée dans l'original)
            System.out.println("Client shutting down.");
        }
    }

    // Lire et analyser l'état complet du plateau 
    private static void parseBoardState(BufferedInputStream input) throws IOException {
        byte[] buffer = new byte[2048]; // Ajuster la taille si nécessaire, devrait être > 81*2 + espaces
        int bytesRead = 0;
        int attempts = 0;

        //  lire les données envoyées par le serveur concernant l'état du plateau
        while (bytesRead < 81 * 2 && attempts < 5) { // Besoin d'au moins 81 chiffres + 80 espaces
            if (input.available() > 0) {
                int readNow = input.read(buffer, bytesRead, buffer.length - bytesRead);
                if (readNow > 0)
                    bytesRead += readNow;
            } else {
                // Attendre brièvement si aucune donnée n'est disponible, 
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            attempts++;
        }

        if (bytesRead == 0) {
            System.err.println("Error: Did not receive board state from server.");
            return;
        }

        String boardString = new String(buffer, 0, bytesRead).trim();
        System.out.println("Received board string (raw): \"" + boardString + "\""); // Sortie de débogage

        String[] boardValues = boardString.split("\\s+"); 

        if (boardValues.length < 81) {
            System.err.println("Error: Received incomplete board state. Expected 81 values, got " + boardValues.length);
            System.err.println("Values: " + Arrays.toString(boardValues));
            return;
        }

        int index = 0;
        for (int gRow = 0; gRow < 3; gRow++) {
            for (int lRow = 0; lRow < 3; lRow++) {
                for (int gCol = 0; gCol < 3; gCol++) {
                    for (int lCol = 0; lCol < 3; lCol++) {
                        try {
                            int piece = Integer.parseInt(boardValues[index]);
                            // Placer la pièce sur le bon sous-plateau
                            gameBoard.boards[gRow][gCol].play(new LocalMove(lRow, lCol), piece); // Définit directement la pièce
                            index++;
                        } catch (NumberFormatException e) {
                            System.err
                                    .println("Error parsing board value at index " + index + ": " + boardValues[index]);
                            return; // Arrêter l'analyse en cas d'erreur
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println("Error: Ran out of board values at index " + index);
                            return;
                        }
                    }
                }
            }
        }
        System.out.println("Successfully parsed board state.");
        // mettre à jour le tableau des gagnants locaux

        gameBoard.updateAllLocalWinners();
    }

    //  Méthode utilitaire pour lire le coup de l'adversaire après la commande '3' or '5'
    private static String parseOpponentMove(BufferedInputStream input) throws IOException {
        // Le serveur envoie le coup sous forme de chaîne " D6"
        byte[] buffer = new byte[16]; 
        int bytesRead = 0;
        int attempts = 0;
        // Lire jusqu'à obtenir des données ou expiration du délai
        while (bytesRead == 0 && attempts < 10) {
            if (input.available() > 0) {
                int readNow = input.read(buffer, 0, buffer.length);
                if (readNow > 0)
                    bytesRead = readNow;
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            attempts++;
        }

        if (bytesRead == 0) {
            System.err.println("Warning: Did not receive opponent move string after command 3/5.");
            return "";
        }

        String moveStr = new String(buffer, 0, bytesRead).trim();

        // Premier tour ==> A0
        if (moveStr.equals("A0"))
            return "A0";

        // Convertion et validation du String reçu. Traduction en position de coup.
        if (moveStr.length() == 2 && moveStr.charAt(0) >= 'A' && moveStr.charAt(0) <= 'I' && moveStr.charAt(1) >= '1'
                && moveStr.charAt(1) <= '9') {
            return moveStr;
        } else {
            System.err.println("Warning: Received potentially invalid move format: \"" + moveStr + "\"");
            return moveStr;
        }
    }

    // Traduit le String du serveur en coup jouable
    public static GlobalMove parseMoveString(String moveStr) {

        // Premier tour ==> A0
        if (moveStr == null || moveStr.length() != 2 || moveStr.equals("A0")) {
            return null;
        }
        moveStr = moveStr.trim().toUpperCase();

        char colChar = moveStr.charAt(0);
        char rowChar = moveStr.charAt(1);


        //Error handling pour un coup impossible
        if (colChar < 'A' || colChar > 'I' || rowChar < '1' || rowChar > '9') {
            System.err.println("Error parsing move string: Invalid format '" + moveStr + "'");
            return null;
        }

        int overallCol = colChar - 'A'; // 0-8
        int overallRow = rowChar - '1'; // 0-8

        //convertion 3x3
        int gRow = overallRow / 3;
        int lRow = overallRow % 3;
        int gCol = overallCol / 3;
        int lCol = overallCol % 3;

        return new GlobalMove(gRow, gCol, lRow, lCol);
    }

    private static void sendMoveToServer(BufferedOutputStream output, GlobalMove move) throws IOException {
        String moveStr = CPUPlayer.moveToString(move);
        System.out.println("Sending move: " + moveStr);
        output.write(moveStr.getBytes());
        output.flush();
    }
}