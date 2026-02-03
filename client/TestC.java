import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TestC {
    public static void main(String[] args) {
        System.out.println("=== Bulletin Board Interactive Client ===");
        
        try {
            Socket socket = new Socket("localhost", 4554);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            String serverWelcome = in.readLine();
            System.out.println("Server: " + serverWelcome);
            
            System.out.println("\nConnected! You can now enter commands.");
            System.out.println("Available commands:");
            System.out.println("  POST <x> <y> <color> <message>");
            System.out.println("  GET [color=<color>] [contains=<x> <y>] [refersTo=<substring>]");
            System.out.println("  GET ALL");
            System.out.println("  GET PINS");
            System.out.println("  PIN <x> <y>");
            System.out.println("  UNPIN <x> <y>");
            System.out.println("  SHAKE");
            System.out.println("  CLEAR");
            System.out.println("  QUIT");
            System.out.println("\nExamples:");
            System.out.println("  GET color=white");
            System.out.println("  GET contains=4 6");
            System.out.println("  GET contains=4 6 refersTo=Fred");
            System.out.println("  GET ALL");
            System.out.println("\nType your command and press Enter:");
            System.out.println("----------------------------------------");
            
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.print("\nClient: ");
                String command = scanner.nextLine();
                
                if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit")) {
                    out.println("QUIT");
                    String response = in.readLine();
                    System.out.println("Server: " + response);
                    break;
                }
                
                if (command.trim().isEmpty()) {
                    continue;
                }
                
                // Send command
                out.println(command);
                
                // For GET commands, read multiple lines
                if (command.startsWith("GET")) {
                    // Read first line (header)
                    String header = in.readLine();
                    System.out.println("Server: " + header);
                    
                    if (header != null && header.startsWith("OK")) {
                        try {
                            // Parse number of items
                            String[] parts = header.split(" ");
                            if (parts.length >= 2) {
                                int count = Integer.parseInt(parts[1]);
                                
                                // Read the specified number of lines
                                for (int i = 0; i < count; i++) {
                                    String line = in.readLine();
                                    if (line != null) {
                                        System.out.println("Server: " + line);
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Not a numbered response
                        }
                    }
                } else {
                    // For non-GET commands, read single response
                    String response = in.readLine();
                    System.out.println("Server: " + response);
                }
            }
            
            scanner.close();
            socket.close();
            System.out.println("\nDisconnected from server.");
            
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}