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

public class ArtistsByYear {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: ArtistsByYear <songfile> <mapfile>");
            return;
        }

        String songfile = args[0];
        String mapfile = args[1];

        // create map of artist aliases
        Set<String> artistNames = Sets.newHashSet();
        Map<String,Set<String>> aliasToArtistMap = Maps.newHashMap(); // one alias can apply to multiple
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(mapfile)));

            String line;
            while ((line = input.readLine()) != null) {
                String[] fields = line.split(",");
                String name = fields[0];
                artistNames.add(name);
                for (int i=0; i<fields.length; i++) {   // map each name as an alias to itself
                    String alias = fields[i];
                    if (aliasToArtistMap.get(alias) == null) {
                        Set<String> names = Sets.newHashSet();
                        names.add(name);
                        aliasToArtistMap.put(alias, names);
                    } else {
                        aliasToArtistMap.get(alias).add(name);
                    }
                }
            }

            input.close();

        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        Artist.setCompareMethod(Artist.COMPARE_TOPTEN_APPEARANCES);
        Map<Integer,Map<String,Artist>> artistCountsByYear = Maps.newHashMap();
        Map<String,Artist> artists = Maps.newHashMap();

        try {
            input = new BufferedReader(new FileReader(new File(songfile)));

            String line;
            while ((line = input.readLine()) != null) {

                ChartAppearance chartAppearance = new ChartAppearance(line);

                Map<String,Artist> yearCounts = artistCountsByYear.get(chartAppearance.getYear());
                if (yearCounts == null) {
                    yearCounts = Maps.newHashMap();
                    artistCountsByYear.put(chartAppearance.getYear(),yearCounts);
                }

                List<String> names = ArtistHelper.parseNames(chartAppearance.getArtist(),null);
                for (String artistName : names) {

                    Set<String> mappedNames = aliasToArtistMap.get(artistName);
                    if (mappedNames == null) {
                        mappedNames = Sets.newHashSet();
//                        mappedNames.add(artistName);
                    }

                    for (String mappedName : mappedNames) {

                        Artist artist = artists.get(mappedName);
                        if (artist == null) {
                            artist = new Artist(mappedName);
                            artists.put(mappedName,artist);
                        }
                        artist.addChartAppearance(chartAppearance);

                        Artist yearArtist = yearCounts.get(mappedName);
                        if (yearArtist == null) {
                            yearArtist = new Artist(mappedName);
                            yearCounts.put(mappedName,yearArtist);
                        }
                        yearArtist.addChartAppearance(chartAppearance);
                    }

                }

            }

            input.close();


        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        String outfile = "countsByYear.csv";
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(outfile);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (Integer year=1960; year<2014; year++) {
                Map<String,Artist> yearCounts = artistCountsByYear.get(year);
                if (yearCounts != null) {
                    List<Artist> yearArtists = Lists.newArrayList(yearCounts.values());
                    Collections.sort(yearArtists);
                    yearArtists = Lists.reverse(yearArtists);
                    for (int i=0; i<Math.min(10,yearArtists.size()); i++) {
                        Artist yearArtist = yearArtists.get(i);
                        bufferedWriter.write(year + "," + (i+1) + "," + yearArtist.getName() + "," + yearArtist.getComparisonScore());
                        bufferedWriter.newLine();
                    }
                }


            }

            bufferedWriter.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }

     }



}