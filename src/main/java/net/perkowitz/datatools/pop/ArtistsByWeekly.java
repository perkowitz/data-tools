package net.perkowitz.datatools.pop;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArtistsByWeekly {

    private static Integer maxRankToCompute = 10;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: ArtistsByWeekly <songfile>");
            return;
        }

        String songfile = args[0];

        Map<String,PopArtist> artists = Maps.newHashMap();

        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(songfile)));

            String line;
            while ((line = input.readLine()) != null) {

                ChartAppearance chartAppearance = new ChartAppearance(line);
                String name = chartAppearance.getArtist();
                List<String> names = ArtistHelper.parseNames(chartAppearance.getArtist(),null);
                for (String artistName : names) {
                    PopArtist artist = artists.get(artistName);
                    if (artist == null) {
                        artist = new PopArtist(artistName);
                        artists.put(artistName,artist);
                    }
                    artist.addChartAppearance(chartAppearance);
                }

            }

            input.close();


        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        // compute ranks by various comparison methods
        List<PopArtist> artistList = Lists.newArrayList(artists.values());
        Set<PopArtist> topArtists = Sets.newHashSet();
        for (int compareMethod=0; compareMethod<= PopArtist.COMPARE_MAX; compareMethod++) {
            PopArtist.setCompareMethod(compareMethod);
            Collections.sort(artistList,Collections.reverseOrder());
            for (int rank=0; rank<maxRankToCompute; rank++) {
                artistList.get(rank).addRank(compareMethod,rank);
                topArtists.add(artistList.get(rank));
            }
        }

        List<PopArtist> rankingArtists = Lists.newArrayList(topArtists);
            String outfile = "top-artists.csv";
            FileWriter fileWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                fileWriter = new FileWriter(outfile);
                bufferedWriter = new BufferedWriter(fileWriter);

                for (PopArtist artist : rankingArtists) {
                    bufferedWriter.write(artist.toString());
                    bufferedWriter.newLine();
                }

                bufferedWriter.close();

            } catch(IOException e) {
                System.err.printf("%s\n", e);
            }

        List<String> names = Lists.newArrayList(artists.keySet());
        Collections.sort(names);
        outfile = "artists.csv";
        try {
            fileWriter = new FileWriter(outfile);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Name,Top Ten Songs,Number One Songs,Top Ten Appearances,Number One Appearances,"
                    + "Top Ten Weeks,Number One Weeks,First Week,Last Week,Career Span,Hit Density");
            bufferedWriter.newLine();

            for (String name : names) {
                bufferedWriter.write(artists.get(name).toString());
                bufferedWriter.newLine();
            }

            bufferedWriter.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }


    }

}