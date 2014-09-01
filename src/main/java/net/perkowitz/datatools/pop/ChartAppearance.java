package net.perkowitz.datatools.pop;


import org.apache.commons.lang.StringUtils;

public class ChartAppearance {

    public static final Integer CHART_WEEK_INDEX = 0;
    public static final Integer CHART_DATESTRING_INDEX = 1;
    public static final Integer CHART_YEAR_INDEX = 2;
    public static final Integer CHART_MONTH_INDEX = 3;
    public static final Integer CHART_DAY_INDEX = 4;
    public static final Integer CHART_RANK_INDEX = 5;
    public static final Integer CHART_ARTIST_INDEX = 6;
    public static final Integer CHART_TITLE_INDEX = 7;

    private Integer week;
    private Integer rank;
    private Integer year;
    private Integer month;
    private Integer day;
    private String artist;
    private String title;

    public ChartAppearance(String line) {

        String[] fields = StringUtils.split(line,",");
        week = new Integer(fields[CHART_WEEK_INDEX]);
        year = new Integer(fields[CHART_YEAR_INDEX]);
        month = new Integer(fields[CHART_MONTH_INDEX]);
        day = new Integer(fields[CHART_DAY_INDEX]);
        rank = new Integer(fields[CHART_RANK_INDEX]);
        artist = fields[CHART_ARTIST_INDEX];
        title = fields[CHART_TITLE_INDEX];

    }

    public Integer getWeek() {
        return week;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getDay() {
        return day;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }
}
