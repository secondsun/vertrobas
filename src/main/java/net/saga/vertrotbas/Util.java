package net.saga.vertrotbas;

import java.awt.Color;
import net.saga.vertrotbas.structs.Point2f;
import static org.lwjgl.opengl.GL11.*;

public class Util {

    public static final double[][] COLOR = new double[][]{
        new double[]{Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue()},
        new double[]{Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue()},
        new double[]{Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue()},
        new double[]{Color.CYAN.getRed(), Color.CYAN.getGreen(), Color.CYAN.getBlue()},
        new double[]{Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue()},
        new double[]{128, 0, 128},//PUrple
        new double[]{255, 69, 0},
        new double[]{211, 211, 211},
        new double[]{169, 169, 169},
        new double[]{173, 216, 230},
        new double[]{144, 238, 144},
        new double[]{224, 255, 255},
        new double[]{255, 99, 71},
        new double[]{216, 191, 216},
        new double[]{255, 255, 0},
        new double[]{255, 255, 255}
    };

    public static void LINE(double x1, double y1, double x2, double y2, int color) {
        // store the current1 model matrix
        glPushMatrix();

        glLineWidth(1);
        glColor3d(COLOR[color][0], COLOR[color][1], COLOR[color][2]);
        glBegin(GL_LINES);
        glVertex3d(x1, y1, 0.0);
        glVertex3d(x2, y2, 0.0);
        glEnd();

        // restore the model view matrix to prevent contamination
        glPopMatrix();
    }

    public static void PSET(double x1, double y1, int color) {
        // store the current1 model matrix
        glPushMatrix();

        glLineWidth(1);
        glColor3d(COLOR[color][0], COLOR[color][1], COLOR[color][2]);
        glBegin(GL_LINES);
        glVertex3d(x1, y1, 0.0);
        glVertex3d(x1, y1, 0.0);
        glEnd();

        // restore the model view matrix to prevent contamination
        glPopMatrix();
    }

    public static double FNcross(double x1, double y1, double x2, double y2) {
        return x1 * y2 - y1 * x2;
    }

    public static double[] Intersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double x = FNcross(x1, y1, x2, y2);
        double y = FNcross(x3, y3, x4, y4);
        double det = FNcross(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
        x = FNcross(x, x1 - x2, y, x3 - x4) / det;
        y = FNcross(x, y1 - y2, y, y3 - y4) / det;
        return new double[]{x, y};
    }

    public static double COS(double angle) {
        return Math.cos(angle);
    }

    public static double SIN(double angle) {
        return Math.sin(angle);
    }

    public static int min(int a, int b) {
        return (((a) < (b)) ? (a) : (b));
    }

    public static float min(float a, float b) {
        return (((a) < (b)) ? (a) : (b));
    }

    public static int max(int a, int b) {
        return (((a) > (b)) ? (a) : (b));
    }

    public static float max(float a, float b) {
        return (((a) > (b)) ? (a) : (b));
    }

    public static float clamp(float a, float mi, float ma) {
        return min(max(a, mi), ma);
    }         // clamp: Clamp value into set range.

    public static float clamp(int a, int mi, int ma) {
        return min(max(a, mi), ma);
    }

    public static float vxs(float x0, float y0, float x1, float y1) {
        return ((x0) * (y1) - (x1) * (y0));
    }   // vxs: Vector cross product

    public static float vxs(int x0, int y0, int x1, int y1) {
        return ((x0) * (y1) - (x1) * (y0));
    }   // vxs: Vector cross product

// Overlap:  Determine whether the two number ranges overlap.
    public static boolean Overlap(int a0, int a1, int b0, int b1) {
        return (min(a0, a1) <= max(b0, b1) && min(b0, b1) <= max(a0, a1));
    }

    // Overlap:  Determine whether the two number ranges overlap.
    public static boolean Overlap(float a0, float a1, float b0, float b1) {
        return (min(a0, a1) <= max(b0, b1) && min(b0, b1) <= max(a0, a1));
    }

// IntersectBox: Determine whether two 2D-boxes intersect.
    public static boolean IntersectBox(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
        return (Overlap(x0, x1, x2, x3) && Overlap(y0, y1, y2, y3));
    }

    public static boolean IntersectBox(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        return (Overlap(x0, x1, x2, x3) && Overlap(y0, y1, y2, y3));
    }

// PointSide: Determine which side of a line the point is on. Return value: <0, =0 or >0.
    public static float PointSide(float px, float py, float x0, float y0, float x1, float y1) {
        return vxs((x1) - (x0), (y1) - (y0), (px) - (x0), (py) - (y0));
    }

    public static float PointSide(int px, int py, int x0, int y0, int x1, int y1) {
        return vxs((x1) - (x0), (y1) - (y0), (px) - (x0), (py) - (y0));
    }
// Intersect: Calculate the point of intersection between two lines.

    public static Point2f Intersect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {

        float x = vxs(vxs(x1, y1, x2, y2), (x1) - (x2), vxs(x3, y3, x4, y4), (x3) - (x4)) / vxs((x1) - (x2), (y1) - (y2), (x3) - (x4), (y3) - (y4));
        float y = vxs(vxs(x1, y1, x2, y2), (y1) - (y2), vxs(x3, y3, x4, y4), (y3) - (y4)) / vxs((x1) - (x2), (y1) - (y2), (x3) - (x4), (y3) - (y4));

        return new Point2f(x, y);
    }

}
