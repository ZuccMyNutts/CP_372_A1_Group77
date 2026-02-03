import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static Board board;
    
    public static void setBoard(Board b) {
        board = b;
    }
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println("Welcome to Bulletin Board Server! Ready for commands.");
            System.out.println("New client connected: " + socket.getInetAddress());
            
            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("[Client] Command: " + command);
                
                String response = processCommand(command);
                out.println(response);
                System.out.println("[Server] Response: " + response);
                
                if (command.equals("QUIT")) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Connection closed.");
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
private String processCommand(String command) {
    String[] parts = command.split(" ");
    
    if (parts.length == 0) {
        return "ERROR INVALID_FORMAT";
    }
    
    String cmd = parts[0].toUpperCase();
    
    // Check for multi-word commands first
    if (command.toUpperCase().equals("GET PINS") || command.toUpperCase().equals("GET_PINS")) {
        return handleGetPins();
    }
    
    if (command.toUpperCase().equals("GET ALL") || command.toUpperCase().equals("GET_ALL")) {
        return handleGetAll();
    }
    // Handle single-word commands
    switch (cmd) {
        case "POST":
            return handlePost(parts);
        case "GET":
            return handleGet(command);
        case "PIN":
            return handlePin(parts);
        case "UNPIN":
            return handleUnpin(parts);
        case "SHAKE":
            return handleShake();
        case "CLEAR":
            return handleClear();
        case "QUIT":
            return "GOODBYE";
        default:
            return "ERROR UNKNOWN_COMMAND";
    }
}
    
    private String handleGet(String command) {
        // Parse filters from command
        String colorFilter = null;
        String containsFilter = null;
        String refersToFilter = null;
        
        // Remove "GET " from beginning
        String filterString = command.substring(4).trim();
        
        if (filterString.isEmpty() || filterString.equalsIgnoreCase("ALL")) {
            return handleGetAll();
        }
        
        // Parse filters like: color=white contains=4 6 refersTo=Fred
        // We need to handle the space in "contains=x y" specially
        String[] parts = filterString.split("\\s+");
        StringBuilder currentFilter = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            
            if (part.contains("=")) {
                // This is a new filter key
                // First, process any accumulated filter value
                if (currentFilter.length() > 0) {
                    String filterValue = currentFilter.toString().trim();
                    if (filterValue.startsWith("color=")) {
                        colorFilter = filterValue.substring(6);
                    } else if (filterValue.startsWith("contains=")) {
                        containsFilter = filterValue.substring(9);
                    } else if (filterValue.startsWith("refersTo=")) {
                        refersToFilter = filterValue.substring(9);
                    }
                    currentFilter = new StringBuilder();
                }
                currentFilter.append(part);
            } else {
                // This is a continuation of the current filter value
                if (currentFilter.length() > 0) {
                    currentFilter.append(" ").append(part);
                }
            }
        }
        
        // Process the last filter
        if (currentFilter.length() > 0) {
            String filterValue = currentFilter.toString().trim();
            if (filterValue.startsWith("color=")) {
                colorFilter = filterValue.substring(6);
            } else if (filterValue.startsWith("contains=")) {
                containsFilter = filterValue.substring(9);
            } else if (filterValue.startsWith("refersTo=")) {
                refersToFilter = filterValue.substring(9);
            }
        }
        
        // Debug output
        System.out.println("Filters parsed: color=" + colorFilter + ", contains=" + containsFilter + ", refersTo=" + refersToFilter);
        
        // Get filtered notes
        List<Note> filteredNotes = board.getNotes(colorFilter, containsFilter, refersToFilter);
        
        // Build response
        StringBuilder response = new StringBuilder();
        response.append("OK ").append(filteredNotes.size());
        
        for (Note note : filteredNotes) {
            response.append("\n");
            response.append("NOTE ")
                   .append(note.getX()).append(" ")
                   .append(note.getY()).append(" ")
                   .append(note.getColor()).append(" ")
                   .append(note.getMessage()).append(" ")
                   .append("PINNED=").append(board.isNotePinned(note));
        }
        
        return response.toString();
    }
    
    private String handleGetAll() {
        List<Note> allNotes = board.getNotes(null, null, null);
        
        StringBuilder response = new StringBuilder();
        response.append("OK ").append(allNotes.size());
        
        for (Note note : allNotes) {
            response.append("\n");
            response.append("NOTE ")
                   .append(note.getX()).append(" ")
                   .append(note.getY()).append(" ")
                   .append(note.getColor()).append(" ")
                   .append(note.getMessage()).append(" ")
                   .append("PINNED=").append(board.isNotePinned(note));
        }
        
        return response.toString();
    }
    
    private String handleGetPins() {
        List<Pin> pins = board.getPins();
        
        StringBuilder response = new StringBuilder();
        response.append("OK ").append(pins.size());
        
        for (Pin pin : pins) {
            response.append("\n");
            response.append("PIN ")
                   .append(pin.getX()).append(" ")
                   .append(pin.getY());
        }
        
        return response.toString();
    }
    
    private String handlePost(String[] parts) {
        if (parts.length < 5) {
            return "ERROR INVALID_FORMAT";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String color = parts[3];
            
            StringBuilder message = new StringBuilder();
            for (int i = 4; i < parts.length; i++) {
                message.append(parts[i]);
                if (i < parts.length - 1) {
                    message.append(" ");
                }
            }
            
            return board.addNote(x, y, color, message.toString());
            
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT";
        }
    }
    
    private String handlePin(String[] parts) {
        if (parts.length != 3) {
            return "ERROR INVALID_FORMAT";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return board.addPin(x, y);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT";
        }
    }
    
    private String handleUnpin(String[] parts) {
        if (parts.length != 3) {
            return "ERROR INVALID_FORMAT";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return board.removePin(x, y);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT";
        }
    }
    
    private String handleShake() {
        return board.shake();
    }
    
    private String handleClear() {
        return board.clear();
    }
}