package net.perkowitz.datatools.pop;


import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtistHelper {

    public static List<String> parseNames(String name, Set<String> artistNames) {

        List<String> patternStrings = Lists.newArrayList();
        patternStrings.add("(.*)\\(.*with (.*)\\)");    // all the patterns are applied case insensitive
        patternStrings.add("(.*) with (.*)");
        patternStrings.add("(.*)\\(.*featuring (.*)\\)");
        patternStrings.add("(.*) featuring (.*)");
        // should have "Person1 and Person2" but only if at least one is in the artistNames

        for (String patternString : patternStrings) {
            Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                String name1 = matcher.group(1).trim();
                String name2 = matcher.group(2).trim();
                return Lists.newArrayList(name1,name2);
            }
        }

        return Lists.newArrayList(name);

    }

}
