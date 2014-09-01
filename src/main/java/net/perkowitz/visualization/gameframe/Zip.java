package net.perkowitz.visualization.gameframe;

import java.awt.*;

public class Zip implements MoverInterface {

    public static int DIRECTION_NORTH = 0;
    public static int DIRECTION_EAST = 1;
    public static int DIRECTION_SOUTH = 2;
    public static int DIRECTION_WEST = 3;

    private int direction = DIRECTION_EAST;
    private int x;
    private int y;
    private Color color;

    public Zip(int x, int y, Color color, int direction) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.direction = direction;
    }

    public void reset(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void step() {
        if (direction == DIRECTION_NORTH) {
            y = (y-1) % GameFrame.XSIZE;
        } else if (direction == DIRECTION_EAST) {
            x = (x+1) % GameFrame.XSIZE;
        } else if (direction == DIRECTION_SOUTH) {
            y = (y+1) % GameFrame.XSIZE;
        } else if (direction == DIRECTION_WEST) {
            x = (x-1) % GameFrame.XSIZE;
        }
    }

    public void render(Frame frame) {
        frame.set(x,y,color);
    }

}
