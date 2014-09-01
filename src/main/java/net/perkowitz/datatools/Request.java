package net.perkowitz.datatools;

import com.google.common.collect.Lists;

import java.util.List;

public class Request {

    private Integer count = 0;
    private String name;
    private List<String> evidencePhrases = Lists.newArrayList();

    public Request(String name) {
        this.name = name;
    }

    public void count(String evidencePhrase) {
        count++;
        evidencePhrases.add(evidencePhrase);
    }

    public Integer getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public List<String> getEvidencePhrases() {
        return evidencePhrases;
    }
}
