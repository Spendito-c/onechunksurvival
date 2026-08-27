package com.pokecraft.onechunk;

import java.util.UUID;

public class PlayerAreaData {
    public UUID uuid;
    public String world;
    public int radius;
    public int stage;
    public int xp;
    public boolean logsObjectiveDone;
    public boolean stoneObjectiveDone;
    public int logsCollected;
    public int stoneCollected;

    // Le centre de la zone est toujours (0,0) puisque chaque joueur a son propre monde.
    public static final int CENTER_X = 0;
    public static final int CENTER_Z = 0;

    public PlayerAreaData(UUID uuid, String world, int radius) {
        this.uuid = uuid;
        this.world = world;
        this.radius = radius;
        this.stage = 1;
        this.xp = 0;
        this.logsObjectiveDone = false;
        this.stoneObjectiveDone = false;
        this.logsCollected = 0;
        this.stoneCollected = 0;
    }
}
