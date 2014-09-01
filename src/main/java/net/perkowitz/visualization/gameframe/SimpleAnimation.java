package net.perkowitz.visualization.gameframe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class SimpleAnimation {

    public static int frameCount = 32;

    public static void main(String[] args) {

        BufferedImage startImage = GameFrame.newImage();
        BufferedImage endImage = GameFrame.newImage();

        for (int x=0; x< GameFrame.XSIZE; x++) {
            for (int y=0; y< GameFrame.XSIZE; y++) {
                Color color = new Color(0f,0f,1f);
                endImage.setRGB(x,y,color.getRGB());
            }
        }

        List<BufferedImage> frames = GameFrame.keyFrame(startImage, endImage, frameCount);

        for (int frame=0; frame<frames.size(); frame++) {
            String filename = frame + ".bmp";
            GameFrame.writeImage(frames.get(frame), filename);

            int secondFrame = (frameCount-1)*2 - frame;
            filename = secondFrame + ".bmp";
            GameFrame.writeImage(frames.get(frame), filename);
        }


    }
}
