package net.perkowitz.visualization.gameframe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Random {

    public static int frameCount = 128;
    public static int pixelsPerFrame = 16;

    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: Random <image name> <red scale> <green scale> <blue scale>");
            return;
        }

        String imageName = args[0];
        Float redScale = new Float(args[1]);
        Float greenScale = new Float(args[2]);
        Float blueScale = new Float(args[3]);

        File dir = new File(imageName);
        if (!dir.exists()) {
            try{
                dir.mkdir();
            } catch(SecurityException e){
                System.err.println(e);
            }
        }


        BufferedImage lastFrame = GameFrame.newImage();
        for (int frameCount=0; frameCount< Random.frameCount; frameCount++) {
            BufferedImage frame = GameFrame.fade(lastFrame,0.9f,0.8f,0.8f);
            for (int pixel=0; pixel<pixelsPerFrame; pixel++) {
                int x = (int)Math.floor(Math.random() * GameFrame.XSIZE);
                int y = (int)Math.floor(Math.random() * GameFrame.YSIZE);
                Color color = new Color(
                        redScale * (float)Math.random(),
                        greenScale * (float)Math.random(),
                        blueScale * (float)Math.random());
                frame.setRGB(x, y, color.getRGB());
            }

            String filename = imageName + "/" + frameCount + ".bmp";
            GameFrame.writeImage(frame,filename);

            lastFrame = frame;
        }


    }
}
