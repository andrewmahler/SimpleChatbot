import java.util.*;
import java.io.*;

class Segment {
    public int i;
    public double left;
    public double right;

    Segment(int i, double left, double right) {
        this.i = i;
        this.left = left;
        this.right = right;
    }
}

public class Chatbot{
    public static ArrayList<Segment> segments = new ArrayList<Segment>();
    private static String filename = "./WARC201709_wid.txt";
    private static ArrayList<Integer> readCorpus(){
        ArrayList<Integer> corpus = new ArrayList<Integer>();
        try{
            File f = new File(filename);
            Scanner sc = new Scanner(f);
            while(sc.hasNext()){
                if(sc.hasNextInt()){
                    int i = sc.nextInt();
                    corpus.add(i);
                }
                else{
                    sc.next();
                }
            }
        }
        catch(FileNotFoundException ex){
            System.out.println("File Not Found.");
        }
        return corpus;
    }

    public static int randomUnigramFinder(int n1, int n2, double rand, double[] probs) {
        segments = new ArrayList<Segment>();
        // add first segment
        Segment first = new Segment(0,0.0, probs[0]);
        segments.add(first);

        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > 0) {
                double left = 0;
                for (int j = 0; j < i; j++) {
                    left += probs[j];
                }
                double right = left + probs[i];
                Segment newSegment = new Segment(i, left, right);
                segments.add(newSegment);
            }
        }
        int i = 0;

        while (i < segments.size() && rand > segments.get(i).right) {
            i++;
        }
        return i;
    }

    public static int randomBigramFinder(int n1, int n2, int h, double rand, ArrayList<Integer> corpus) {
        segments = new ArrayList<Segment>();
        int[] hCounts = new int[4700];
        double[] hFreqs = new double[4700];

        ArrayList<Integer> words_after_h = new ArrayList<Integer>();
        for (int i = 0; i < corpus.size();i++) {
            if (corpus.get(i) == h) {
                if (i + 1 < corpus.size()) {
                    words_after_h.add(corpus.get(i+1));
                }
            }
        }

        for (int i = 0; i < words_after_h.size(); i++) {
            hCounts[words_after_h.get(i)] = hCounts[words_after_h.get(i)] + 1;
        }
        for (int i = 0; i < hFreqs.length; i++) {
            hFreqs[i] = (double)hCounts[i]/(double)words_after_h.size();
            //System.out.println(hFreqs[i]);
        }

        // add first segment
        Segment first = new Segment(0,0.0, hFreqs[0]);
        segments.add(first);

        for (int i = 1; i < hFreqs.length; i++) {
            if (hFreqs[i] > 0) {
                double left = 0;
                for (int j = 0; j < i; j++) {
                    left += hFreqs[j];
                }
                double right = left + hFreqs[i];
                Segment newSegment = new Segment(i, left, right);
                segments.add(newSegment);
            }
        }
        int i = 0;
        while (i < segments.size() && rand > segments.get(i).right) {
            i++;
        }
        return i;
    }

    public static int randomTrigramFinder(int n1, int n2, int h1, int h2, double rand, ArrayList<Integer> corpus) {
        segments = new ArrayList<Segment>();
        int[] h1h2Counts = new int[4700];
        double[] h1h2Freqs = new double[4700];
        ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();

        for (int i = 0; i < corpus.size();i++) {
            if (corpus.get(i) == h1) {
                if (i + 1 < corpus.size() -1 && corpus.get(i+1) == h2) {
                    words_after_h1h2.add(corpus.get(i+2));
                }
            }
        }

        for (int i = 0; i < words_after_h1h2.size(); i++) {
            h1h2Counts[words_after_h1h2.get(i)] = h1h2Counts[words_after_h1h2.get(i)] + 1;
        }
        for (int i = 0; i < h1h2Freqs.length; i++) {
            h1h2Freqs[i] = (double)h1h2Counts[i]/(double)words_after_h1h2.size();
        }

        // add first segment
            /*Segment first = new Segment(0,0.0, h1h2Freqs[0]);
            segments.add(first);*/

        for (int i = 0; i < h1h2Freqs.length; i++) {
            if (h1h2Freqs[i] > 0) {
                double left = 0;
                for (int j = 0; j < i; j++) {
                    left += h1h2Freqs[j];
                }
                double right = left + h1h2Freqs[i];
                Segment newSegment = new Segment(i, left, right);
                segments.add(newSegment);
            }
        }
        int i = 0;
        while (i < segments.size() && rand > segments.get(i).right) {
            i++;
        }

        return i;
    }

    static public void main(String[] args){
        ArrayList<Integer> corpus = readCorpus();
        int flag = Integer.valueOf(args[0]);
        double[] probs = new double[4700];
        int[] freqs = new int[4700];

        // setting up frequencies and probabilities
        for (int i = 0; i < corpus.size(); i++) {
            freqs[corpus.get(i)] = freqs[corpus.get(i)] + 1;
        }
        // calculating probabilities for each
        for (int i = 0; i < freqs.length; i++) {
            probs[i] = freqs[i]/(double)corpus.size();
        }

        if(flag == 100){
            int w = Integer.valueOf(args[1]);
            int count = freqs[w];
            System.out.println(count);
            System.out.println(String.format("%.7f",count/(double)corpus.size()));
        }
        else if(flag == 200){
            int n1 = Integer.valueOf(args[1]);
            int n2 = Integer.valueOf(args[2]);
            double rand = (double)n1/(double)n2;

            int i = randomUnigramFinder(n1, n2, rand, probs);

            System.out.println(i);
            System.out.println(String.format("%.7f",segments.get(i).left));
            System.out.println(String.format("%.7f",segments.get(i).right));
        }
        else if(flag == 300){
            int h = Integer.valueOf(args[1]);
            int w = Integer.valueOf(args[2]);
            int count = 0;
            ArrayList<Integer> words_after_h = new ArrayList<Integer>();
            for (int i = 0; i < corpus.size();i++) {
                if (corpus.get(i) == h) {
                    if (i + 1 < corpus.size()) {
                        words_after_h.add(corpus.get(i+1));
                        if (corpus.get(i+1) == w) count++;
                    }
                }
            }
            //output
            System.out.println(count);
            System.out.println(words_after_h.size());
            System.out.println(String.format("%.7f",count/(double)words_after_h.size()));
        }
        // TODO: for this and option 2 make sure that the bounds are working correctly, (open/closed brackets)
        else if(flag == 400){
            int n1 = Integer.valueOf(args[1]);
            int n2 = Integer.valueOf(args[2]);
            int h = Integer.valueOf(args[3]);
            double rand = (double)n1/(double)n2;
            int i = randomBigramFinder(n1, n2, h, rand, corpus);
            System.out.println(segments.get(i).i);
            System.out.println(String.format("%.7f",segments.get(i).left));
            System.out.println(String.format("%.7f",segments.get(i).right));

        }
        else if(flag == 500){
            int h1 = Integer.valueOf(args[1]);
            int h2 = Integer.valueOf(args[2]);
            int w = Integer.valueOf(args[3]);
            int count = 0;
            ArrayList<Integer> words_after_h1h2 = new ArrayList<Integer>();
            for (int i = 0; i < corpus.size();i++) {
                if (corpus.get(i) == h1) {
                    if (i + 1 < corpus.size() -1 && corpus.get(i+1) == h2) {
                        words_after_h1h2.add(corpus.get(i+2));
                    }
                }
            }
            for (int i = 0; i < words_after_h1h2.size(); i++) {
                if (words_after_h1h2.get(i) == w) count++;
            }

            //output
            System.out.println(count);
            System.out.println(words_after_h1h2.size());
            if(words_after_h1h2.size() == 0)
                System.out.println("undefined");
            else
                System.out.println(String.format("%.7f",count/(double)words_after_h1h2.size()));
        }
        else if(flag == 600){
            int n1 = Integer.valueOf(args[1]);
            int n2 = Integer.valueOf(args[2]);
            int h1 = Integer.valueOf(args[3]);
            int h2 = Integer.valueOf(args[4]);
            double rand = (double)n1/(double)n2;

            int i = randomTrigramFinder(n1, n2,h1, h2, rand,corpus);

            // seeing if the trigram is defined
            if (segments.size() == 0) {
                System.out.println("undefined");
            } else {
                System.out.println(segments.get(i).i);
                System.out.println(String.format("%.7f", segments.get(i).left));
                System.out.println(String.format("%.7f", segments.get(i).right));
            }
        }
        else if(flag == 700){
            int seed = Integer.valueOf(args[1]);
            int t = Integer.valueOf(args[2]);
            int h1=0,h2=0;

            Random rng = new Random();
            if (seed != -1) rng.setSeed(seed);

            if(t == 0){
                // TODO Generate first word using r
                double r = rng.nextDouble();
                h1 = randomUnigramFinder(0,0,r, probs);
                h1 = segments.get(h1).i;
                System.out.println(h1);
                if(h1 == 9 || h1 == 10 || h1 == 12){
                    return;
                }

                // TODO Generate second word using r
                r = rng.nextDouble();
                h2 = randomBigramFinder(0,0,h1,r,corpus);
                h2 = segments.get(h2).i;
                System.out.println(h2);
            }
            else if(t == 1){
                h1 = Integer.valueOf(args[3]);
                // TODO Generate second word using r
                double r = rng.nextDouble();
                h2 = randomBigramFinder(0,0,h1, r, corpus);
                h2 = segments.get(h2).i;
                System.out.println(h2);
            }
            else if(t == 2){
                h1 = Integer.valueOf(args[3]);
                h2 = Integer.valueOf(args[4]);
            }

            while(h2 != 9 && h2 != 10 && h2 != 12){
                double r = rng.nextDouble();
                int w  = 0;
                // TODO Generate new word using h1,h2
                w = randomTrigramFinder(0,0,h1, h2, r, corpus);
                w = segments.get(w).i;
                System.out.println(w);
                h1 = h2;
                h2 = w;
            }
        }

        return;
    }
}

