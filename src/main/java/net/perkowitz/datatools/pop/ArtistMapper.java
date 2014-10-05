package net.perkowitz.datatools.pop;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ArtistMapper {

    private Set<String> artistNames = Sets.newHashSet();
    private Map<String,Set<String>> aliasToArtistMap = Maps.newHashMap(); // one alias can apply to multiple
    private boolean returnOnlyMappedArtists = true;

    public ArtistMapper(String mapfile) {

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

    }

    public Set<String> getArtistNames(String name) {

        Set<String> results = Sets.newHashSet();
        for (String artistName : ArtistHelper.parseNames(name,null)) {
            if (aliasToArtistMap.get(artistName) != null) {
                results.addAll(aliasToArtistMap.get(artistName));
            }
        }

        if (results.size() == 0 && !returnOnlyMappedArtists) {
            results.add(name);
        }

        return results;
    }

    public void setReturnOnlyMappedArtists(boolean returnOnlyMappedArtists) {
        this.returnOnlyMappedArtists = returnOnlyMappedArtists;
    }
}
