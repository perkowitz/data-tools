package net.perkowitz.visualization.gameframe;

import com.google.common.collect.Lists;

import java.awt.*;
import java.io.File;
import java.util.List;

public class Zips {

    private static int maxFrames = 128;

    private static float redFade = 0.9f;
    private static float greenFade = 0.8f;
    private static float blueFade = 0.85f;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: Zips <image name>");
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

        List<MoverInterface> zips = Lists.newArrayList();
//        for (int zip=0; zip<GameFrame.XSIZE; zip++) {
//            zips.add(new Zip(zip, zip, Color.white));
//        }
        zips.add(new Zip(0,3,Color.red,Zip.DIRECTION_EAST));
        zips.add(new Zip(5,9,Color.green,Zip.DIRECTION_EAST));
        zips.add(new Zip(2,13,Color.blue,Zip.DIRECTION_EAST));
        zips.add(new Zip(6,0,Color.gray,Zip.DIRECTION_SOUTH));
        zips.add(new Zip(14,3,Color.white,Zip.DIRECTION_SOUTH));

        Frame lastFrame = new Frame();
        for (int frameIndex=0; frameIndex<maxFrames; frameIndex++) {
            Frame thisFrame = Frame.copyFrameAndFade(lastFrame, redFade, greenFade, blueFade);
            for (MoverInterface zip : zips) {
                zip.step();
                zip.render(thisFrame);
            }
            thisFrame.output(imageName + "/" + frameIndex + ".bmp");

            lastFrame = thisFrame;
        }

    }
}
