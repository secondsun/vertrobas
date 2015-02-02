/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.saga.vertrotbas;

import java.nio.ByteBuffer;
import net.saga.vertrotbas.structs.Item;
import net.saga.vertrotbas.structs.Player;
import net.saga.vertrotbas.structs.Point2f;
import net.saga.vertrotbas.structs.Sector;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
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
import static net.saga.vertrotbas.Util.*;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import org.lwjgl.opengl.GL12;

/**
 *
 * @author summers
 */
public class Portrend {

    /* Define window size */
    public static int W = 608;
    public static int H = 480;
    public static ByteBuffer buffer = BufferUtils.createByteBuffer(W * H * 3); //4 for RGBA, 3 for RGB
    /* Define various vision related constants */
    public static int EyeHeight = 6;    // Camera height from floor when standing
    public static float DuckHeight = 2.5f;  // And when crouching
    public static int HeadMargin = 1;    // How much room there is above camera before the head hits the ceiling
    public static int KneeHeight = 2;    // How tall obstacles the player can simply walk over without jumping
    public static float hfov = (0.73f * H);  // Affects the horizontal field of vision
    public static float vfov = (.2f * H);    // Affects the vertical field of vision
    static int NumSectors = 0;

    static Sector[] sectors = new Sector[NumSectors];

    static Player player;

