import java.io.*;
import java.net.*;


public class BulletinBoardSever {
    private static int boardWidth;
    private static int boardHeight;
    private static int noteWidth;
    private static int noteHeight;
    private static String[] colors;
    public static void main(String[] args) {
            if (args.length < 6) {
            System.out.println("Usage: java BulletinBoardServer <port> <board_width> <board_height> <note_width> <note_height> <color1> ... <colorN>");
            System.out.println("Example: java BulletinBoardServer 4554 200 100 20 10 red white green yellow");
            return;
        }
        
        try {
            // Parse arguments
            int port = Integer.parseInt(args[0]);
            boardWidth = Integer.parseInt(args[1]);
            boardHeight = Integer.parseInt(args[2]);
            noteWidth = Integer.parseInt(args[3]);
            noteHeight = Integer.parseInt(args[4]);
            
            // Get colors
            colors = new String[args.length - 5];
            for (int i = 5; i < args.length; i++) {
                colors[i - 5] = args[i];
            }
            
            System.out.println("Starting Bulletin Board Server...");
            System.out.println("Port: " + port);
            System.out.println("Board: " + boardWidth + "x" + boardHeight);
            System.out.println("Note size: " + noteWidth + "x" + noteHeight);
            System.out.println("Colors: " + String.join(", ", colors));
            
            // Create server socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            
            // Main server loop
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Handle client in new thread
                ClientHandler handler = new ClientHandler (clientSocket);
                handler.start();
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format in arguments");
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
    
    // Getter methods for board info
    public static int getBoardWidth() { return boardWidth; }
    public static int getBoardHeight() { return boardHeight; }
    public static int getNoteWidth() { return noteWidth; }
    public static int getNoteHeight() { return noteHeight; }
    public static String[] getColors() { return colors; }
}





    
    

