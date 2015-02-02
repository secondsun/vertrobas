package net.saga.vertrotbas.structs;

public class Player {

    public Point3f where, // Current position
            velocity;   // Current motion vector
    public float angle, anglesin, anglecos, yaw;   // Looking towards (and sin() and cos() thereof)
    public int sector;
}
