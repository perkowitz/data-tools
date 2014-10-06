package net.perkowitz.datatools.survey;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import opennlp.tools.chunker.Chunker;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnswersByChunking {

    private static SentenceDetector sentenceDetector;
    private static Tokenizer tokenizer;
    private static POSTagger posTagger;
    private static ChunkerME chunker;
    private static Set<String> stopWords;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: Answer <answerfile>");
            return;
        }

        String answerfile = args[0];

        initializeModels();

        Map<String,Integer> wordCounts = Maps.newHashMap();
        Map<String,Integer> nounCounts = Maps.newHashMap();
        Map<String,Integer> phraseCounts = Maps.newHashMap();

        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(new File(answerfile)));

            String line;
            while ((line = input.readLine()) != null) {
                String[] fields = StringUtils.split(line,",");
                if (fields.length >= 3) {
                    String city = fields[0];
                    String question = fields[1];
                    String answer = fields[2];
                    for (int i=3; i<fields.length; i++) {
                        answer += "," + fields[i];
                    }
                    System.out.printf("-----\nRESPONSE: %s\n",answer);

                    String[] sentences = sentenceDetector.sentDetect(answer);

                    for (String sentence : sentences) {

                        System.out.printf("SENTENCE: %s\n",sentence);

                        String[] words = tokenizer.tokenize(sentence);
                        String[] tags = posTagger.tag(words);
                        String[] chunks = chunker.chunk(words, tags);

                        List<String> phrases = Lists.newArrayList();
                        List<String> currentPhrase = Lists.newArrayList();
                        System.out.printf("POS: ");
                        for (int i=0; i<words.length; i++) {

                            String word = words[i].toLowerCase();
                            String tag = tags[i];
                            String chunk = chunks[i];
                            System.out.printf("%s[%s] ",word,tag,chunk);

                            if (!stopWords.contains(word)) {
                                count(wordCounts,word);
                            }
                            if (tag.equals("NN") || tag.equals("NNS")) {
                                count(nounCounts,word);
                            }

                            if (chunk.equals("B-NP") || chunk.equals("I-NP")) {
                                currentPhrase.add(word);
                            } else {
                                if (currentPhrase.size() > 0) {
                                    phrases.add(StringUtils.join(currentPhrase," "));
                                    currentPhrase = Lists.newArrayList();
                                }
                            }

                        }
                        System.out.println();

                        System.out.printf("CHUNK: ");
                        for (int i=0; i<words.length; i++) {

                            String word = words[i].toLowerCase();
                            String tag = tags[i];
                            String chunk = chunks[i];
                            System.out.printf("%s[%s] ",word,chunk);

                            if (!stopWords.contains(word)) {
                                count(wordCounts,word);
                            }
                            if (tag.equals("NN") || tag.equals("NNS")) {
                                count(nounCounts,word);
                            }

                            if (chunk.equals("B-NP") || chunk.equals("I-NP")) {
                                currentPhrase.add(word);
                            } else {
                                if (currentPhrase.size() > 0) {
                                    phrases.add(StringUtils.join(currentPhrase," "));
                                    currentPhrase = Lists.newArrayList();
                                }
                            }

                        }
                        System.out.println();

                        if (currentPhrase.size() > 0) {
                            phrases.add(StringUtils.join(currentPhrase," "));
                        }

//                        for (String phrase : phrases) {
//                            System.out.printf("-- %s\n",phrase);
//                            count(phraseCounts,phrase);
//                        }
                    }

                }

            }

            input.close();


        } catch (IOException e) {
            System.out.printf("%s\n",e);
            return;
        }

        output("words.csv",wordCounts);
        output("nouns.csv",nounCounts);
        output("phrases.csv",phraseCounts);

    }

    private static void count(Map<String,Integer> counts, String string) {
        if (counts.get(string) == null) {
            counts.put(string,1);
        } else {
            counts.put(string,counts.get(string)+1);
        }
    }

    private static void initializeModels() {

        loadStopwords();

        tokenizer = SimpleTokenizer.INSTANCE;

        InputStream modelIn = null;
        try {
            // loading tokenizer model
            modelIn = AnswersByChunking.class.getResourceAsStream("/en-pos-maxent.bin");
            final POSModel posModel = new POSModel(modelIn);
            modelIn.close();
            posTagger = new POSTaggerME(posModel);

            // loading sentence model
            modelIn = AnswersByChunking.class.getResourceAsStream("/en-sent.bin");
            final SentenceModel sentModel= new SentenceModel(modelIn);
            modelIn.close();
            sentenceDetector = new SentenceDetectorME(sentModel);

            // loading chunker model
            modelIn = AnswersByChunking.class.getResourceAsStream("/en-chunker.bin");
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

    private static void output(String filename, Map<String,Integer> counts) {

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileWriter = new FileWriter(filename);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (String key : counts.keySet()) {
                bufferedWriter.write(counts.get(key) + "," + key + "\n");
            }

            bufferedWriter.close();

        } catch(IOException e) {
            System.err.printf("%s\n", e);
        }

    }


    public static void loadStopwords() {

        try {
            InputStream inputStream = AnswersByChunking.class.getResourceAsStream("/stopwords.csv");
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