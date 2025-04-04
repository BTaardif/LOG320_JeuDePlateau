import java.io.*;
import java.net.*;
import java.util.ArrayList;
// No need to import Arrays if not used directly here
import java.util.HashMap;
import java.util.Map;

// Assuming Board.java (containing inner LocalBoard & Move classes),
// Mark.java, and CPUPlayer.java are in the correct place (e.g., same directory or classpath)

public class Client {

    // Use the Board class for Ultimate Tic-Tac-Toe
    private static GiantBoard ultimateBoard = new GiantBoard(); // Use Board, not GiantBoard
    // Use the CPUPlayer that works with Board and Mark enum
    private static CPUPlayer cpu; // Use CPUPlayer
    // Use the Mark enum for player representation
    private static Mark myMark;
    private static Mark opponentMark;

    // Mappings for server communication (consistent with previous example)
    private static final Map<Character, Integer> colToServerCol = new HashMap<>();
    private static final Map<Integer, Character> serverColToCol = new HashMap<>();
    static {
        colToServerCol.put('A', 0); colToServerCol.put('B', 1); colToServerCol.put('C', 2);
        colToServerCol.put('D', 3); colToServerCol.put('E', 4); colToServerCol.put('F', 5);
        colToServerCol.put('G', 6); colToServerCol.put('H', 7); colToServerCol.put('I', 8);

        for (Map.Entry<Character, Integer> entry : colToServerCol.entrySet()) {
            serverColToCol.put(entry.getValue(), entry.getKey());
        }
    }

