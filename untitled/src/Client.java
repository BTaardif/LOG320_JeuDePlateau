import java.io.*;
import java.net.*;
import java.util.Arrays;

class Client {

    private static GlobalBoard gameBoard = new GlobalBoard();
    private static CPUPlayer aiPlayer;
    private static int myMark; // Board.X or Board.O
    private static int opponentMark;
    private static GlobalMove lastOpponentMove = null; // Track opponent's last move

    public static void main(String[] args) {

        String serverAddress = "localhost"; // Default
        int serverPort = 8888; // Default

        // Optional: Parse command line arguments for server address/port
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

            while (true) { // Use boolean flag for clearer loop exit
                System.out.println("\nWaiting for server command...");
                int command = input.read(); // Read single byte command

                if (command == -1) {
                    System.out.println("Server disconnected.");
                    break;
                }

                char cmd = (char) command;
                System.out.println("Received command: " + cmd);

                // Command '1': Start game as Player 1 (X)
                if (cmd == '1') {
                    myMark = Board.X;
                    opponentMark = Board.O;
                    aiPlayer = new CPUPlayer(myMark);
                    System.out.println("Starting new game as Player 1 (X).");
                    parseBoardState(input); // Read initial board state
                    gameBoard.printDetailedGlobalBoard(); // Print initial board (should be empty)

                    System.out.println("Making first move...");
                    GlobalMove aiMove = aiPlayer.findBestMove(gameBoard, null); // No opponent move yet
                    if (aiMove != null) {
                        // Apply AI move to our internal board *before* sending
                        boolean played = gameBoard.play(aiMove, myMark);
                        if (!played)
                            System.err.println("CRITICAL: AI Generated an invalid first move!"); // Should not happen

                        sendMoveToServer(output, aiMove);
                        gameBoard.printDetailedGlobalBoard(); // Print board after our move
                    } else {
                        System.err.println("AI could not determine a first move.");
                        // Handle error - maybe send a default move or exit?
                        break;
                    }
                    lastOpponentMove = null; // Reset opponent move for next turn logic

                }
                // Command '2': Start game as Player 2 (O)
                else if (cmd == '2') {
                    myMark = Board.O;
                    opponentMark = Board.X;
                    aiPlayer = new CPUPlayer(myMark);
                    System.out.println("Starting new game as Player 2 (O).");
                    System.out.println("Waiting for Player 1's first move.");
                    parseBoardState(input); // Read initial board state (will be updated by server again)
                    // Note: The server will likely send command '3' next with Player 1's move.
                    lastOpponentMove = null; // Opponent hasn't moved yet
                    // Do not print board here, wait for command '3'

                }
                // Command '3': Server requests next move, provides opponent's last move
                else if (cmd == '3') {
                    String opponentMoveStr = parseOpponentMove(input); // Read opponent's move string
                    System.out.println("Opponent played: " + opponentMoveStr);

                    // Important: Parse and apply the opponent's move *before* AI calculates its
                    // move
                    lastOpponentMove = parseMoveString(opponentMoveStr); // Update opponent's last move

                    if (lastOpponentMove != null) {
                        // Validate if the received move is playable on our current board state
                        // Note: Server *should* send valid moves, but good to check
                        if (gameBoard.isLocalBoardClosed(lastOpponentMove.getGlobalRow(),
                                lastOpponentMove.getGlobalCol()) ||
                                gameBoard.boards[lastOpponentMove.getGlobalRow()][lastOpponentMove.getGlobalCol()]
                                        .getCell(lastOpponentMove.getLocalRow(),
                                                lastOpponentMove.getLocalCol()) != Board.EMPTY) {
                            System.err.println("WARNING: Server sent an opponent move (" + opponentMoveStr
                                    + ") that seems invalid on our board!");
                            // Potentially request board resync or handle error
                        }

                        boolean played = gameBoard.play(lastOpponentMove, opponentMark);
                        if (!played) {
                            System.err.println("ERROR: Failed to apply valid opponent move " + opponentMoveStr
                                    + " to internal board!");
                            // This indicates a serious state mismatch
                            gameBoard.printDetailedGlobalBoard(); // Print board state for debugging
                            break; // Exit on critical error
                        }
                        System.out.println("Board state after opponent's move:");
                        gameBoard.printDetailedGlobalBoard();
                    } else if (!opponentMoveStr.equals("A0") && !opponentMoveStr.trim().isEmpty()) {
                        // A0 is expected for the very first prompt for X, ignore other invalid formats
                        System.err.println("Could not parse opponent move: " + opponentMoveStr);
                    }

                    // Now, calculate and send AI's move
                    System.out.println("Calculating AI move...");
                    GlobalMove aiMove = aiPlayer.findBestMove(gameBoard, lastOpponentMove);
                    if (aiMove != null) {
                        // Apply AI move to our internal board *before* sending
                        boolean played = gameBoard.play(aiMove, myMark);
                        if (!played) {
                            System.err.println(
                                    "CRITICAL: AI generated an invalid move: " + CPUPlayer.moveToString(aiMove));
                            // Attempt recovery or exit
                            break; // Exit on critical error
                        }

                        sendMoveToServer(output, aiMove);
                        System.out.println("Board state after AI's move:");
                        gameBoard.printDetailedGlobalBoard();
                    } else {
                        System.err.println("AI could not determine a move.");
                        // Handle error - maybe send a default move or exit?
                        break;
                    }

                }
                // Command '4': Last move sent was invalid
                else if (cmd == '4') {
                    System.err.println("!!!! Server reported last move was invalid !!!!");


                    System.out.println("Attempting to recalculate move...");
                    // Re-use the *previous* lastOpponentMove to determine valid moves now
                    GlobalMove aiMove = aiPlayer.findBestMove(gameBoard, lastOpponentMove);
                    if (aiMove != null) {
                        // DO NOT apply the move locally again if the server rejected it. Just send.
                        // boolean played = gameBoard.play(aiMove, myMark); // No!
                        sendMoveToServer(output, aiMove);
                        // We assume the server will accept this one. If not, we might loop.
                    } else {
                        System.err.println("AI could not determine a recovery move.");
                        break;
                    }

                }
                else if (cmd == '5') {
                    String finalMoveStr = parseOpponentMove(input); // Read the very last move played
                    System.out.println("Game Over! Last move played was: " + finalMoveStr);

                    // Apply the final move to see the end state
                    GlobalMove finalMove = parseMoveString(finalMoveStr);
                    if (finalMove != null) {
                        // Determine who played last based on whose turn it *would* have been
                        int lastPlayer = (aiPlayer.findBestMove(gameBoard, finalMove) == null) ? myMark : opponentMark;
                    }

                    int winner = gameBoard.checkGlobalWinner();
                    if (winner == myMark)
                        System.out.println("AI WINS!");
                    else if (winner == opponentMark)
                        System.out.println("Opponent Wins.");
                    else
                        System.out.println("It's a DRAW!");

                    break; // Exit game loop
                } else {
                    System.out.println("Unknown command from server: " + cmd);
                    // Skip remaining bytes if any?
                    if (input.available() > 0) {
                        byte[] buffer = new byte[input.available()];
                        input.read(buffer, 0, buffer.length);
                        System.out.println("  Skipped extra data: " + new String(buffer));
                    }
                }
            } // End while loop

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources (though socket closing isn't explicitly shown in original)
            // try { if (MyClient != null) MyClient.close(); } catch (IOException e) {}
            System.out.println("Client shutting down.");
        }
    }

