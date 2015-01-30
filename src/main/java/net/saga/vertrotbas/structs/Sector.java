
package net.saga.vertrotbas.structs;

import java.util.List;

public class Sector {
    float floor, ceil;
    List<Point2f> vertex;
    List<Sector> neighbors;           // Each edge may have a corresponding neighboring sector
    int npoints;
}
