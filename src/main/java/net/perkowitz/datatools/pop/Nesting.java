package net.perkowitz.datatools.pop;


import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;

import java.util.List;

public class Nesting {

    public void run(String apiKey) throws EchoNestException {

        EchoNestAPI echoNest = new EchoNestAPI(apiKey);
        List<com.echonest.api.v4.Artist> artists = echoNest.searchArtists("Madonna");

        if (artists.size() > 0) {
            com.echonest.api.v4.Artist artist = artists.get(0);
            System.out.println("Similar artists for " + artist.getName());
            for (com.echonest.api.v4.Artist simArtist : artist.getSimilar(10)) {
                System.out.println("   " + simArtist.getName());
            }
        }


    }


    public static void main(String[] args) throws EchoNestException {
        Nesting nesting = new Nesting();
        long start = System.currentTimeMillis();
        try {
            nesting.run(args[0]);
        } finally {
            System.out.println("Runtime " + (System.currentTimeMillis() - start));
        }
    }
}
