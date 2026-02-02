import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Setup input/output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Send board info to client
            sendBoardInfo();
            
            // Process client commands
            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("Received: " + command);
                processCommand(command);
            }
            
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private void sendBoardInfo() {
        String info = "BOARD_INFO " + 
                     BulletinBoardSever.getBoardWidth() + " " +
                     BulletinBoardSever.getBoardHeight() + " " +
                     BulletinBoardSever.getNoteWidth() + " " +
                     BulletinBoardSever.getNoteHeight();
        
        // Add colors
        for (String color : BulletinBoardSever.getColors()) {
            info += " " + color;
        }
        
        out.println(info);
        System.out.println("Sent board info to client");
    }
    
    private void processCommand(String command) {
        // Split command into parts
        String[] parts = command.split(" ", 5); // Split into max 5 parts for POST
        
        if (parts[0].equals("POST") && parts.length == 5) {
            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                String color = parts[3];
                String message = parts[4];
                
                // For now, just echo success - you'll add proper validation later
                out.println("OK NOTE_POSTED");
                System.out.println("Processed POST: " + x + "," + y + " " + color + " \"" + message + "\"");
                
            } catch (NumberFormatException e) {
                out.println("ERROR INVALID_FORMAT Invalid coordinates");
            }
        } else {
            // Echo other commands for now
            out.println("ECHO: " + command);
        }
    }
}