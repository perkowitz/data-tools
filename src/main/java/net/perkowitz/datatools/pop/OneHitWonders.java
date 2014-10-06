package net.perkowitz.datatools.pop;


import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class OneHitWonders {

    private static Integer maxRankToCompute = 10;
    private static Integer QUERY_LIMIT_PER_MINUTE = 110;
    private static Integer QUERY_SLEEP_SECONDS = 60;

    private EchoNestAPI echoNest;
    private int queryCount = 0;


    public static void main(String[] args) throws EchoNestException {

        if (args.length < 1) {
            System.out.println("Usage: OneHitWonders <songfile>");
            return;
        }

        OneHitWonders app = new OneHitWonders();
        app.run(args[0]);

    }

    public void run(String songfile) {

        Set<String> songsSeen = Sets.newHashSet();
        Map<String,Integer> artistCounts = Maps.newHashMap();
        Map<String,Integer> songCounts = Maps.newHashMap();

        BufferedReader input;
        try {

            String line;

            input = new BufferedReader(new FileReader(new File(songfile)));
            while ((line = input.readLine()) != null) {

                ChartAppearance chartAppearance = new ChartAppearance(line);
                String artistName = chartAppearance.getArtist();
                String songKey = artistName + "," + chartAppearance.getTitle();

                if (!songsSeen.contains(songKey)) {
                    if (artistCounts.get(artistName) == null) {
                        artistCounts.put(artistName,1);
                    } else {
                        artistCounts.put(artistName,artistCounts.get(artistName)+1);
                    }
                    songsSeen.add(songKey);
                }
            }
            input.close();

            input = new BufferedReader(new FileReader(new File(songfile)));
            while ((line = input.readLine()) != null) {

                ChartAppearance chartAppearance = new ChartAppearance(line);
                String artistName = chartAppearance.getArtist();
                String songKey = artistName + "," + chartAppearance.getTitle();

                if (artistCounts.get(artistName) == 1) {
                    if (songCounts.get(songKey) == null) {
                        songCounts.put(songKey,1);
                    } else {
                        songCounts.put(songKey,songCounts.get(songKey)+1);
                    }
                }
            }
            input.close();


        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        for (String songKey : songCounts.keySet()) {
            System.out.printf("%d,%s\n",songCounts.get(songKey),songKey);
        }

    }

}