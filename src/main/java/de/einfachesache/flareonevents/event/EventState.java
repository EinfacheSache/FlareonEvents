package de.einfachesache.flareonevents.event;

public enum EventState {

    NOT_RUNNING(0, "NICHT GESTARTET"),
    PREPARING(1, "VORBEREITUNG"),
    STARTING(2, "STARTET"),
    RUNNING(3, "LÄUFT"),
    ENDED(4, "BEENDET");

    private final int id;
    private final String name;

    EventState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
