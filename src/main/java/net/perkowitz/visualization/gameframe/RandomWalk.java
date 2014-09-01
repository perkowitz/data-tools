package net.perkowitz.visualization.gameframe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class RandomWalk {

    public static int frameCount = 128;
    public static int pixelsPerFrame = 16;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: RandomWalk <image name>");
            return;
        }

        String imageName = args[0];
        File dir = new File(imageName);
        if (!dir.exists()) {
            try{
                dir.mkdir();
            } catch(SecurityException e){
                System.err.println(e);
            }
        }

        float rFade = 0.8f;
        float gFade = 0.8f;
        float bFade = 0.8f;

        int x = GameFrame.XSIZE/2;
        int y = GameFrame.YSIZE/2;
        int r = 200;
        int g = 200;
        int b = 200;

        BufferedImage lastImage = GameFrame.newImage();
        for (int frame=0; frame<frameCount; frame++) {
            BufferedImage image = GameFrame.fade(lastImage,rFade,gFade,bFade);

            int deltaX = (int)Math.floor(Math.random() * 3) - 1;
            x = (x + deltaX + GameFrame.XSIZE) % GameFrame.XSIZE;

            int deltaY = (int)Math.floor(Math.random() * 3) - 1;
            y = (y + deltaY + GameFrame.YSIZE) % GameFrame.YSIZE;

            int deltaR = (int)Math.floor(Math.random() * 20) - 10;
            r = Math.max(Math.min(r + deltaR,255),0);
            int deltaG = (int)Math.floor(Math.random() * 20) - 10;
            g = Math.max(Math.min(g + deltaG,255),0);
            int deltaB = (int)Math.floor(Math.random() * 20) - 10;
            b = Math.max(Math.min(b + deltaB,255),0);
            Color color = new Color(r,g,b);

//            System.out.printf("%d, %d, %s\n",x,y,color);
            image.setRGB(x,y,color.getRGB());

            String filename = imageName + "/" + frame + ".bmp";
            GameFrame.writeImage(image,filename);

            lastImage = image;
        }


    }
}
