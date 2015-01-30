package net.saga.vertrotbas.structs;

public class Player {

    Point3f where, // Current position
            velocity;   // Current motion vector
    float angle, anglesin, anglecos, yaw;   // Looking towards (and sin() and cos() thereof)
    int sector;
}
