package project.model; // ✅ Correct package


import java.time.LocalDate;

public class Habit {
    private int id;
    private String name;
    private boolean completed;
    private LocalDate createdDate;
    

    // ✅ Constructor with ID (for database retrieval)
    public Habit(int id, String name, boolean completed, LocalDate createdDate) {
        this.id = id;
        this.name = name;
        this.completed = completed;
        this.createdDate = createdDate;
    }

    // ✅ Constructor without ID (for new habits)
    public Habit(String name, LocalDate createdDate) {
        this.name = name;
        this.completed = false; // Default to false
        this.createdDate = createdDate;
    }

    // ✅ Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        return name; // Show name in the UI list
    }
}