    // Helper to read and parse the full board state (81 numbers)
    private static void parseBoardState(BufferedInputStream input) throws IOException {
        // Server sends 81 numbers separated by spaces, potentially followed by
        // newline/other chars
        // Need to read until we have 81 numbers or encounter issues.
        // A simple approach is to read a large buffer and parse.
        // Warning: input.available() can be unreliable. Reading byte-by-byte until
        // enough data or a clear delimiter is safer but more complex. Let's try buffer.

        byte[] buffer = new byte[2048]; // Adjust size as needed, should be > 81*2 + spaces
        int bytesRead = 0;
        int attempts = 0;
        // Read in chunks until we likely have the full board string
        while (bytesRead < 81 * 2 && attempts < 5) { // Need at least 81 digits + 80 spaces
            if (input.available() > 0) {
                int readNow = input.read(buffer, bytesRead, buffer.length - bytesRead);
                if (readNow > 0)
                    bytesRead += readNow;
            } else {
                // Wait briefly if no data is available, server might be slow
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
        System.out.println("Received board string (raw): \"" + boardString + "\""); // Debug output

        String[] boardValues = boardString.split("\\s+"); // Split by one or more spaces

        if (boardValues.length < 81) {
            System.err.println("Error: Received incomplete board state. Expected 81 values, got " + boardValues.length);
            System.err.println("Values: " + Arrays.toString(boardValues));
            // Might need to read more data or handle error
            return;
        }

        int index = 0;
        for (int gRow = 0; gRow < 3; gRow++) {
            for (int lRow = 0; lRow < 3; lRow++) {
                for (int gCol = 0; gCol < 3; gCol++) {
                    for (int lCol = 0; lCol < 3; lCol++) {
                        try {
                            int piece = Integer.parseInt(boardValues[index]);
                            // Need to place the piece on the correct local board
                            gameBoard.boards[gRow][gCol].play(new Move(lRow, lCol), piece); // Directly set piece
                            index++;
                        } catch (NumberFormatException e) {
                            System.err
                                    .println("Error parsing board value at index " + index + ": " + boardValues[index]);
                            return; // Stop parsing on error
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.err.println("Error: Ran out of board values at index " + index);
                            return;
                        }
                    }
                }
            }
        }
        System.out.println("Successfully parsed board state.");
        // After parsing the raw state, update the winners array based on the parsed
        // boards
        gameBoard.updateAllLocalWinners();
    }

    // Helper to read opponent move string (e.g., "D6") after command '3' or '5'
    private static String parseOpponentMove(BufferedInputStream input) throws IOException {
        // Server sends the move string, e.g., " D6" (note potential leading space)
        // Read a small buffer, expect move like " D6\n" or similar.
        byte[] buffer = new byte[16]; // More than enough for " D6\n"
        int bytesRead = 0;
        int attempts = 0;
        // Read until we get *some* data or timeout
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
        // Handle the special "A0" case for the first move prompt for player X
        if (moveStr.equals("A0"))
            return "A0";

        // Basic validation: Should be 2 chars, Letter A-I, Digit 1-9
        if (moveStr.length() == 2 && moveStr.charAt(0) >= 'A' && moveStr.charAt(0) <= 'I' && moveStr.charAt(1) >= '1'
                && moveStr.charAt(1) <= '9') {
            return moveStr;
        } else {
            System.err.println("Warning: Received potentially invalid move format: \"" + moveStr + "\"");
            return moveStr; // Return raw string for now, parsing logic will handle later
        }
    }

    // Helper to parse move string ("A1" - "I9") into GlobalMove object
    public static GlobalMove parseMoveString(String moveStr) {
        if (moveStr == null || moveStr.length() != 2 || moveStr.equals("A0")) {
            // A0 is the special invalid move sent with the first '3' command to player X
            return null;
        }
        moveStr = moveStr.trim().toUpperCase();

        char colChar = moveStr.charAt(0);
        char rowChar = moveStr.charAt(1);

        if (colChar < 'A' || colChar > 'I' || rowChar < '1' || rowChar > '9') {
            System.err.println("Error parsing move string: Invalid format '" + moveStr + "'");
            return null;
        }

        int overallCol = colChar - 'A'; // 0-8
        int overallRow = rowChar - '1'; // 0-8

        int gRow = overallRow / 3;
        int lRow = overallRow % 3;
        int gCol = overallCol / 3;
        int lCol = overallCol % 3;

        return new GlobalMove(gRow, gCol, lRow, lCol);
    }

    // Helper to send the AI's move to the server
    private static void sendMoveToServer(BufferedOutputStream output, GlobalMove move) throws IOException {
        String moveStr = CPUPlayer.moveToString(move);
        System.out.println("Sending move: " + moveStr);
        output.write(moveStr.getBytes());
        output.flush(); // Ensure data is sent immediately
    }
}