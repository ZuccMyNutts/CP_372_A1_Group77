import java.util.*;

public class Board {
    private int width;
    private int height;
    private int noteWidth;
    private int noteHeight;
    private List<Note> notes;
    private List<Pin> pins;

    public Board(int width, int height, int noteWidth, int noteHeight) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.notes = new ArrayList<>();
        this.pins = new ArrayList<>();
    
}
// Add synchronized methods (we'll implement later)
    public synchronized boolean addNote(Note note) {
        // TODO: Check bounds and overlaps
        notes.add(note);
        return true;
    }
    
    public synchronized List<Note> getNotes() {
        return new ArrayList<>(notes); // Return copy for safety
    }
}