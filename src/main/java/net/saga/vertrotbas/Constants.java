package net.saga.vertrotbas;

public final class Constants {

    public static final int W = 608;
    public static final int H = 480;
    public static final int EyeHeight = 6;    // Camera height from floor when standing
    public static final float DuckHeight = 2.5f;  // And when crouching
    public static final int HeadMargin = 1;    // How much room there is above camera before the head hits the ceiling
    public static final int KneeHeight = 2;    // How tall obstacles the player can simply walk over without jumping
    public static final float hfov = (0.73f * H);  // Affects the horizontal field of vision
    public static final float vfov = (.2f * H);    // Affects the vertical field of vision

    private Constants() {
    }

}
