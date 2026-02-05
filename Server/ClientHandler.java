import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static Board board;
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    public static void setBoard(Board b) {
        board = b;
    }
    // PIN Errorrs
    private String handlePin(String[] parts) {
        if (parts.length != 3) {
            return "ERROR INVALID_FORMAT PIN requires exactly two coordinates";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return board.addPin(x, y);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be numeric values";}
    }
    
    private String handleUnpin(String[] parts) {
        if (parts.length != 3) {
            return "ERROR INVALID_FORMAT UNPIN requires exactly two coordinates";}
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return board.removePin(x, y);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be numeric values";}
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
            return "ERROR INVALID_FORMAT POST requires coordinates, color, and message";
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
            return "ERROR INVALID_FORMAT Coordinates must be numeric values";
        }
    }
    
    private String handleGet(String command) {
    String filterString = command.substring(4).trim();
    
    if (filterString.isEmpty() || filterString.equalsIgnoreCase("ALL")) {
        return handleGetAll();
    }
    
    String colorFilter = extractFilterValue(filterString, "color=");
    String containsFilter = extractFilterValue(filterString, "contains=");
    String refersToFilter = extractFilterValue(filterString, "refersTo=");
    System.out.println("Filters parsed: color=" + colorFilter + ", contains=" + containsFilter + ", refersTo=" + refersToFilter);
    List<Note> filteredNotes = board.getNotes(colorFilter, containsFilter, refersToFilter);
    
    //response
    if (filteredNotes.isEmpty()) {
        return "OK 0";
    }
    
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

    private String extractFilterValue(String filterString, String filterKey) {
        int keyIndex = filterString.indexOf(filterKey);
        if (keyIndex == -1) {
            return null;
        }
    
        int valueStart = keyIndex + filterKey.length();
        int nextFilterIndex = -1;
        String[] possibleNextFilters = {"color=", "contains=", "refersTo="};
        
        for (String nextFilter : possibleNextFilters) {
            if (!nextFilter.equals(filterKey)) {
                int index = filterString.indexOf(nextFilter, valueStart);
                if (index != -1 && (nextFilterIndex == -1 || index < nextFilterIndex)) {
                    nextFilterIndex = index;
                }
            }
        }
        
        // Extract the value
        String value;
        if (nextFilterIndex != -1) {
            value = filterString.substring(valueStart, nextFilterIndex).trim();
        } else {
            value = filterString.substring(valueStart).trim();
        }
        
        return value.isEmpty() ? null : value;
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
    
        private String handleShake() {
            return board.shake();
        }
        
        private String handleClear() {
            return board.clear();
        }
        
        private String processCommand(String command) {
            // check for empty/null command
            if (command == null || command.trim().isEmpty()) {
                return "ERROR MALFORMED_REQUEST Empty command received";
            }
            
            String[] parts = command.split(" ");
            
            // This check might be redundant IDK
            if (parts.length == 0) {
                return "ERROR MALFORMED_REQUEST Command cannot be empty";
            }
            
            String cmd = parts[0].toUpperCase();
            
            // Check weird gets commands first
            if (command.toUpperCase().equals("GET PINS")) {
                return handleGetPins();
            }
            
            if (command.toUpperCase().equals("GET ALL")){
                return handleGetAll();
            }
            
            // commands
            if (cmd.equals("POST")) {
                return handlePost(parts);
            } else if (cmd.equals("GET")) {
                return handleGet(command);
            } else if (cmd.equals("PIN")) {
                return handlePin(parts);
            } else if (cmd.equals("UNPIN")) {
                return handleUnpin(parts);
            } else if (cmd.equals("SHAKE")) {
                return handleShake();
            } else if (cmd.equals("CLEAR")) {
                return handleClear();
            } else if (cmd.equals("QUIT")) {
                return "GOODBYE";
            } else {
                return "ERROR MALFORMED_REQUEST Command '" + cmd + "' not recognized";
            }
        }
    
    public void run() {
        try {in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
    }
    }
 }
}