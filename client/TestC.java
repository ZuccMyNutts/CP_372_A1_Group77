import java.io.*;
import java.net.*;

public class TestC {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 4554);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read board info
            String boardInfo = in.readLine();
            System.out.println("Server: " + boardInfo);
            
            // Test POST command exactly as in spec
            out.println("POST 10 10 white Project meeting at noon");
            String response = in.readLine();
            System.out.println("Response: " + response);
            
            // Test GET command
            out.println("GET color=white");
            response = in.readLine();
            System.out.println("Response: " + response);
            
            socket.close();
            
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
    
