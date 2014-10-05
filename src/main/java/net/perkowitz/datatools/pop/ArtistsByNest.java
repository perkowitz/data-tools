package net.perkowitz.datatools.pop;


import com.echonest.api.v4.Artist;
import com.echonest.api.v4.ArtistParams;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Params;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ArtistsByNest {

    private static Integer maxRankToCompute = 10;
    private static Integer QUERY_LIMIT_PER_MINUTE = 110;
    private static Integer QUERY_SLEEP_SECONDS = 60;

    private EchoNestAPI echoNest;
    private int queryCount = 0;


    public static void main(String[] args) throws EchoNestException {

        if (args.length < 3) {
            System.out.println("Usage: ArtistsBynest <songfile> <mapfile> <API key>");
            return;
        }

        ArtistsByNest artistsByNest = new ArtistsByNest();
        artistsByNest.run(args[0],args[1],args[2]);

    }

    public void run(String songfile, String mapfile, String apiKey) throws EchoNestException {

        Map<String,PopArtist> artists = Maps.newHashMap();
        echoNest = new EchoNestAPI(apiKey);
        Map<String,Artist> artistMap = Maps.newHashMap();
        Map<String,Song> songMap = Maps.newHashMap();
        Set<String> lostSongs = Sets.newHashSet();
        int notFound = 0;

        ArtistMapper artistMapper = new ArtistMapper(mapfile);
        artistMapper.setReturnOnlyMappedArtists(false);

        Map<Integer,Double> hotByWeek = Maps.newHashMap();

        BufferedReader input;
        String outfile = "song-hotttnesss.csv";
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            input = new BufferedReader(new FileReader(new File(songfile)));

            fileWriter = new FileWriter(outfile);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Week,Date,Year,Month,Day,Rank,Artist,Title,Song Hotttnesss,Artist Hotttnesss,Artist Familiarity");
            bufferedWriter.newLine();

            String line;
            while ((line = input.readLine()) != null) {

                ChartAppearance chartAppearance = new ChartAppearance(line);
                Set<String> artistNames = artistMapper.getArtistNames(chartAppearance.getArtist());

                double songHotttnesss = 0d;
                double artistHotttnesss = 0d;
                double artistFamiliarity = 0d;

                for (String name : artistNames) {

                    PopArtist popArtist = artists.get(name);
                    if (popArtist == null) {
                        popArtist = new PopArtist(name);
                        artists.put(name,popArtist);
                    }

                    Artist echoNestArtist = artistMap.get(name);
                    if (echoNestArtist == null) {
                        echoNestArtist = EchoNestHelper.getArtist(echoNest,name);
                        artistMap.put(name, echoNestArtist);
                    }

                    String songKey = chartAppearance.getArtist() + "," + chartAppearance.getTitle();
                    if (lostSongs.contains(songKey)) {
                        continue;
                    }
                    Song song = songMap.get(songKey);
                    if (song == null && echoNestArtist != null) {
                        song = EchoNestHelper.getSong(echoNest,echoNestArtist.getID(),chartAppearance.getTitle());
                        if (song == null) {
                            Artist songArtist = EchoNestHelper.getArtist(echoNest,chartAppearance.getArtist());
                            if (songArtist != null) {
                                song = EchoNestHelper.getSong(echoNest,songArtist.getID(),chartAppearance.getTitle());
                            }
                        }

                        if (song != null) {
                            songMap.put(songKey,song);
                            System.out.printf("%f,%s,%s\n",song.getSongHotttnesss(),echoNestArtist.getName(),song.getTitle());
                            popArtist.addHotttness(song.getSongHotttnesss());
                        } else {
                            lostSongs.add(songKey);
                            System.err.printf("Song not found: %s\n",songKey);
                            notFound++;
                        }
                    }

                    if (song != null && song.getSongHotttnesss() > songHotttnesss) {
                        songHotttnesss = song.getSongHotttnesss();
                    }
                    if (echoNestArtist != null && echoNestArtist.getHotttnesss() > artistHotttnesss) {
                        artistHotttnesss = echoNestArtist.getHotttnesss();
                    }
                    if (echoNestArtist != null && echoNestArtist.getFamiliarity() > artistFamiliarity) {
                        artistFamiliarity = echoNestArtist.getFamiliarity();
                    }

                    bufferedWriter.write(line + "," + songHotttnesss + "," + artistHotttnesss + "," + artistFamiliarity);
                    bufferedWriter.newLine();

                }

            }

            input.close();
            bufferedWriter.close();


        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

//        for (PopArtist artist : artists.values()) {
//            System.out.printf("%s: total=%f, avg=%f\n",artist.getName(),artist.getTotalHotttness(),artist.getAverageHotttness());
//        }

        System.err.printf("Failed to find %d songs.\n",notFound);

    }

}