import java.util.*;
import java.util.stream.Collectors;

public class Board {
    private int width;
    private int height;
    private int noteWidth;
    private int noteHeight;
    private List<Note> notes;
    private List<Pin> pins;
    private List<String> validColors;

    public Board(int width, int height, int noteWidth, int noteHeight, String[] colors) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();
        this.validColors = Arrays.asList(colors);
    }

    public synchronized String addNote(int x, int y, String color, String message) {
        // Check bounds
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return "ERROR OUT_OF_BOUNDS";
        }
        
        // Check if note fits
        if (x + noteWidth > width || y + noteHeight > height) {
            return "ERROR OUT_OF_BOUNDS";
        }
        
        // Check color
        if (!validColors.contains(color.toLowerCase())) {
            return "ERROR COLOR_NOT_SUPPORTED";
        }
        
        // Check for complete overlap (rectangles overlap exactly)
        for (Note note : notes) {
            if (notesOverlapExactly(x, y, note.getX(), note.getY())) {
                return "ERROR COMPLETE_OVERLAP";
            }
        }
        
        // Add note
        Note newNote = new Note(x, y, color, message);
        notes.add(newNote);
        return "OK NOTE_POSTED";
    }
    
    // Helper method to check if two notes overlap exactly
    private boolean notesOverlapExactly(int x1, int y1, int x2, int y2) {
        // Check if top-left corners are the same AND dimensions are the same
        // Since all notes have same dimensions, just check corners
        return x1 == x2 && y1 == y2;
    }
    
    public synchronized List<Note> getNotes(String colorFilter, String containsFilter, String refersToFilter) {
        List<Note> filteredNotes = new ArrayList<>(notes);
        
        // Color filter
        if (colorFilter != null && !colorFilter.isEmpty()) {
            filteredNotes = filteredNotes.stream()
                .filter(note -> note.getColor().equalsIgnoreCase(colorFilter))
                .collect(Collectors.toList());
        }
        
        // Contains filter - check if point is within note boundaries
        if (containsFilter != null && !containsFilter.isEmpty()) {
            try {
                String[] coords = containsFilter.trim().split("\\s+");
                if (coords.length == 2) {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    
                    filteredNotes = filteredNotes.stream()
                        .filter(note -> {
                            // Check if point (x, y) is within this note's rectangle
                            return isPointInNote(x, y, note);
                        })
                        .collect(Collectors.toList());
                } else {
                    // Invalid coordinate format
                    return new ArrayList<>();
                }
            } catch (NumberFormatException e) {
                return new ArrayList<>();
            }
        }
        
        // RefersTo filter
        if (refersToFilter != null && !refersToFilter.isEmpty()) {
            filteredNotes = filteredNotes.stream()
                .filter(note -> note.getMessage().toLowerCase().contains(refersToFilter.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return filteredNotes;
    }
    
    public boolean isPointInNote(int x, int y, Note note) {
        return x >= note.getX() && 
               x < note.getX() + noteWidth && 
               y >= note.getY() && 
               y < note.getY() + noteHeight;
    }
    
    public synchronized String addPin(int x, int y) {
        // Check bounds
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return "ERROR OUT_OF_BOUNDS";
        }
        
        // Check if point is inside any note
        boolean foundNote = false;
        for (Note note : notes) {
            if (isPointInNote(x, y, note)) {
                foundNote = true;
                note.setPinned(true); // Mark note as pinned
            }
        }
        
        if (!foundNote) {
            return "ERROR NO_NOTE_AT_COORDINATE";
        }
        
        // Check if pin already exists at this location
        for (Pin pin : pins) {
            if (pin.getX() == x && pin.getY() == y) {
                // Pin already exists, return success (idempotent)
                return "OK PIN_ADDED";
            }
        }
        
        // Add new pin
        pins.add(new Pin(x, y));
        return "OK PIN_ADDED";
    }
    
    public synchronized String removePin(int x, int y) {
        // Remove pin
        Pin pinToRemove = null;
        for (Pin pin : pins) {
            if (pin.getX() == x && pin.getY() == y) {
                pinToRemove = pin;
                break;
            }
        }
        
        if (pinToRemove == null) {
            return "ERROR PIN_NOT_FOUND";
        }
        
        pins.remove(pinToRemove);
        
        // Update notes pinned status
        for (Note note : notes) {
            if (isPointInNote(x, y, note)) {
                // Check if note still has any pins
                boolean stillPinned = false;
                for (Pin remainingPin : pins) {
                    if (isPointInNote(remainingPin.getX(), remainingPin.getY(), note)) {
                        stillPinned = true;
                        break;
                    }
                }
                note.setPinned(stillPinned);
            }
        }
        
        return "OK PIN_REMOVED";
    }
    
    public synchronized List<Pin> getPins() {
        return new ArrayList<>(pins);
    }
    
    public boolean isNotePinned(Note note) {
        // Check if note has any pin within its boundaries
        for (Pin pin : pins) {
            if (isPointInNote(pin.getX(), pin.getY(), note)) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized String shake() {
        // Remove unpinned notes
        List<Note> notesToKeep = new ArrayList<>();
        for (Note note : notes) {
            if (isNotePinned(note)) {
                notesToKeep.add(note);
            }
        }
        notes = notesToKeep;
        
        // Also remove pins that are no longer in any note
        List<Pin> pinsToKeep = new ArrayList<>();
        for (Pin pin : pins) {
            boolean pinInNote = false;
            for (Note note : notes) {
                if (isPointInNote(pin.getX(), pin.getY(), note)) {
                    pinInNote = true;
                    break;
                }
            }
            if (pinInNote) {
                pinsToKeep.add(pin);
            }
        }
        pins = pinsToKeep;
        
        return "OK SHAKE_COMPLETE";
    }
    
    public synchronized String clear() {
        notes.clear();
        pins.clear();
        return "OK BOARD_CLEARED";
    }
    
    // Helper method for debugging
    public synchronized void printBoardState() {
        System.out.println("Board State:");
        System.out.println("Notes: " + notes.size());
        for (Note note : notes) {
            System.out.println("  Note at (" + note.getX() + "," + note.getY() + 
                             ") color=" + note.getColor() + 
                             " message='" + note.getMessage() + "'" +
                             " pinned=" + isNotePinned(note));
        }
        System.out.println("Pins: " + pins.size());
        for (Pin pin : pins) {
            System.out.println("  Pin at (" + pin.getX() + "," + pin.getY() + ")");
        }
    }
}