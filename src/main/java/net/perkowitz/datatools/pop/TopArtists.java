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

public class TopArtists {

    private static Integer maxRankToCompute = 10;
    private static Double scoreDecay = 0.999;

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: TopArtists <songfile> <mapfile>");
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

        // read in the chart data
        Map<String,PopArtist> artists = Maps.newHashMap();
        List<Map<String,Integer>> scoresOverTime = Lists.newArrayList();
        List<Map<String,Double>> decayedScoresOverTime = Lists.newArrayList();
        List<Map<String,Integer>> scoresPerYear = Lists.newArrayList();
        Map<String,List<Double>> artistDensityOverCareer = Maps.newHashMap();
        Integer lastWeek = 0;
        try {
            input = new BufferedReader(new FileReader(new File(songfile)));

            String line;
            while ((line = input.readLine()) != null) {

                ChartAppearance chartAppearance = new ChartAppearance(line);

                // assumes chart appearances are provided in chronological order
                Integer week = chartAppearance.getWeek();
                if (!week.equals(lastWeek)) {

                    Map<String,Integer> artistScores = Maps.newHashMap();
                    Map<String,Double> decayedArtistScores = Maps.newHashMap();
                    Map<String,Integer> yearlyScores = Maps.newHashMap();

                    for (PopArtist artist : artists.values()) {
                        String name = artist.getName();
                        artistScores.put(name,artist.getSummedScore());

                        Double decayedScore = 0.0 + artist.getSummedScore();
                        if (scoresOverTime.size() > 0) {
                            Integer lastWeekScore = scoresOverTime.get(scoresOverTime.size()-1).get(name);

                            if (lastWeekScore != null) {
                                decayedScore -= lastWeekScore;

                                Double lastWeekDecayedScore = decayedScoresOverTime.get(decayedScoresOverTime.size()-1).get(name);
                                if (lastWeekDecayedScore != null) {
                                    decayedScore += (double)lastWeekDecayedScore * scoreDecay;

                                }
                            }
                        }
                        decayedArtistScores.put(artist.getName(),decayedScore);

                        Integer lastYearScore = 0;
                        if (scoresOverTime.size() >= 52 && scoresOverTime.get(scoresOverTime.size()-52).get(name) != null) {
                            lastYearScore = scoresOverTime.get(scoresOverTime.size()-52).get(name);
                        }
                        yearlyScores.put(name,artist.getSummedScore() - lastYearScore);

                        Double density = (double)artist.getTotalWeeksInTopTen() / (double)(week - artist.getFirstWeek());
                        if (artistDensityOverCareer.get(name) != null) {
                            artistDensityOverCareer.get(name).add(density);
                        } else {
                            if (artist.getTotalTopTenAppearances() > 0) {
                                List<Double> densityList = Lists.newArrayList(density);
                                artistDensityOverCareer.put(name,densityList);
                            }
                        }


                    }
                    scoresOverTime.add(artistScores);
                    decayedScoresOverTime.add(decayedArtistScores);
                    scoresPerYear.add(yearlyScores);




                    lastWeek = week;
                }

                List<String> names = ArtistHelper.parseNames(chartAppearance.getArtist(),null);
                for (String artistName : names) {

                    Set<String> mappedNames = aliasToArtistMap.get(artistName);
                    if (mappedNames != null) {
                        for (String mappedName : mappedNames) {
                            PopArtist artist = artists.get(mappedName);
                            if (artist == null) {
                                artist = new PopArtist(mappedName);
                                artists.put(mappedName,artist);
                            }
                            artist.addChartAppearance(chartAppearance);
                        }
                    }

                }

            }

            input.close();

        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        List<String> names = Lists.newArrayList(artists.keySet());
        Collections.sort(names);
        String outfile = "mapped-artists.csv";
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(outfile);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Name,"
                    + "Number One Songs,Number One Weeks,"
                    + "Top Ten Songs,Top Ten Weeks,Top Ten Appearances,"
                    + "First Week,Last Week,Career Span,Hit Density");
            bufferedWriter.newLine();

            for (String name : names) {
                bufferedWriter.write(artists.get(name).toString());
                bufferedWriter.newLine();
            }

            bufferedWriter.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }

        outfile = "artist-scores-over-time-decayed-weeks.csv";
        String outfile2 = "artist-scores-over-time-decayed-years.csv";
        String outfile3 = "artist-scores-per-year.csv";
        fileWriter = null;
        bufferedWriter = null;
        FileWriter fileWriter2 = null;
        BufferedWriter bufferedWriter2 = null;
        FileWriter fileWriter3 = null;
        BufferedWriter bufferedWriter3 = null;
        try {
            fileWriter = new FileWriter(outfile);
            bufferedWriter = new BufferedWriter(fileWriter);
            fileWriter2 = new FileWriter(outfile2);
            bufferedWriter2 = new BufferedWriter(fileWriter2);
            fileWriter3 = new FileWriter(outfile3);
            bufferedWriter3 = new BufferedWriter(fileWriter3);

            bufferedWriter.write("Week");
            bufferedWriter2.write("Year");
//            bufferedWriter3.write("Year");
            for (String name : names) {
                bufferedWriter.write("," + name);
                bufferedWriter2.write("," + name);
//                bufferedWriter3.write("," + name);
            }
            bufferedWriter.newLine();
            bufferedWriter2.newLine();
//            bufferedWriter3.newLine();

            for (Integer week=0; week<decayedScoresOverTime.size(); week++) {
                bufferedWriter.write(week.toString());
                Integer year = 1963 + week / 52;
                if (week % 52 == 0) {
                    bufferedWriter2.write(year.toString());
                    bufferedWriter3.write(year.toString());
                }

                Map<String,Double> artistScores = decayedScoresOverTime.get(week);
                Map<String,Integer> yearlyScores = scoresPerYear.get(week);
                String maxName = null;
                Integer maxScore = 0;
                for (String name : names) {
                    Double score = 0.0;
                    Integer yearScore = 0;
                    if (artistScores.get(name) != null) {
                        score = artistScores.get(name);
                        yearScore = yearlyScores.get(name);
                    }
                    if (yearScore > maxScore && !name.contains("+")) {
                        maxScore = yearScore;
                        maxName = name;
                    }

                    bufferedWriter.write("," + score.toString());
                    if (week % 52 == 0) {
                        bufferedWriter2.write("," + score.toString());
//                        bufferedWriter3.write("," + yearScore.toString());
                    }

                }
                bufferedWriter.newLine();
                if (week % 52 == 0) {
                    bufferedWriter2.newLine();
                    bufferedWriter3.write("," + maxName + "," + maxScore.toString());
                    bufferedWriter3.newLine();
                }

            }
            bufferedWriter.close();
            bufferedWriter2.close();
            bufferedWriter3.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }

        outfile = "artist-career-density.csv";
        fileWriter = null;
        bufferedWriter = null;
        try {
            fileWriter = new FileWriter(outfile);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Name,Hit Density");
            bufferedWriter.newLine();

            for (String name : names) {
                bufferedWriter.write(name);
                for (Double density : artistDensityOverCareer.get(name)) {
                    bufferedWriter.write("," + density);
                }
                bufferedWriter.newLine();
            }

            bufferedWriter.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }



    }

}