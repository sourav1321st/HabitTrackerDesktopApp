package project.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitTracker {
    public static class Habit {
        String name;
        LocalDate createdAt;

        public Habit(String name) {
            this.name = name;
            this.createdAt = LocalDate.now();
        }

        @Override
        public String toString() {
            return name + " (Added: " + createdAt + ")";
        }
    }

    private final List<Habit> habits;

    public HabitTracker() {
        this.habits = new ArrayList<>();
    }

    public void addHabit(String habit) {
        habits.add(new Habit(habit));
    }

    public void removeHabit(String habitName) {
        habits.removeIf(habit -> habit.name.equals(habitName));
    }

    public List<Habit> getHabits() {
        return new ArrayList<>(habits);
    }
}
