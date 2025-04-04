import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        AIPlayer aiPlayer = new AIPlayer();
        GlobalBoard globalBoard = new GlobalBoard();
        // Determine your mark (assume playing white by default; adjust as needed)
        int myMark = 4; // PLAYER_X is 4, PLAYER_O is 2

        try {
            socket = new Socket("localhost", 8888);
            input = new BufferedInputStream(socket.getInputStream());
            output = new BufferedOutputStream(socket.getOutputStream());
            // We no longer need console input since AI will choose moves.
            // BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            
            while (true) {
                char cmd = (char) input.read();
                System.out.println("Received command: " + cmd);

                if (cmd == '1') {  
                    // Start game as white; server sends initial board configuration.
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String boardStr = new String(aBuffer).trim();
                    System.out.println("Initial board configuration: " + boardStr);
                    
                    // Parse the board configuration (assumes 81 space-separated numbers)
                    int[][] boardArray = parseBoardString(boardStr);
                    globalBoard = convertIntBoardToGlobalBoard(boardArray);
                    
                    // Generate AI move (no forced local board at first move)
                    GlobalMove move = aiPlayer.getBestMove(globalBoard, null, null, myMark);
                    System.out.println("AI selects move: " + move);
                    String moveStr = moveToString(move);
                    output.write(moveStr.getBytes());
                    output.flush();
                    
                    // Update our board with our move.
                    int globalRow = move.getLocalBoardRow() * 3 + move.getCellRow();
                    int globalCol = move.getLocalBoardCol() * 3 + move.getCellCol();
                    globalBoard.playMove(globalRow, globalCol, myMark);
                } else if (cmd == '2') {
                    // Start game as black; server sends initial board configuration.
                    System.out.println("Starting game as Black. Waiting for white's move.");
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String boardStr = new String(aBuffer).trim();
                    System.out.println("Initial board configuration: " + boardStr);
                    
                    int[][] boardArray = parseBoardString(boardStr);
                    globalBoard = convertIntBoardToGlobalBoard(boardArray);
                    // Since we are black, we wait for the opponent’s move (handled in cmd '3').
                } else if (cmd == '3') {
                    // Server requests our next move and provides the opponent's last move.
                    byte[] aBuffer = new byte[16];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String lastMoveStr = new String(aBuffer).trim();
                    System.out.println("Opponent's last move: " + lastMoveStr);
                    
                    // Update our GlobalBoard with the opponent's move.
                    int[] oppCoords = parseMoveString(lastMoveStr); // Assumes "row col" format.
                    int opponentMark = (myMark == 4) ? 2 : 4;
                    globalBoard.playMove(oppCoords[0], oppCoords[1], opponentMark);
                    
                    // Determine the forced local board based on the opponent's move.
                    int activeLocalRow = oppCoords[0] % 3;
                    int activeLocalCol = oppCoords[1] % 3;
                    if (globalBoard.getLocalBoard(activeLocalRow, activeLocalCol).getState() != LocalBoard.BoardState.ONGOING) {
                        activeLocalRow = -1;
                        activeLocalCol = -1;
                    }
                    
                    // Generate AI move using the forced board if available.
                    GlobalMove move = aiPlayer.getBestMove(globalBoard, 
                        (activeLocalRow == -1) ? null : activeLocalRow, 
                        (activeLocalCol == -1) ? null : activeLocalCol, 
                        myMark);
                    System.out.println("AI selects move: " + move);
                    String moveStr = moveToString(move);
                    
                    // Update our board with our move.
                    int globalRow = move.getLocalBoardRow() * 3 + move.getCellRow();
                    int globalCol = move.getLocalBoardCol() * 3 + move.getCellCol();
                    globalBoard.playMove(globalRow, globalCol, myMark);
                    
                    output.write(moveStr.getBytes());
                    output.flush();
                } else if (cmd == '4') {
                    // The last move was invalid.
                    System.out.println("Invalid move. AI will choose a new move.");
                    GlobalMove move = aiPlayer.getBestMove(globalBoard, null, null, myMark);
                    String moveStr = moveToString(move);
                    
                    // Update our board with our new move.
                    int globalRow = move.getLocalBoardRow() * 3 + move.getCellRow();
                    int globalCol = move.getLocalBoardCol() * 3 + move.getCellCol();
                    globalBoard.playMove(globalRow, globalCol, myMark);
                    
                    output.write(moveStr.getBytes());
                    output.flush();
                } else if (cmd == '5') {
                    // Game over.
                    byte[] aBuffer = new byte[16];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String lastMoveStr = new String(aBuffer).trim();
                    System.out.println("Game over. Final move: " + lastMoveStr);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (socket != null) { socket.close(); }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    // Helper method to parse a board configuration string into a 9x9 int array.
    // Assumes the board string consists of 81 space-separated numbers.
    private static int[][] parseBoardString(String boardStr) {
        int[][] board = new int[9][9];
        String[] tokens = boardStr.split(" ");
        int index = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = Integer.parseInt(tokens[index++]);
            }
        }
        return board;
    }
    
    // Helper method to convert a 9x9 int array into a GlobalBoard by replaying moves.
    private static GlobalBoard convertIntBoardToGlobalBoard(int[][] boardArray) {
        GlobalBoard gb = new GlobalBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int mark = boardArray[i][j];
                if (mark != 0) {
                    gb.playMove(i, j, mark);
                }
            }
        }
        return gb;
    }
    
    // Helper method to parse a move string into global coordinates.
    // Expects a format like "row col" where row and col are 0-indexed integers.
    private static int[] parseMoveString(String moveStr) {
        String[] parts = moveStr.split(" ");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new int[]{row, col};
    }
    
    // Helper method to convert a GlobalMove into a string.
    // Here we represent the move as "row col" using global coordinates (0-8).
    private static String moveToString(GlobalMove move) {
        int globalRow = move.getLocalBoardRow() * 3 + move.getCellRow();
        int globalCol = move.getLocalBoardCol() * 3 + move.getCellCol();
        return globalRow + "" + globalCol;
    }
}
