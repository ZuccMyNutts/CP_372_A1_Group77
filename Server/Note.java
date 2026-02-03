public class Note {
    private int x, y;
    private String color;
    private String message;
    private boolean pinned;
    
    public Note(int x, int y, String color, String message) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.message = message;
        this.pinned = false;
    }
    
    // Getters
    public int getX() { 
        return x; 
    }
    public int getY() { 
        return y; 
    }
    public String getColor() { 
        return color; 
    }
    public String getMessage() {
         return message; 
        }
    public boolean isPinned() {
         return pinned; 
        }
    
    // Setter for pinned
    public void setPinned(boolean pinned) {
         this.pinned = pinned; 
        }
}