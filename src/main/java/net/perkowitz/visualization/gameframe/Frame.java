package net.perkowitz.visualization.gameframe;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Frame {

    private BufferedImage bufferedImage;

    public Frame() {
        bufferedImage = GameFrame.newImage();
    }

    public Frame(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public void set(int x, int y, Color color) {
        bufferedImage.setRGB(x,y,color.getRGB());
    }

    public Color get(int x, int y) {
        return new Color(bufferedImage.getRGB(x,y));
    }

    public void output(String filename) {
        try {
            ImageIO.write(bufferedImage, "BMP", new File(filename));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static Frame copyFrameAndFade(Frame frame, float redFade, float greenFade, float blueFade) {

        BufferedImage startImage = frame.getBufferedImage();
        BufferedImage fadedImage = GameFrame.newImage();
        for (int x=0; x<GameFrame.XSIZE; x++) {
            for (int y=0; y<GameFrame.YSIZE; y++) {
                Color color = new Color(startImage.getRGB(x,y));
                int r = (int)((float)color.getRed() * redFade);
                int g = (int)((float)color.getGreen() * greenFade);
                int b = (int)((float)color.getBlue() * blueFade);
                color = new Color(r,g,b);
                fadedImage.setRGB(x,y,color.getRGB());
            }

        }

        return new Frame(fadedImage);
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
