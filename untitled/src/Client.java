import java.io.*;
import java.net.*;
import java.util.Arrays;
class Client {
    private static GiantBoard giantBoard = new GiantBoard();
    private static AIPlayer aiPlayer;
    private static int myPlayerId;
    public static void main(String[] args) {
        String serverIP = "localhost";
        int serverPort = 8888;
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
        int[][] boardStateFromServer = new int[9][9];
        try {
            System.out.println("Connecting to server " + serverIP + ":" + serverPort);
            MyClient = new Socket(serverIP, serverPort);
            System.out.println("Connected to server!");
            input = new BufferedInputStream(MyClient.getInputStream());
            output = new BufferedOutputStream(MyClient.getOutputStream());
            while (true) {
                char cmd = (char) input.read();
                System.out.println("Received command: " + cmd);
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
                if (cmd == '1') {
                    myPlayerId = Board.PLAYER_X;
                    aiPlayer = new AIPlayer(myPlayerId);
                    System.out.println("New game: Playing as X.");
                    parseBoardState(dataString, boardStateFromServer);
                    giantBoard.updateBoard(boardStateFromServer);
                    System.out.println("Board state updated:");
                    System.out.println(giantBoard);
                    int[] bestMove = aiPlayer.findBestMove(giantBoard);
                    if (bestMove != null) {
                        String moveStr = convertMoveToString(bestMove);
                        System.out.println("AI chooses move: " + moveStr);
                        output.write(moveStr.getBytes(), 0, moveStr.length());
                        output.flush();
                    } else {
                        System.out.println("AI could not find a move.");
                    }
                }
                if (cmd == '2') {
                    myPlayerId = Board.PLAYER_O;
                    aiPlayer = new AIPlayer(myPlayerId);
                    System.out.println("New game: Playing as O. Waiting for opponent's move.");
                    parseBoardState(dataString, boardStateFromServer);
                    giantBoard.updateBoard(boardStateFromServer);
                    System.out.println("Board state updated:");
                    System.out.println(giantBoard);
                }
                else if (cmd == '3') {
                    System.out.println("Server requests next move.");
                    String lastMoveStr = dataString;
                    System.out.println("Opponent move: " + lastMoveStr);
                    int[] opponentMoveCoords = convertStringToMoveCoords(lastMoveStr);
                    if (opponentMoveCoords != null) {
                        int opponentGlobalRow = opponentMoveCoords[0];
                        int opponentGlobalCol = opponentMoveCoords[1];
                        int opponentPlayer = (myPlayerId == Board.PLAYER_X) ? Board.PLAYER_O : Board.PLAYER_X;
                        giantBoard.makeMove(opponentGlobalRow, opponentGlobalCol, opponentPlayer);
                        System.out.println("Board updated with opponent move:");
                        System.out.println(giantBoard);
                        int nextLocalRow = opponentGlobalRow % 3;
                        int nextLocalCol = opponentGlobalCol % 3;
                        giantBoard.setNextBoardFromLastMove(nextLocalRow, nextLocalCol);
                        System.out.println("Next board to play set to: " + (giantBoard.getNextLocalBoardRow() == -1 ? "Any" : giantBoard.getNextLocalBoardRow() + "," + giantBoard.getNextLocalBoardCol()));
                    } else {
                        System.out.println("Opponent move not parsed, setting play anywhere.");
                        giantBoard.setNextBoardFromLastMove(-1, -1);
                    }
                    int[] bestMove = aiPlayer.findBestMove(giantBoard);
                    if (bestMove != null) {
                        String moveStr = convertMoveToString(bestMove);
                        System.out.println("AI chooses move: " + moveStr);
                        output.write(moveStr.getBytes(), 0, moveStr.length());
                        output.flush();
                        System.out.println("Move sent to server.");
                    } else {
                        System.out.println("AI could not find a move.");
                    }
                }
                if (cmd == '4') {
                    System.out.println("Previous move invalid. Recalculating move.");
                    int[] bestMove = aiPlayer.findBestMove(giantBoard);
                    if (bestMove != null) {
                        String moveStr = convertMoveToString(bestMove);
                        System.out.println("AI recalculates move: " + moveStr);
                        output.write(moveStr.getBytes(), 0, moveStr.length());
                        output.flush();
                    } else {
                        System.out.println("AI could not find a corrective move.");
                    }
                }
                if (cmd == '5') {
                    System.out.println("Game over command received.");
                    String dummy = console.readLine();
                    output.write(dummy.getBytes(), 0, dummy.length());
                    output.flush();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        } finally {
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
    }
    private static void parseBoardState(String stateString, int[][] boardArray) {
        String[] boardValues = stateString.split("\\s+");
        int expectedValues = 81;
        if (boardValues.length != expectedValues) {
            for (int i = 0; i < 9; i++) Arrays.fill(boardArray[i], Board.EMPTY);
            return;
        }
        int index = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                try {
                    boardArray[row][col] = Integer.parseInt(boardValues[index]);
                } catch (NumberFormatException e) {
                    boardArray[row][col] = Board.EMPTY;
                }
                index++;
            }
        }
    }
    private static String convertMoveToString(int[] moveCoords) {
        if (moveCoords == null || moveCoords.length != 2) return "A1";
        int globalRow = moveCoords[0];
        int globalCol = moveCoords[1];
        char colChar = (char) ('A' + globalCol);
        int rowNum = globalRow + 1;
        return "" + colChar + rowNum;
    }
    private static int[] convertStringToMoveCoords(String moveStr) {
        if (moveStr == null || moveStr.length() != 2) return null;
        moveStr = moveStr.toUpperCase();
        char colChar = moveStr.charAt(0);
        char rowChar = moveStr.charAt(1);
        if (colChar < 'A' || colChar > 'I' || rowChar < '1' || rowChar > '9') return null;
        int globalCol = colChar - 'A';
        int globalRow = Character.getNumericValue(rowChar) - 1;
        return new int[]{globalRow, globalCol};
    }
}
