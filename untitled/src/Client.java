import java.io.*;
import java.net.*;
import java.util.Arrays;


class Client {
    private static GiantBoard giantBoard = new GiantBoard();
    private static AIPlayer aiPlayer;
    private static int myPlayerId; // Board.PLAYER_X or Board.PLAYER_O

    public static void main(String[] args) {
        String serverIP = "localhost";
        int serverPort = 8888;

        // Basic argument parsing (optional, as per PDF [cite: 28, 29])
        if (args.length > 0) {
            serverIP = args[0];
        }
        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number provided. Using default 8888.");
            }
        }

        Socket MyClient = null;
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        // Temporary board storage from server message
        int[][] boardStateFromServer = new int[9][9];

        try {
            System.out.println("Connecting to server " + serverIP + ":" + serverPort + "...");
            MyClient = new Socket(serverIP, serverPort);
            System.out.println("Connected to server!");

            input    = new BufferedInputStream(MyClient.getInputStream());
            output   = new BufferedOutputStream(MyClient.getOutputStream());

            while(true){
                char cmd = 0;
                cmd = (char)input.read();
                System.out.println("Commande recu: " + cmd);

                // Read remaining data after command byte
                byte[] aBuffer = new byte[1024]; // Adjust size if necessary
                int size = 0;
                // A small delay might be needed if data isn't immediately available after cmd
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                if (input.available() > 0) {
                    size = input.read(aBuffer, 0, Math.min(aBuffer.length, input.available()));
                }

                String dataString = "";
                if (size > 0) {
                    dataString = new String(aBuffer, 0, size).trim();
                }
                System.out.println("Received data: \"" + dataString + "\"");

                // Debut de la partie en joueur blanc (0)
                if(cmd == '1'){
                    myPlayerId = Board.PLAYER_X;
                    aiPlayer = new AIPlayer(myPlayerId);
                    System.out.println("Nouvelle partie! Vous jouez Blanc (X).");

                    parseBoardState(dataString, boardStateFromServer);
                    giantBoard.updateBoard(boardStateFromServer); // Update internal board state
                    System.out.println(giantBoard); // Print board


                    // AI calculates the first move
                    int[] bestMove = aiPlayer.findBestMove(giantBoard);
                    if (bestMove != null) {
                        String moveStr = convertMoveToString(bestMove);
                        System.out.println("AI plays: " + moveStr);
                        output.write(moveStr.getBytes(), 0, moveStr.length());
                        output.flush();
                        // Assume server will update board state, or update locally if needed:
                        // giantBoard.makeMove(bestMove[0], bestMove[1], myPlayerId);
                    } else {
                        System.out.println("AI could not determine a first move.");
                        // Handle error - maybe send a default move or resign?
                    }

                }

                // Debut de la partie en joueur Noir (X)
                if(cmd == '2'){
                    myPlayerId = Board.PLAYER_O;
                    aiPlayer = new AIPlayer(myPlayerId);
                    System.out.println("Nouvelle partie! Vous jouez Noir (O), attendez le coup des blancs.");

                    parseBoardState(dataString, boardStateFromServer);
                    giantBoard.updateBoard(boardStateFromServer);
                    System.out.println(giantBoard);

                    // Attente pour command '3'
                }


                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue. Ex: "3 D6"
                else if(cmd == '3'){
                    System.out.println("Server requests next move.");
                    String lastMoveStr = dataString; // The data is the last move, e.g., "D6" [cite: 39]
                    System.out.println("Dernier coup de l'adversaire: "+ lastMoveStr);

                    // Update board state based on opponent's move IF needed.
                    // The safest approach is to rely on the server sending the full board state
                    // with commands '1' or '2', and potentially assume the server keeps track.
                    // However, we MUST use the opponent's move to determine the *next* playable board. [cite: 15, 16]
                    int[] opponentMoveCoords = convertStringToMoveCoords(lastMoveStr);
                    if (opponentMoveCoords != null) {
                        int opponentGlobalRow = opponentMoveCoords[0];
                        int opponentGlobalCol = opponentMoveCoords[1];
                        // It's generally safer to update our internal state *before* calculating the next move.
                        // But we need the board state *before* the opponent move to correctly apply it.
                        // Let's assume the giantBoard is up-to-date *before* this command arrives,
                        // and just update the *next target board* based on the opponent's move coordinates.

                        // Important: Update which local board the AI must play in
                        int opponentLocalRow = opponentGlobalRow % 3;
                        int opponentLocalCol = opponentGlobalCol % 3;
                        giantBoard.setNextBoardFromLastMove(opponentLocalRow, opponentLocalCol);


                        // *Optional but Recommended*: If the server doesn't send the full board state
                        // with move requests, update the board locally with the opponent's move here.
                        // giantBoard.makeMove(opponentGlobalRow, opponentGlobalCol, (myPlayerId == Board.PLAYER_X ? Board.PLAYER_O : Board.PLAYER_X));
                        // System.out.println("Board state after opponent's move:\n" + giantBoard);

                    } else if (!lastMoveStr.equals("A0")) { // A0 is the invalid first move for player X [cite: 40]
                        System.err.println("Could not parse opponent's last move: " + lastMoveStr);
                        // Decide how to handle - maybe play anywhere? Or request board state?
                        giantBoard.setNextBoardFromLastMove(-1,-1); // Play anywhere if unsure
                    } else {
                        giantBoard.setNextBoardFromLastMove(-1,-1); // First move for player X, play anywhere
                    }


                    // AI calculates the next move
                    int[] bestMove = aiPlayer.findBestMove(giantBoard);
                    if (bestMove != null) {
                        String moveStr = convertMoveToString(bestMove);
                        System.out.println("AI plays: " + moveStr);
                        output.write(moveStr.getBytes(), 0, moveStr.length());
                        output.flush();
                        // Update our board state after sending the move
                        // giantBoard.makeMove(bestMove[0], bestMove[1], myPlayerId);
                        // System.out.println("Board state after AI's move:\n" + giantBoard);

                    } else {
                        System.out.println("AI could not determine a move.");
                        // Handle error
                    }

                }

                // Le dernier coup est invalide
                if(cmd == '4'){
                    System.out.println("Coup precedent invalide, recalculez...");
                    // The AI needs to recalculate based on the *previous* board state.
                    // This implies we might need to store/revert the board state.
                    // For simplicity now, we'll just try finding the best move again from the current state.
                    // A more robust solution would involve board history or state cloning.

                    int[] bestMove = aiPlayer.findBestMove(giantBoard); // Recalculate
                    if (bestMove != null) {
                        String moveStr = convertMoveToString(bestMove);
                        System.out.println("AI re-plays: " + moveStr);
                        output.write(moveStr.getBytes(), 0, moveStr.length());
                        output.flush();
                        // giantBoard.makeMove(bestMove[0], bestMove[1], myPlayerId);
                    } else {
                        System.out.println("AI could not determine a corrective move.");
                        // Handle error
                    }
                }
                // La partie est terminée
                if(cmd == '5'){
                    String lastMove = dataString; // Contains the last move played
                    System.out.println("Partie Terminée. Le dernier coup joué est: " + lastMove);
                    // Optionally parse the last move and update the final board state
                    // int[] finalMoveCoords = convertStringToMoveCoords(lastMove);
                    // if (finalMoveCoords != null) {
                    //     giantBoard.makeMove(finalMoveCoords[0], finalMoveCoords[1], (myPlayerId == Board.PLAYER_X ? Board.PLAYER_O : Board.PLAYER_X)); // Assume opponent made last move
                    // }
                    System.out.println("Final Board State:\n" + giantBoard);
                    int winner = giantBoard.getGlobalWinner();
                    if (winner == myPlayerId) {
                        System.out.println("VICTORY!");
                    } else if (winner == Board.EMPTY) {
                        System.out.println("DRAW!");
                    } else {
                        System.out.println("DEFEAT!");
                    }
                    break; // End the loop
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                System.out.println("Closing connection...");
                if (input != null) input.close();
                if (output != null) output.close();
                if (MyClient != null && !MyClient.isClosed()) MyClient.close();
                if (console != null) console.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
            System.out.println("Client shut down.");
        }
    }

    // Parses the flat board string "0 0 4 ... 2 0" into the 2D array
    private static void parseBoardState(String stateString, int[][] boardArray) {
        String[] boardValues = stateString.split("\\s+"); // Split by whitespace
        int expectedValues = 9 * 9;
        if (boardValues.length != expectedValues) {
            System.err.println("Error: Received board state with " + boardValues.length + " values, expected " + expectedValues);
            // Optional: Fill boardArray with a default value (e.g., EMPTY) or handle error
            for (int i=0; i<9; i++) Arrays.fill(boardArray[i], Board.EMPTY);
            return;
        }

        int index = 0;
        for(int row = 0; row < 9; row++){ // Iterate through rows (0-8)
            for(int col = 0; col < 9; col++){ // Iterate through columns (0-8)
                try {
                    boardArray[row][col] = Integer.parseInt(boardValues[index]);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing board value at index " + index + ": " + boardValues[index]);
                    boardArray[row][col] = Board.EMPTY; // Default to empty on error
                }
                index++;
            }
        }
    }

    // Converts AI move coordinates {globalRow, globalCol} (0-8) to server string format "A1"-"I9" [cite: 42, 43]
    // Rows 0-8 map to 1-9, Columns 0-8 map to A-I
    private static String convertMoveToString(int[] moveCoords) {
        if (moveCoords == null || moveCoords.length != 2) return "A1"; // Default/error
        int globalRow = moveCoords[0]; // 0-8
        int globalCol = moveCoords[1]; // 0-8
        char colChar = (char) ('A' + globalCol);
        int rowNum = globalRow + 1;
        return "" + colChar + rowNum;
    }

    // Converts server string format "A1"-"I9" to {globalRow, globalCol} (0-8)
    private static int[] convertStringToMoveCoords(String moveStr) {
        if (moveStr == null || moveStr.length() < 2 || moveStr.length() > 2) { // e.g., D6
            return null;
        }
        moveStr = moveStr.toUpperCase();
        char colChar = moveStr.charAt(0);
        char rowChar = moveStr.charAt(1);


        if (colChar < 'A' || colChar > 'I' || rowChar < '1' || rowChar > '9') {
            return null; // Invalid format
        }

        int globalCol = colChar - 'A'; // A=0, B=1, ..., I=8
        int globalRow = Character.getNumericValue(rowChar) - 1; // '1'=1 -> 0, ..., '9'=9 -> 8


        return new int[]{globalRow, globalCol};
    }
}