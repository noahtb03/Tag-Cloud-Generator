import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;

/**
 * Tag Cloud Generator.
 *
 * @author Noah Bennett, Mark Karev
 */
public final class TagCloudGeneratorSJC {
    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGeneratorSJC() {
    }

    /**
     * String of separators.
     */
    private static String separatorsStr = "/\t\n\r.,&!? []{}|-=+@#$%*\"()'`";
    /**
     * Minimum font size.
     */
    private static final int MINSIZE = 11;
    /**
     * Amount of font sizes.
     */
    private static final int SIZES = 38;

    /**
     * Comparator IntOrder. Compares pairs of strings and integers sorts them by
     * the integer value.
     */
    private static class IntOrder
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * Comparator StrOrder. Compares pairs of strings and integers and sorts
     * them by the string value.
     */
    private static class StrOrder
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    /**
     * Returns a string of consecutive letters or separators.
     *
     * @param text
     *            String of characters the code is finding the word or separator
     *            in
     * @param position
     *            Where in the String the method needs to start finding the next
     *            word or separator
     * @param separators
     * @return str
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        //initialize variables
        String str = "";
        int i = position;

        //create a string of separators if the first character is a separator
        if (separators.contains(text.charAt(position))) {
            while (i < text.length() && separators.contains(text.charAt(i))) {
                str += text.charAt(i);
                i++;
            }
            //create a string of letters if the first character is a letter
        } else {
            while (i < text.length() && !separators.contains(text.charAt(i))) {
                str += text.charAt(i);
                i++;
            }
        }

        return str;
    }

    /**
     * Prints the opening tags and the header.
     *
     * @param n
     *            The amount of words in the tag cloud
     * @param out
     *            output stream
     * @param file
     *            The name of the input file
     */
    private static void printHeader(int n, PrintWriter out, String file) {
        assert out != null : "Violation of: Output stream is not null";
        assert !file.isEmpty() : "Violation of: file is not null";
        assert n > 0 : "Violation of: n > 0";

        //print the title and the opening html tags
        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>Top " + n + " words in " + file + "</title>");
        out.println(
                "    <link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("  </head>");
        out.println("  <body>");
        out.println("    <h2>Top " + n + " words in " + file + "</h2>");
        out.println("    <hr>");
        out.println("    <div class=\"cdiv\">");
        out.println("      <p class=\"cbox\">");
    }

    /**
     * Prints a word into the tag cloud.
     *
     * @param fontSize
     *            Font size of the word being added
     * @param count
     *            Amount of times the word appears in the file
     * @param word
     *            The word to be added to the tag cloud
     * @param out
     *            output stream
     */
    private static void printTags(int fontSize, int count, String word,
            PrintWriter out) {
        assert !word.isEmpty() : "Violation of: file is not null";
        assert count > 0 : "Violation of: count > 0";
        assert fontSize > 0 : "Violation of: size > 0";

        //print each word into the tag clout
        out.println("        " + "<span style=\"cursor:default\" class=\"f"
                + fontSize + "\" title=\"count: " + count + "\">" + word
                + "</span>");
    }

    /**
     * Prints out the closing tags on the html file.
     *
     * @param out
     *            output stream
     */
    private static void printFooter(PrintWriter out) {
        assert out != null : "Violation of: Output stream is not null";

        //print the footers
        out.println("      </p>");
        out.println("    </div>");
        out.println("  </body>");
        out.println("</html>");
    }

    /**
     * Calculates the size font size of a word given its word count.
     *
     * @param value
     *            the amount of times the word is used
     * @param n
     *            the amount of words in the tag cloud
     * @param maxword
     *            largest amount of times a word shows up in the input file
     * @return calcSize
     */
    private static int calcSize(int n, int value, int maxword) {
        assert value > 0 : "Violation of: value is greater than 0";
        assert maxword > 0 : "Violation of: maxword is greater than 0";

        //create an increment value and initialize other variables
        int increment = (maxword / SIZES) + 1;
        int size = MINSIZE;
        int i = 1;
        boolean sized = false;

        //Check each increment and increase it and the size until the word count
        //is less than the increment
        while (i <= SIZES && !sized) {
            if (value < i * increment) {
                sized = true;
            } else {
                size++;
            }
            i++;
        }

        return size;
    }

    /**
     * Adds pairs of all words and their counts to a list that sorts them by the
     * amount of times each word shows up.
     *
     * @param file
     *            The file which is to be read for all of the words and their
     *            counts.
     * @param sInts
     *            The list which sorts the pairs by the word count.
     * @requires The file string isn't empty.
     * @ensures List sInts is filled with pairs of all the words and their
     *          counts
     */
    private static void addToIntList(String file,
            List<Map.Entry<String, Integer>> sInts) {
        assert file.length() > 0 : "Violation of file is not empty";

        //create the map for words and their counts and a simple reader reading
        //the file with the text
        Map<String, Integer> words = new HashMap<>();
        SimpleReader in = new SimpleReader1L(file);

        //create a set with separators
        Set<Character> separators = new HashSet<Character>();
        for (int j = 0; j < separatorsStr.length(); j++) {
            separators.add(separatorsStr.charAt(j));
        }

        //while the file isn't empty remove each line, separate each word
        //and add it to the map if it doesn't contain separators,
        //if the word is already in the map, increment the word count
        while (!in.atEOS()) {
            String line = in.nextLine();
            line = line.toLowerCase();
            int position = 0;
            while (position < line.length()) {
                String word = nextWordOrSeparator(line, position, separators);
                if (words.containsKey(word)) {
                    words.replace(word, words.get(word), words.get(word) + 1);
                } else if (!separators.contains(word.charAt(0))) {
                    words.put(word, 1);
                }
                position += word.length();
            }

        }

        //create entry set and iterator
        Set<Map.Entry<String, Integer>> wordEntries = words.entrySet();
        Iterator<Map.Entry<String, Integer>> iter = wordEntries.iterator();

        //iterate through the set, add the pairs to the integer list
        while (iter.hasNext()) {
            Map.Entry<String, Integer> p = iter.next();
            sInts.add(p);
        }

        //close input stream
        in.close();
    }

    /**
     * Adds all of the top {@code n} pairs of words and their word count, and
     * returns the largest word count.
     *
     * @param n
     *            the number of words the user wants in the tag cloud.
     * @param sInts
     *            the list with all pairs of words and word counts in sorted
     *            order.
     * @param sStrs
     *            the list which is to have all of the top n words
     * @return max the largest word count
     * @requires sInts is not empty and sStrs is empty.
     */
    private static int addToStringListReturnMax(int n,
            List<Map.Entry<String, Integer>> sInts,
            List<Map.Entry<String, Integer>> sStrs) {
        assert sInts.size() > 0 : "Violation of sInts is not empty";
        assert sStrs.size() == 0 : "Violation of sStrings is empty";

        //create comparator, sort ints, remove the max and get its word count
        Comparator<Map.Entry<String, Integer>> intSort = new IntOrder();
        sInts.sort(intSort);
        Map.Entry<String, Integer> p = sInts.remove(0);
        int max = p.getValue();

        //add the top n words to the list to be sorted alphabetically
        for (int i = 0; i < n; i++) {
            sStrs.add(p);
            p = sInts.remove(0);
        }

        return max;
    }

    /**
     * Prints the entire html file.
     *
     * @param outFile
     *            File the tag cloud should be written to
     * @param n
     *            Amount of words in the tag cloud
     * @param file
     *            File to be read in
     * @param out
     *            Output stream
     */
    private static void printAll(PrintWriter out, int n, String file,
            String outFile) {
        assert !file.isEmpty() : "Violation of: file is not null";
        assert !outFile.isEmpty() : "Violation of: outFile is not null";

        //create a list to store the pairs sorted by word count and add all words
        List<Map.Entry<String, Integer>> sortedInts;
        sortedInts = new ArrayList<Map.Entry<String, Integer>>();
        addToIntList(file, sortedInts);

        //create a list to store pairs sorted by word. create a comparator for the
        //strings to sort them alphabetically.
        Comparator<Map.Entry<String, Integer>> strOrder = new StrOrder();
        List<Map.Entry<String, Integer>> sortedStrs;
        sortedStrs = new ArrayList<Map.Entry<String, Integer>>();

        //print opening html tags and header
        printHeader(n, out, file);

        //find the max word count if the file isn't empty
        int maxword = 0;
        if (sortedInts.size() > 0) {
            maxword = addToStringListReturnMax(n, sortedInts, sortedStrs);
        }

        //sort the strings alphabetically
        sortedStrs.sort(strOrder);

        //for the top n most common words, calculate the font and print it to the
        //output file
        for (int i = 0; i < n; i++) {
            if (sortedStrs.size() > 0) {
                Map.Entry<String, Integer> pStr = sortedStrs.remove(0);
                int size = calcSize(n, pStr.getValue(), maxword);
                printTags(size, pStr.getValue(), pStr.getKey(), out);
            }
        }

        //print closing tags
        printFooter(out);
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        String file = "";
        System.out.println("Input the location/file name: ");
        BufferedReader input = null;
        while (input == null) {
            try {
                file = in.readLine();
                input = new BufferedReader(new FileReader(file));

            } catch (IOException e) {
                System.err.println("Invalid file");
            }
            /*
             * prompt user for a tag cloud size, n
             */
            int n = 0;
            System.out.println(
                    "How many words would you like in the tag cloud: ");
            boolean flag = true;
            while (flag) {
                try {
                    n = Integer.parseInt(in.readLine());
                    if (n > 0) {
                        flag = false;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error: Number is in the wrong format");
                } catch (IOException e) {
                    System.err.println("Error: could not read input properly");
                }
                if (n < 0) {
                    System.out.println("Error: Value greater than 0 required");

                }
            }
            //prompt user for output file name
            String outputFile = "";
            System.out.println("Enter the output file name: ");
            PrintWriter out = null;
            while (out == null) {
                try {
                    outputFile = in.readLine();
                    out = new PrintWriter(
                            new BufferedWriter(new FileWriter(outputFile)));
                } catch (IOException e) {
                    System.out.println("Invalid output file name");

                }

            }
            /*
             * print the file.
             */

            printAll(out, n, file, outputFile);

            /*
             * close the input and output file
             */
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("Error closing file");
            }

        }
    }
}
