package net.perkowitz.datatools.pop;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

public class Artist implements Comparable<Artist> {

    public static final String DELIMITER = ",";
    public static final String SECONDARY_DELIMITER = "|";

    public static final Integer COMPARE_TOPONE_SONGS = 0;
    public static final Integer COMPARE_TOPONE_WEEKS = 1;
    public static final Integer COMPARE_TOPONE_APPEARANCES = 2;
    public static final Integer COMPARE_TOPTEN_SONGS = 3;
    public static final Integer COMPARE_TOPTEN_WEEKS = 4;
    public static final Integer COMPARE_TOPTEN_APPEARANCES = 5;
//    public static final Integer COMPARE_TOPONE_SONGS = 1;
//    public static final Integer COMPARE_TOPONE_WEEKS = 5;
//    public static final Integer COMPARE_TOPONE_APPEARANCES = 3;
//    public static final Integer COMPARE_TOPTEN_SONGS = 0;
//    public static final Integer COMPARE_TOPTEN_WEEKS = 4;
//    public static final Integer COMPARE_TOPTEN_APPEARANCES = 2;
    public static final Integer COMPARE_MAX = 5;

    public static int compareMethod = COMPARE_TOPTEN_SONGS;

    private String name;

    private Set<String> topTenSongs = Sets.newHashSet();
    private Set<String> numberOneSongs = Sets.newHashSet();
    private Set<Integer> weeksInTopTen = Sets.newHashSet();

    private Integer totalTopTenAppearances = 0;
    private Integer totalNumberOneAppearances = 0;
    private Integer firstWeek = null;
    private Integer lastWeek = null;

    private Integer[] ranks = new Integer[COMPARE_MAX+1];

    public Artist(String name) {
        this.name = name;
    }

    public void addChartAppearance(ChartAppearance chartAppearance) {

        totalTopTenAppearances++;
        topTenSongs.add(chartAppearance.getTitle());
        weeksInTopTen.add(chartAppearance.getWeek());

        if (chartAppearance.getRank() == 1) {
            totalNumberOneAppearances++;
            numberOneSongs.add(chartAppearance.getTitle());
        }

        if (firstWeek == null || chartAppearance.getWeek() < firstWeek) {
            firstWeek = chartAppearance.getWeek();
        }
        if (lastWeek == null || chartAppearance.getWeek() > lastWeek) {
            lastWeek = chartAppearance.getWeek();
        }

    }

    public void addRank(Integer compareMethod, Integer rank) {
        ranks[compareMethod] = rank;
    }

    public String toString() {
        return name + DELIMITER
                + getTotalNumberOneSongs() + DELIMITER
                + getTotalWeeksAtNumberOne() + DELIMITER
//                + getTotalNumberOneAppearances() + DELIMITER
                + getTotalTopTenSongs() + DELIMITER
                + getTotalWeeksInTopTen() + DELIMITER
                + getTotalTopTenAppearances() + DELIMITER
                + getFirstWeek() + DELIMITER
                + getLastWeek() + DELIMITER
                + getCareerSpanInWeeks() + DELIMITER
                + getHitDensity()
//                + DELIMITER + getRanksString()
                ;
    }

    public String getRanksString() {
        return StringUtils.join(ranks, DELIMITER);
    }

    public boolean isFullyRanked() {

        for (int compareMethod=0; compareMethod<=Artist.COMPARE_MAX; compareMethod++) {
            if (ranks[compareMethod] == null) {
                return false;
            }
        }

        return true;
    }

    public int compareTo(Artist artist) {

        if (compareMethod == COMPARE_TOPTEN_SONGS) {
            return this.getTotalTopTenSongs().compareTo(artist.getTotalTopTenSongs());
        } else if (compareMethod == COMPARE_TOPONE_SONGS) {
            return this.getTotalNumberOneSongs().compareTo(artist.getTotalNumberOneSongs());
        } else if (compareMethod == COMPARE_TOPTEN_APPEARANCES) {
            return this.getTotalTopTenAppearances().compareTo(artist.getTotalTopTenAppearances());
        } else if (compareMethod == COMPARE_TOPONE_APPEARANCES) {
            return this.getTotalNumberOneAppearances().compareTo(artist.getTotalNumberOneAppearances());
        } else if (compareMethod == COMPARE_TOPTEN_WEEKS) {
            return this.getTotalWeeksInTopTen().compareTo(artist.getTotalWeeksInTopTen());
        } else if (compareMethod == COMPARE_TOPONE_WEEKS) {
            return this.getTotalWeeksAtNumberOne().compareTo(artist.getTotalWeeksAtNumberOne());
        }

        return this.getTotalTopTenSongs().compareTo(artist.getTotalTopTenSongs());
    }

    public double getHitDensity() {

        return Math.round(1000 * (double)getTotalWeeksInTopTen() / (double)getCareerSpanInWeeks()) / (double)1000;

    }

    public Integer getScore() {
        return getTotalNumberOneSongs() + getTotalWeeksAtNumberOne()
                + getTotalTopTenSongs() + getTotalWeeksInTopTen() + getTotalTopTenAppearances();
    }

    public Integer getCareerSpanInWeeks() {
        return lastWeek - firstWeek + 1;
    }

    public Integer getTotalTopTenSongs() {
        return topTenSongs.size();
    }

    public Integer getTotalNumberOneSongs() {
        return numberOneSongs.size();
    }

    public Integer getTotalWeeksInTopTen() {
        return weeksInTopTen.size();
    }

    public Integer getTotalWeeksAtNumberOne() {
        return totalNumberOneAppearances; // redundant with number one appearances
    }

    public Integer getTotalTopTenAppearances() {
        return totalTopTenAppearances;
    }

    public Integer getTotalNumberOneAppearances() {
        return totalNumberOneAppearances;
    }

    public Integer getFirstWeek() {
        return firstWeek;
    }

    public Integer getLastWeek() {
        return lastWeek;
    }

    public String getName() {
        return name;
    }

    public static void setCompareMethod(int compareMethod) {
        Artist.compareMethod = compareMethod;
    }

    public Integer[] getRanks() {
        return ranks;
    }

}
