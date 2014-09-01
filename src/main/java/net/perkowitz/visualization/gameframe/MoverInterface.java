package net.perkowitz.visualization.gameframe;


import java.awt.*;

public interface MoverInterface {

    public void reset(int x, int y, Color color);
    public void step();
    public void render(Frame frame);

}
