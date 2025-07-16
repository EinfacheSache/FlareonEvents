package de.einfachesache.flareonEvents;

public enum EventState {

    NOT_RUNNING(0),
    PREPARING(1),
    STARTING(2),
    RUNNING(3);

    private final int id;

    EventState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
