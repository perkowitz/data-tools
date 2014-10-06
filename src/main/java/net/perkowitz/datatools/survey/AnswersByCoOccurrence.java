package net.perkowitz.datatools.survey;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class AnswersByCoOccurrence {

    private static final String delimiter = "+";

    private static SentenceDetector sentenceDetector;
    private static Tokenizer tokenizer;
    private static POSTagger posTagger;
    private static ChunkerME chunker;
    private static Set<String> stopWords;
    private static Integer minimumCount = 0;
    private static Boolean useLocations = false;

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: Answer <answerfile> <minimum count> <useLocations>");
            return;
        }

        String answerfile = args[0];
        minimumCount = new Integer(args[1]);
        useLocations = new Boolean(args[2]);

        initializeModels();
        Map<String,Integer> counts = Maps.newHashMap();
        Map<String,Request> requests = Maps.newHashMap();
        Map<String,Map<String,Request>> requestsByCity = Maps.newHashMap();

        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(answerfile)));

            String line;
            while ((line = input.readLine()) != null) {
                if (line.startsWith(",")) {
                    line = "none" + line;
                }
                String[] fields = StringUtils.split(line,",");
                if (fields.length >= 3) {
                    String city = fields[0];
                    String question = fields[1];
                    String answer = fields[2];
                    for (int i=3; i<fields.length; i++) {
                        answer += "," + fields[i];
                    }
//                    if (!city.equals("Miami")) {
//                        continue;
//                    }
//                    System.out.printf("%s -- %s\n",city,answer);


                    Map<String,Request> cityRequests = requestsByCity.get(city);
                    if (cityRequests == null) {
                        cityRequests = Maps.newHashMap();
                        requestsByCity.put(city,cityRequests);
                    }

                    String[] sentences = sentenceDetector.sentDetect(fields[2]);

                    for (String sentence : sentences) {

//                        System.out.printf("- %s\n",sentence);
                        Set<String> wordSet = Sets.newHashSet();
                        for (String word : tokenizer.tokenize(sentence)) {
                            word = word.toLowerCase();
                            if (!stopWords.contains(word) && word.length() > 2 && Pattern.matches("[a-z]+", word) == true) {
                                wordSet.add(word);
                            }
                        }

                        List<String> words = Lists.newArrayList(wordSet);
                        Collections.sort(words);
//                        for (String word : words) {
//                            System.out.printf("%s, ",word);
//                        }
//                        System.out.println();

                        for (int i=0; i<words.size()-1; i++) {
                            for (int j=i+1; j<words.size(); j++) {
                                String pair = words.get(i) + "+" + words.get(j);
                                count(counts,pair);
                                countRequest(requests,pair,sentence);
                                countRequest(cityRequests,pair,sentence);

                                for (int k=j+1; k<words.size(); k++) {
                                    String triple = words.get(i) + "+" + words.get(j) + "+" + words.get(k);
                                    count(counts,triple);
                                    countRequest(requests,triple,sentence);
                                    countRequest(cityRequests,triple,sentence);
                                }
                            }
                        }


                    }

                }

            }

            input.close();

            outputRequests("everywhere",requests,minimumCount);
            for (String city : requestsByCity.keySet()) {
                outputRequests(city,requestsByCity.get(city),3);
            }

        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }


    }

    private static void count(Map<String,Integer> counts, String string) {
        if (counts.get(string) == null) {
            counts.put(string,1);
        } else {
            counts.put(string, counts.get(string) + 1);
        }
    }

    private static void countRequest(Map<String,Request> requests, String string, String phrase) {
        if (requests.get(string) == null) {
            Request request = new Request(string);
            request.count(phrase);
            requests.put(string,request);
        } else {
            Request request = requests.get(string);
            request.count(phrase);
        }
    }

    private static void initializeModels() {

        loadStopwords();

        tokenizer = SimpleTokenizer.INSTANCE;

        InputStream modelIn = null;
        try {
            // loading tokenizer model
            modelIn = AnswersByCoOccurrence.class.getResourceAsStream("/en-pos-maxent.bin");
            final POSModel posModel = new POSModel(modelIn);
            modelIn.close();
            posTagger = new POSTaggerME(posModel);

            // loading sentence model
            modelIn = AnswersByCoOccurrence.class.getResourceAsStream("/en-sent.bin");
            final SentenceModel sentModel= new SentenceModel(modelIn);
            modelIn.close();
            sentenceDetector = new SentenceDetectorME(sentModel);

            // loading chunker model
            modelIn = AnswersByCoOccurrence.class.getResourceAsStream("/en-chunker.bin");
            final ChunkerModel chunkerModel = new ChunkerModel(modelIn);
            modelIn.close();
            chunker = new ChunkerME(chunkerModel);

        } catch (final IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (final IOException e) {} // oh well!
            }
        }
    }

    private static void output(String filename, Map<String,Integer> counts, Integer minimum) {

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileWriter = new FileWriter(filename);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (String key : counts.keySet()) {
                if (counts.get(key) >= minimum) {
                    bufferedWriter.write(counts.get(key) + "," + key + "\n");
                }
            }

            bufferedWriter.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }

    }

    private static void outputRequests(String name, Map<String,Request> requests, Integer minimum) {

        String allFile = "co-" + name + "-all.csv";
        String bestFile = "co-" + name + "-best.csv";
        String detailFile = "co-" + name + "-detail.txt";


        try {

            BufferedWriter allWriter = new BufferedWriter(new FileWriter(allFile));
            BufferedWriter bestWriter = new BufferedWriter(new FileWriter(bestFile));
            BufferedWriter detailWriter = new BufferedWriter(new FileWriter(detailFile));

            int bestCount = 0;

            for (String key : requests.keySet()) {

                Request request = requests.get(key);
                Integer count = request.getCount();

                allWriter.write(count + "," + key);
                allWriter.newLine();

                if (count >= minimum) {

                    bestCount++;

                    bestWriter.write(count + "," + key);
                    bestWriter.newLine();

                    detailWriter.write(count + "," + key);
                    detailWriter.newLine();
                    for (String phrase : requests.get(key).getEvidencePhrases()) {
                        detailWriter.write("- " + phrase);
                        detailWriter.newLine();
                    }

                }

            }

            allWriter.close();
            bestWriter.close();
            detailWriter.close();

            if (bestCount == 0) {
                Files.deleteIfExists(Paths.get(bestFile));
                Files.deleteIfExists(Paths.get(detailFile));
            }

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }

    }


    public static void loadStopwords() {

        try {
            InputStream inputStream = AnswersByCoOccurrence.class.getResourceAsStream("/stopwords.csv");
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            stopWords = Sets.newHashSet();
            while ((line = inputReader.readLine()) != null) {
                stopWords.add(line.toLowerCase());
            }
            inputReader.close();

        } catch (IOException e) {
            System.err.printf("%s\n", e);
        }
    }

}