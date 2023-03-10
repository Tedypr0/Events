package org.example;

import java.util.Objects;

public class Event {
    private final int id;
    private final String message;

    public Event(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id == event.id && Objects.equals(message, event.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message);
    }
}