    public static void main(String[] args) {
        String serverIP = "localhost";
        int serverPort = 8888;
        // --- Argument parsing ---
        if (args.length > 0) {
            serverIP = args[0];
        }
        if (args.length > 1) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port provided, using default 8888");
            }
        }

        Socket MyClient = null;
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Connecting to server " + serverIP + ":" + serverPort);
            MyClient = new Socket(serverIP, serverPort);
            System.out.println("Connected to server!");
            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());

            while (true) {
                int commandCode = input.read();
                 if (commandCode == -1) {
                     System.out.println("Server disconnected.");
                     break;
                 }
                char cmd = (char) commandCode;
                System.out.println("Received command: " + cmd);

                // --- Read associated data ---
                byte[] aBuffer = new byte[1024];
                int size = 0;
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                if (input.available() > 0) {
                    size = input.read(aBuffer, 0, Math.min(aBuffer.length, input.available()));
                }
                String dataString = "";
                if (size > 0) {
                    dataString = new String(aBuffer, 0, size).trim();
                }
                System.out.println("Data received: \"" + dataString + "\"");

                // --- Handle Commands ---
                if (cmd == '1') { // Start as Player 1 (X)
                    myMark = Mark.X;
                    opponentMark = Mark.O;
                    cpu = new CPUPlayer(myMark); // Initialize CPUPlayer
                    System.out.println("New game: Playing as X.");
                    updateBoardFromDataString(dataString);
                    System.out.println("Board state updated:");
                    System.out.println(ultimateBoard);
                    // Player X makes the first move
                    Move bestMove = getAIMove(); // Use Board.Move type
                    sendMoveToServer(bestMove, output);

                } else if (cmd == '2') { // Start as Player 2 (O)
                    myMark = Mark.O;
                    opponentMark = Mark.X;
                    cpu = new CPUPlayer(myMark); // Initialize CPUPlayer
                    System.out.println("New game: Playing as O. Waiting for opponent's move.");
                    updateBoardFromDataString(dataString);
                    System.out.println("Board state updated:");
                    System.out.println(ultimateBoard);
                    // Player O waits for command '3'

                } else if (cmd == '3') { // Server requests next move
                    System.out.println("Server requests next move.");
                    String lastMoveStr = dataString;
                    System.out.println("Last move reported by server: " + lastMoveStr);

                    Move opponentMove = convertStringToMove(lastMoveStr); // Use Board.Move type
                    if (opponentMove != null) {
                        updateBoardConstraint(opponentMove); // Update constraint based on opponent move
                    } else if (!"A0".equals(lastMoveStr)) {
                        System.out.println("Warning: Could not parse last move '" + lastMoveStr + "' to update constraint.");
                    }

                    System.out.println("Board state before AI move:");
                    System.out.println(ultimateBoard);
                    System.out.println("Next board constraint: " + ultimateBoard.getNextLocalBoardRow() + "," + ultimateBoard.getNextLocalBoardCol());

                    Move bestMove = getAIMove(); // Use Board.Move type
                    sendMoveToServer(bestMove, output);

                } else if (cmd == '4') { // Last move was invalid
                    System.out.println("Previous move invalid. Recalculating move.");
                    System.out.println("Board state before retry:");
                    System.out.println(ultimateBoard);
                    Move bestMove = getAIMove(); // Use Board.Move type
                    sendMoveToServer(bestMove, output);

                } else if (cmd == '5') { // Game Over
                    System.out.println("Game over command received.");
                    System.out.println("Final reported move: " + dataString);
                    LocalBoard.BoardState finalState = ultimateBoard.getOverallGameState(); // Use correct class name
                    System.out.println("Final game state: " + finalState);
                    // Print winner message...

                    System.out.println("Press Enter to acknowledge game over.");
                    String dummy = console.readLine();
                    output.write(dummy.getBytes(), 0, dummy.length());
                    output.flush();
                    break;
                } else {
                     System.out.println("Unknown command: " + cmd);
                }
            } // End while loop

        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // --- Resource closing ---
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (MyClient != null && !MyClient.isClosed()) MyClient.close();
                if (console != null) console.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
            System.out.println("Client shut down.");
        }
    } // End main method

    // --- Helper Methods ---

    // Wrapper to get AI move
    private static Move getAIMove() { // Return type is Board.Move
        if (cpu == null) {
            System.err.println("ERROR: CPUPlayer not initialized!");
            return null;
        }
        System.out.println("AI (" + myMark + ") is thinking...");
        // Use the CPUPlayer's method - returns ArrayList<Board.Move>
        ArrayList<Move> bestMoves = cpu.getNextMoveAB(ultimateBoard); // Use Board.Move

        if (bestMoves == null || bestMoves.isEmpty()) {
            System.err.println("Error: AI returned no moves!");
            return null;
        }
        return bestMoves.get(0); // Return Board.Move object
    }

    // Wrapper to send move to server and update internal board
    private static void sendMoveToServer(Move move, BufferedOutputStream output) throws IOException { // Parameter is Board.Move
        if (move == null) {
             System.out.println("AI could not find a move. Sending default A1.");
             String moveStr = "A1";
             output.write(moveStr.getBytes(), 0, moveStr.length());
             output.flush();
         } else {
             // Apply the move to our internal board FIRST
             if (!ultimateBoard.play(move, myMark)) { // Use the Board.Move object
                  System.err.println("FATAL: AI generated move " + move + " failed internal validation!");
                  String moveStr = "A1"; // Fallback
                  output.write(moveStr.getBytes(), 0, moveStr.length());
                  output.flush();
             } else {
                 // Convert valid internal move to server string format
                 String moveStr = convertMoveToString(move); // Use the Board.Move object
                 System.out.println("AI chooses move: " + move + " -> " + moveStr);
                 output.write(moveStr.getBytes(), 0, moveStr.length());
                 output.flush();
                 System.out.println("Move sent to server. Board state after move:");
                 System.out.println(ultimateBoard);
             }
         }
    }

    // Parses the server's 81-number string and updates the Board object
    private static void updateBoardFromDataString(String stateString) {
        String[] boardValues = stateString.trim().split("\\s+");
        int expectedValues = 81;
        if (boardValues.length != expectedValues) {
             System.err.println("Error: Invalid board string length " + boardValues.length + " received. Cannot update board.");
            return;
        }
        int index = 0;
        boolean stateChanged = false;
        for (int gR = 0; gR < 3; gR++) {
            for (int gC = 0; gC < 3; gC++) {
                 // Access LocalBoard correctly (assuming it's an inner class or imported)
                 LocalBoard localBoard = ultimateBoard.getLocalBoard(gR, gC);
                if (localBoard == null) {
                    System.err.println("Error: Null LocalBoard at " + gR + "," + gC);
                    continue;
                }
                for (int lR = 0; lR < 3; lR++) {
                    for (int lC = 0; lC < 3; lC++) {
                        try {
                            int val = Integer.parseInt(boardValues[index]);
                            Mark serverMark = Mark.EMPTY;
                            if (val == 2) serverMark = Mark.O;
                            if (val == 4) serverMark = Mark.X;

                            // Use direct setMark (needs implementation in LocalBoard)
                            if (localBoard.getMark(lR, lC) != serverMark) {
                                 localBoard.setMark(lR, lC, serverMark); // IMPLEMENT THIS in LocalBoard
                                 stateChanged = true;
                            }
                        } catch (NumberFormatException e) {
                             System.err.println("Error parsing board value at index " + index + ": '" + boardValues[index] + "'");
                             if (localBoard.getMark(lR, lC) != Mark.EMPTY) {
                                 localBoard.setMark(lR, lC, Mark.EMPTY);
                                 stateChanged = true;
                             }
                        }
                        index++;
                    }
                }
            }
        }
        // IMPORTANT: After syncing marks, recalculate all board states
        if (stateChanged) {
            System.out.println("Board marks synced from server. Recalculating all states...");
            ultimateBoard.recalculateAllStates(); // IMPLEMENT THIS in Board
        }
    }

    // Converts a Board.Move object to the server's "A1"-"I9" string format
    private static String convertMoveToString(Move move) { // Parameter is Board.Move
        if (move == null) return "A1";
        int gRow = move.getGlobalRow();
        int gCol = move.getGlobalCol();
        int lRow = move.getLocalRow();
        int lCol = move.getLocalCol();
        int globalIndex = gRow * 3 + gCol;
        if (!serverColToCol.containsKey(globalIndex)) {
             System.err.println("Error converting move: Invalid global index " + globalIndex);
             return "A1";
        }
        char globalChar = serverColToCol.get(globalIndex);
        int localNum = lRow * 3 + lCol + 1;
        if (localNum < 1 || localNum > 9) {
             System.err.println("Error converting move: Invalid local num " + localNum);
             return "A1";
        }
        return "" + globalChar + localNum;
    }

    // Converts the server's "A1"-"I9" string format to a Board.Move object
    private static Move convertStringToMove(String moveStr) { // Return type is Board.Move
        if (moveStr == null || moveStr.length() != 2) return null;
        moveStr = moveStr.toUpperCase();
        char globalChar = moveStr.charAt(0);
        char localNumChar = moveStr.charAt(1);
        if (!colToServerCol.containsKey(globalChar)) return null;
        int globalIndex = colToServerCol.get(globalChar);
        int gRow = globalIndex / 3;
        int gCol = globalIndex % 3;
        int localNum;
        try {
            localNum = Integer.parseInt(String.valueOf(localNumChar));
        } catch (NumberFormatException e) { return null; }
        if (localNum < 1 || localNum > 9) return null;
        int localIndex = localNum - 1;
        int lRow = localIndex / 3;
        int lCol = localIndex % 3;
        // Use the Board.Move constructor
        return new Move(gRow, gCol, lRow, lCol);
    }

    // Updates the board's constraint based on the *local* coords of the last move
    private static void updateBoardConstraint(Move lastMove) { // Parameter is Board.Move
        if (lastMove == null) return;
        int nextGlobalRow = lastMove.getLocalRow();
        int nextGlobalCol = lastMove.getLocalCol();
        LocalBoard targetBoard = ultimateBoard.getLocalBoard(nextGlobalRow, nextGlobalCol); // Use correct class name
        if (targetBoard != null && targetBoard.getState() != LocalBoard.BoardState.ONGOING) {
             System.out.println("Constraint calc: Target board ("+nextGlobalRow+","+nextGlobalCol+") finished. Setting next move: ANYWHERE");
             ultimateBoard.setNextLocalBoard(-1, -1); // IMPLEMENT THIS in Board
        } else {
             System.out.println("Constraint calc: Setting next move constraint to board ("+nextGlobalRow+","+nextGlobalCol+")");
             ultimateBoard.setNextLocalBoard(nextGlobalRow, nextGlobalCol); // IMPLEMENT THIS in Board
        }
    }

} // End Client class