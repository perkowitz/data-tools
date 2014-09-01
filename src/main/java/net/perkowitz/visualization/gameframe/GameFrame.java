package net.perkowitz.visualization.gameframe;

import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameFrame {

    public static int XSIZE = 16;
    public static int YSIZE = 16;

    public static BufferedImage newImage() {
        return new BufferedImage(XSIZE, YSIZE,BufferedImage.TYPE_INT_RGB);
    }

    public static void writeImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(image, "BMP", new File(filename));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static List<BufferedImage> keyFrame(BufferedImage startImage, BufferedImage endImage, int frameCount) {

        List<BufferedImage> frames = Lists.newArrayList();

        for (int frame=0; frame<frameCount; frame++) {

            BufferedImage frameImage = GameFrame.newImage();
            for (int x=0; x< XSIZE; x++) {
                for (int y=0; y< YSIZE; y++) {
                    int startRgb = startImage.getRGB(x,y);
                    int endRgb = endImage.getRGB(x,y);
                    float scale = (float)frame/frameCount;
                    int frameRgb = Math.round((1-scale)*startRgb + scale*endRgb);
                    frameImage.setRGB(x,y,frameRgb);
                }
            }
            frames.add(frameImage);

        }

        return frames;
    }

    public static BufferedImage fade(BufferedImage image, float redFade, float greenFade, float blueFade) {

        BufferedImage fadedImage = GameFrame.newImage();
        for (int x=0; x<image.getWidth(); x++) {
            for (int y=0; y<image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x,y));
                int r = (int)(color.getRed() * redFade);
                int g = (int)(color.getGreen() * redFade);
                int b = (int)(color.getBlue() * redFade);
                color = new Color(r,g,b);
                fadedImage.setRGB(x,y,color.getRGB());
            }

        }

        return fadedImage;
    }

}
