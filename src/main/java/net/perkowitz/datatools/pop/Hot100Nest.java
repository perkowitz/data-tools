package net.perkowitz.datatools.pop;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class Hot100Nest {

    private EchoNestAPI echoNest;

    public static void main(String[] args) throws EchoNestException {

        if (args.length < 4) {
            System.out.println("Usage: Hot100Nest <datafile> <start year> <end year> <API key>");
            return;
        }

        Hot100Nest app = new Hot100Nest();
        app.run(args[0], new Integer(args[1]), new Integer(args[2]), args[3]);

    }

    public void run(String datafile, int startYear, int endYear, String apiKey) throws EchoNestException {

        echoNest = new EchoNestAPI(apiKey);

        Map<String,Artist> artistMap = Maps.newHashMap();
        Map<Integer,Double> yearHotttnesss = Maps.newHashMap();
        Map<Integer,Double> yearFamiliarity = Maps.newHashMap();

        for (int year=startYear; year<=endYear; year++) {
            yearHotttnesss.put(year,0d);
            yearFamiliarity.put(year,0d);
        }

        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(datafile)));

            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                String[] fields = StringUtils.split(line, ",");

                Integer year = new Integer(fields[0]);
                Integer rank = new Integer(fields[1]);
                String artistName = fields[3];

                if (year >= startYear && year <= endYear) {

                    Artist artist = artistMap.get(artistName);
                    if (artist == null) {
                        artist = EchoNestHelper.getArtist(echoNest,artistName);
                    }

                    if (artist != null) {
                        yearHotttnesss.put(year,yearHotttnesss.get(year) + artist.getHotttnesss());
                        yearFamiliarity.put(year,yearFamiliarity.get(year) + artist.getFamiliarity());
                    }

                }

            }

            input.close();


        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        for (int year=startYear; year<=endYear; year++) {
            System.out.printf("%d,%f,%f\n",year,yearHotttnesss.get(year),yearFamiliarity.get(year));
        }

    }


}
