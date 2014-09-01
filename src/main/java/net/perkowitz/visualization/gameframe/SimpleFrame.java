package net.perkowitz.visualization.gameframe;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SimpleFrame {

    public static int xsize = 16;
    public static int ysize = 16;

    public static void main(String[] args) {

        BufferedImage bufferedImage = new BufferedImage(xsize,ysize,BufferedImage.TYPE_INT_RGB);

        for (int x=0; x<xsize; x++) {
            for (int y=0; y<ysize; y++) {
                Color color = new Color(0, x/(float)xsize, y/(float)ysize);
                bufferedImage.setRGB(x,y,color.getRGB());

            }
        }

        try {
            ImageIO.write(bufferedImage,"BMP",new File("test.bmp"));
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
