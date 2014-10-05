package net.perkowitz.datatools.pop;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.ArtistParams;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EchoNestHelper {

    private static Integer QUERY_LIMIT_PER_MINUTE = 110;
    private static Integer QUERY_SLEEP_SECONDS = 60;

    private static int queryCount = 0;

    public static Artist getArtist(EchoNestAPI echoNest, String name) throws EchoNestException {

        querySleep();

        ArtistParams p = new ArtistParams();
        p.add("name", name);
        p.add("results", 5);
        p.includeFamiliarity();
        p.includeHotttnesss();
        List<Artist> echoNestArtists = echoNest.searchArtists(p);
        if (echoNestArtists.size() > 0) {
            return echoNestArtists.get(0);
        }

        return null;
    }

    public static Song getSong(EchoNestAPI echoNest, String artistId, String title) throws EchoNestException {

        querySleep();

        SongParams p = new SongParams();
        p.add("title", title);
        p.add("artist_id", artistId);
        p.add("results", 1);
        p.sortBy(SongParams.SORT_SONG_HOTTTNESSS, false);
        p.includeSongHotttnesss();
//        p.includeAudioSummary();

        List<Song> songs = echoNest.searchSongs(p);
        if (songs.size() > 0) {
            return songs.get(0);
        }

        return null;
    }

    public static void querySleep() {

        queryCount++;

        if (queryCount > QUERY_LIMIT_PER_MINUTE) {
            try {
                System.err.println("Query sleeping...");
                TimeUnit.SECONDS.sleep(QUERY_SLEEP_SECONDS);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            queryCount = 0;
        }



    }

}
