package de.einfachesache.flareonevents.event;

import net.kyori.adventure.sound.Sound;

public enum EventSound {

    NOTIFY    (org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING,      Sound.Source.MASTER, 1f, 1f),
    START_END (org.bukkit.Sound.ENTITY_ENDER_DRAGON_AMBIENT, Sound.Source.MASTER, 1f, 1f);

    public final org.bukkit.Sound bukkit;
    public final Sound.Source src;
    public final float vol, pitch;

    EventSound(org.bukkit.Sound s, Sound.Source src, float v, float p) {
        this.bukkit = s;
        this.src = src;
        this.vol = v;
        this.pitch = p;
    }

    public Sound adventure() {
        return Sound.sound(bukkit, src, vol, pitch);
    }
}
