package net.saga.vertrotbas;

import static net.saga.vertrotbas.Util.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;

public class Main {

// The end coordinates for the line segment representing a "wall"
    static double vx1 = 70;
    static double vy1 = 20;
    static double vx2 = 70;
    static double vy2 = 70;

//' The coordinates of the player
    static double px = 50;
    static double py = 50;
    static double angle = 0;

    public static void main(String[] args) {
        int targetWidth = 640;
        int targetHeight = 480;

        try {
            DisplayMode chosenMode = new DisplayMode(targetWidth, targetHeight);

            Display.setDisplayMode(chosenMode);
            Display.setTitle("Example Maven Natives");
            Display.setFullscreen(false);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Unable to create display.");
            System.exit(0);
        }

        // disable the OpenGL depth test since we're rendering 2D graphics
        glDisable(GL_DEPTH_TEST);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0, targetWidth, targetHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glViewport(0, 0, targetWidth, targetHeight);

        GL11.glClearColor(0, 0, 0, 0);

        boolean gameRunning = true;
        double pos = 0;

        int FRAMERATE = 60;

        while (gameRunning) {
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            render();

            handleInput();

            Display.update();
            Display.sync(FRAMERATE);

            if (Display.isCloseRequested()) {
                gameRunning = false;
                Display.destroy();
                System.exit(0);
            }
        }
    }

    private static void render() {

        //' Transform the vertexes relative to the player
        double tx1 = vx1 - px;
        double ty1 = vy1 - py;
        double tx2 = vx2 - px;
        double ty2 = vy2 - py;
        //' Rotate them around the player's view
        double tz1 = tx1 * COS(angle) + ty1 * SIN(angle);
        double tz2 = tx2 * COS(angle) + ty2 * SIN(angle);
        tx1 = tx1 * SIN(angle) - ty1 * COS(angle);
        tx2 = tx2 * SIN(angle) - ty2 * COS(angle);
        LINE(50 - tx1, 50 - tz1, 50 - tx2, 50 - tz2, 14);
        LINE(50, 50, 50, 45, 8);
        PSET(50, 50, 15);

        if (tz1 > 0 || tz2 > 0) { //' If the line crosses the player's viewplane, clip it.
            double[] i1 = Intersect(tx1, tz1, tx2, tz2, -0.0001, 0.0001, -20, 5);
            double[] i2 = Intersect(tx1, tz1, tx2, tz2, 0.0001, 0.0001, 20, 5);
            double iz1 = i1[1];
            double ix1 = i1[0];
            double iz2 = i2[1];
            double ix2 = i2[0];

            if (tz1 <= 0) {
                if (iz1 > 0) {
                    tx1 = ix1;
                    tz1 = iz1;
                } else {
                    tx1 = ix2;
                    tz1 = iz2;
                }
            }
            if (tz2 <= 0) {
                if (iz1 > 0) {
                    tx2 = ix1;
                    tz2 = iz1;
                } else {
                    tx2 = ix2;
                    tz2 = iz2;
                }
            }
            double x1 = -tx1 * 16 / tz1;
            double y1a = -50 / tz1;
            double y1b = 50 / tz1;
            double x2 = -tx2 * 16 / tz2;
            double y2a = -50 / tz2;
            double y2b = 50 / tz2;

            LINE(50 + x1, 50 + y1a, 50 + x2, 50 + y2a, 14);//'top (1-2 b)
            LINE(50 + x1, 50 + y1b, 50 + x2, 50 + y2b, 14); //'bottom (1-2 b)
            LINE(50 + x1, 50 + y1a, 50 + x1, 50 + y1b, 6); //'left (1)
            LINE(50 + x2, 50 + y2a, 50 + x2, 50 + y2b, 6); //'right (2)
        }
    }

    private static void handleInput() {
        boolean leftPressed = hasInput(Keyboard.KEY_LEFT);
        boolean rightPressed = hasInput(Keyboard.KEY_RIGHT);
        boolean upPressed = hasInput(Keyboard.KEY_UP);
        boolean downPressed = hasInput(Keyboard.KEY_DOWN);

        boolean strafeLeft = Keyboard.isKeyDown(Keyboard.KEY_COMMA);
        boolean strafeRight = Keyboard.isKeyDown(Keyboard.KEY_PERIOD);

        if ((leftPressed) && (!rightPressed)) {
            angle = angle - 0.1;
        } else if ((rightPressed) && (!leftPressed)) {
            angle = angle + 0.1;
        }

        if ((upPressed) && (!downPressed)) {
            px = px + COS(angle);
            py = py + SIN(angle);
        } else if ((downPressed) && (!upPressed)) {
            px = px - COS(angle);
            py = py - SIN(angle);
        }

        if ((strafeLeft) && (!strafeRight)) {
            px = px + SIN(angle);
            py = py - COS(angle);
            
        } else if ((strafeRight) && (!strafeLeft)) {
            px = px - SIN(angle);
            py = py + COS(angle);
        }

        
    }

    private static boolean hasInput(int direction) {
        switch (direction) {
            case Keyboard.KEY_LEFT:
                return Keyboard.isKeyDown(Keyboard.KEY_LEFT);

            case Keyboard.KEY_RIGHT:
                return Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
            case Keyboard.KEY_UP:
                return Keyboard.isKeyDown(Keyboard.KEY_UP);

            case Keyboard.KEY_DOWN:
                return Keyboard.isKeyDown(Keyboard.KEY_DOWN);

            case Keyboard.KEY_SPACE:
                return Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        }
        return false;
    }

}
