import java.io.*;
import java.net.*;

public class BulletinBoardSever {
    private static Board board;
    
    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: java BulletinBoardServer <port> <board_width> <board_height> <note_width> <note_height> <color1> ... <colorN>");
            System.out.println("Example: java BulletinBoardServer 4554 200 100 20 10 red white green yellow");
            return;
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            int boardWidth = Integer.parseInt(args[1]);
            int boardHeight = Integer.parseInt(args[2]);
            int noteWidth = Integer.parseInt(args[3]);
            int noteHeight = Integer.parseInt(args[4]);
            
            // Get colors
            String[] colors = new String[args.length - 5];
            for (int i = 5; i < args.length; i++) {
                colors[i - 5] = args[i].toLowerCase();
            }
            
            // Create board
            board = new Board(boardWidth, boardHeight, noteWidth, noteHeight, colors);
            ClientHandler.setBoard(board);
            
            System.out.println("Starting Bulletin Board Server...");
            System.out.println("Port: " + port);
            System.out.println("Board: " + boardWidth + "x" + boardHeight);
            System.out.println("Note size: " + noteWidth + "x" + noteHeight);
            System.out.println("Colors: " + String.join(", ", colors));
            System.out.println("Server listening on port " + port);
            
            // Create server socket
            ServerSocket serverSocket = new ServerSocket(port);
            
            // Main server loop
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress());
                
                // Handle client in new thread
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format in arguments");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}