    public static void main(String args[]) {
        loadData(args[0]);

        int targetWidth = W;
        int targetHeight = H;

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

        while (gameRunning) {
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            buffer.flip(); 
            render();
            buffer.flip(); 
            
            int textureID = glGenTextures(); //Generate texture ID
            glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

            //Setup wrap mode
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            //Setup texture scaling filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            //Send texel data to OpenGL
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, W, H, 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);


            handleInput();

            Display.update();
            GL11.glDeleteTextures(textureID);
            Display.sync(60);

            if (Display.isCloseRequested()) {
                gameRunning = false;
                Display.destroy();
                System.exit(0);
            }
        }
    }

    private static void loadData(String arg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void render() {
        int MaxQueue = 32;
        Item[] queue = new Item[32];
        int head = 0;
        int tail = 0;

        int[] ytop = new int[W];
        int[] ybottom = new int[W];

        int[] renderedsectors = new int[NumSectors];
        for (int x = 0; x < W; ++x) {
            ybottom[x] = H - 1;
        }
        for (int n = 0; n < NumSectors; ++n) {
            renderedsectors[n] = 0;
        }

        /* Begin whole-screen rendering from where the player is. */
        queue[head] = new Item(player.sector, 0, W - 1);
        if (++head == MaxQueue) {
            head = 0;
        }

        do {
            /* Pick a sector & slice from the queue to draw */
            int now = tail;
            if (++tail == MaxQueue) {
                tail = 0;
            }

            if ((renderedsectors[queue[now].sectorno] & 0x21) != 0) {
                continue; // Odd = still rendering, 0x20 = give up
            }
            renderedsectors[queue[now].sectorno] = ++renderedsectors[queue[now].sectorno] + 1;
            Sector sect = sectors[queue[now].sectorno];
            /* Render each wall of this sector that is facing towards player. */
            for (int s = 0; s < sect.npoints; ++s) {
                /* Acquire the x,y coordinates of the two endpoints (vertices) of this edge of the sector */
                float vx1 = sect.vertex[s + 0].x - player.where.x;
                float vy1 = sect.vertex[s + 0].y - player.where.y;
                float vx2 = sect.vertex[s + 1].x - player.where.x;
                float vy2 = sect.vertex[s + 1].y - player.where.y;
                /* Rotate them around the player's view */
                float pcos = player.anglecos, psin = player.anglesin;
                float tx1 = vx1 * psin - vy1 * pcos, tz1 = vx1 * pcos + vy1 * psin;
                float tx2 = vx2 * psin - vy2 * pcos, tz2 = vx2 * pcos + vy2 * psin;
                /* Is the wall at least partially in front of the player? */
                if (tz1 <= 0 && tz2 <= 0) {
                    continue;
                }
                /* If it's partially behind the player, clip it against player's view frustrum */
                if (tz1 <= 0 || tz2 <= 0) {
                    float nearz = 1e-4f, farz = 5, nearside = 1e-5f, farside = 20.f;
                    // Find an intersection between the wall and the approximate edges of player's view
                    Point2f i1 = Util.Intersect(tx1, tz1, tx2, tz2, -nearside, nearz, -farside, farz);
                    Point2f i2 = Util.Intersect(tx1, tz1, tx2, tz2, nearside, nearz, farside, farz);
                    if (tz1 < nearz) {
                        if (i1.y > 0) {
                            tx1 = i1.x;
                            tz1 = i1.y;
                        } else {
                            tx1 = i2.x;
                            tz1 = i2.y;
                        }
                    }
                    if (tz2 < nearz) {
                        if (i1.y > 0) {
                            tx2 = i1.x;
                            tz2 = i1.y;
                        } else {
                            tx2 = i2.x;
                            tz2 = i2.y;
                        }
                    }
                }
                /* Do perspective transformation */
                float xscale1 = hfov / tz1, yscale1 = vfov / tz1;
                int x1 = W / 2 - (int) (tx1 * xscale1);
                float xscale2 = hfov / tz2, yscale2 = vfov / tz2;
                int x2 = W / 2 - (int) (tx2 * xscale2);
                if (x1 >= x2 || x2 < queue[now].sx1 || x1 > queue[now].sx2) {
                    continue; // Only render if it's visible
                }            /* Acquire the floor and ceiling heights, relative to where the player's view is */

                float yceil = sect.ceil - player.where.z;
                float yfloor = sect.floor - player.where.z;
                /* Check the edge type. neighbor=-1 means wall, other=boundary between two sectors. */
                int neighbor = sect.neighbors[s];
                float nyceil = 0, nyfloor = 0;
                if (neighbor >= 0) // Is another sector showing through this portal?
                {
                    nyceil = sectors[neighbor].ceil - player.where.z;
                    nyfloor = sectors[neighbor].floor - player.where.z;
                }
                /* Project our ceiling & floor heights into screen coordinates (Y coordinate) */

                int y1a = H / 2 - (int) (Yaw(yceil, tz1, player) * yscale1), y1b = H / 2 - (int) (Yaw(yfloor, tz1, player) * yscale1);
                int y2a = H / 2 - (int) (Yaw(yceil, tz2, player) * yscale2), y2b = H / 2 - (int) (Yaw(yfloor, tz2, player) * yscale2);
                /* The same for the neighboring sector */
                int ny1a = H / 2 - (int) (Yaw(nyceil, tz1, player) * yscale1), ny1b = H / 2 - (int) (Yaw(nyfloor, tz1, player) * yscale1);
                int ny2a = H / 2 - (int) (Yaw(nyceil, tz2, player) * yscale2), ny2b = H / 2 - (int) (Yaw(nyfloor, tz2, player) * yscale2);

                /* Render the wall. */
                int beginx = max(x1, queue[now].sx1);
                int endx = min(x2, queue[now].sx2);
                for (int x = beginx; x <= endx; ++x) {
                    /* Calculate the Z coordinate for this point. (Only used for lighting.) */
                    int z = (int) (((x - x1) * (tz2 - tz1) / (x2 - x1) + tz1) * 8f);
                    /* Acquire the Y coordinates for our ceiling & floor for this X coordinate. Clamp them. */
                    int ya = (x - x1) * (y2a - y1a) / (x2 - x1) + y1a;
                    int cya = (int) clamp(ya, ytop[x], ybottom[x]); // top
                    int yb = (x - x1) * (y2b - y1b) / (x2 - x1) + y1b;
                    int cyb = (int) clamp(yb, ytop[x], ybottom[x]); // bottom

                    /* Render ceiling: everything above this sector's ceiling height. */
                    vline(x, ytop[x], cya - 1, 0x111111, 0x222222, 0x111111);
                    /* Render floor: everything below this sector's floor height. */
                    vline(x, cyb + 1, ybottom[x], 0x0000FF, 0x0000AA, 0x0000FF);

                    /* Is there another sector behind this edge? */
                    if (neighbor >= 0) {
                        /* Same for _their_ floor and ceiling */
                        int nya = (x - x1) * (ny2a - ny1a) / (x2 - x1) + ny1a;
                        int cnya = (int) clamp(nya, ytop[x], ybottom[x]);
                        int nyb = (x - x1) * (ny2b - ny1b) / (x2 - x1) + ny1b;
                        int cnyb = (int) clamp(nyb, ytop[x], ybottom[x]);
                        /* If our ceiling is higher than their ceiling, render upper wall */
                        int r1 = 0x010101 * (255 - z);
                        int r2 = 0x040007 * (31 - z / 8);
                        vline(x, cya, cnya - 1, 0, x == x1 || x == x2 ? 0 : r1, 0); // Between our and their ceiling
                        ytop[x] = (int) clamp(max(cya, cnya), ytop[x], H - 1); // Shrink the remaining window below these ceilings
                    /* If our floor is lower than their floor, render bottom wall */
                        vline(x, cnyb + 1, cyb, 0, x == x1 || x == x2 ? 0 : r2, 0); // Between their and our floor
                        ybottom[x] = (int) clamp(min(cyb, cnyb), 0, ybottom[x]); // Shrink the remaining window above these floors
                    } else {
                        /* There's no neighbor. Render wall from top (cya = ceiling level) to bottom (cyb = floor level). */
                        int r = 0x010101 * (255 - z);
                        vline(x, cya, cyb, 0, x == x1 || x == x2 ? 0 : r, 0);
                    }
                }
                /* Schedule the neighboring sector for rendering within the window formed by this wall. */
                if (neighbor >= 0 && endx >= beginx && ((head + MaxQueue + 1 - tail) % MaxQueue) != 0) {
                    queue[head] = new Item(neighbor, beginx, endx);

                    if (++head == MaxQueue) {
                        queue[head] = queue[0];
                    }
                }
            } // for s in sector's edges
            ++renderedsectors[queue[now].sectorno];
        } while (head != tail); // render any other queued sectors

    }

    private static void handleInput() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void vline(int x, int y1, int y2, int top, int middle, int bottom) {
        y1 = (int) clamp(y1, 0, H - 1);
        y2 = (int) clamp(y2, 0, H - 1);
        if (y2 == y1) {
            buffer.put((byte) ((middle >> 16) & 0xFF));     // Red component
            buffer.put((byte) ((middle >> 8) & 0xFF));      // Green component
            buffer.put((byte) (middle & 0xFF));               // Blue component;
        } else if (y2 > y1) {
            buffer.put((byte) ((top >> 16) & 0xFF));     // Red component
            buffer.put((byte) ((top >> 8) & 0xFF));      // Green component
            buffer.put((byte) (top & 0xFF));               // Blue component;
            for (int y = y1 + 1; y < y2; ++y) {
                buffer.put((byte) ((middle >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((middle >> 8) & 0xFF));      // Green component
                buffer.put((byte) (middle & 0xFF));               // Blue component;
            }
            buffer.put((byte) ((bottom >> 16) & 0xFF));     // Red component
            buffer.put((byte) ((bottom >> 8) & 0xFF));      // Green component
            buffer.put((byte) (bottom & 0xFF));               // Blue component;
        }
    }

}
