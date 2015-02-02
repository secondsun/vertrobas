
package net.saga.vertrotbas.structs;

public class Sector {
    public float floor, ceil;
    public Point2f[] vertex;
    public int[] neighbors;           // Each edge may have a corresponding neighboring sector
    public int npoints;
}
