import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;

/**
 * Tag Cloud Generator.
 *
 * @author Noah Bennett
 */
public final class TagCloudGenerator {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
    }

    /**
     * String of separators.
     */
    private static String separatorsStr = "/\t\n\r.,&!? []{}|-=+@#$%*\"()";

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
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            return o2.value().compareTo(o1.value());
        }
    }

    /**
     * Comparator StrOrder. Compares pairs of strings and integers and sorts
     * them by the string value.
     */
    private static class StrOrder
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            return o1.key().compareTo(o2.key());
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
     *
     * @param n
     * @param out
     * @param file
     */
    private static void printHeader(int n, SimpleWriter out, String file) {
        assert out.isOpen() : "Violation of: Output stream is open";
        assert !file.isEmpty() : "Violation of: file is not null";
        assert n > 0 : "Violation of: n > 0";

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
     *
     * @param fontSize
     * @param count
     * @param word
     * @param out
     */
    private static void printTags(int fontSize, int count, String word,
            SimpleWriter out) {
        assert out.isOpen() : "Violation of: Output stream is open";
        assert !word.isEmpty() : "Violation of: file is not null";
        assert count > 0 : "Violation of: count > 0";
        assert fontSize > 0 : "Violation of: size > 0";

        out.println("        " + "<span style=\"cursor:default\" class=\"f"
                + fontSize + "\" title=\"count: " + count + "\">" + word
                + "</span>");
    }

    /**
     *
     * @param out
     */
    private static void printFooter(SimpleWriter out) {
        assert out.isOpen() : "Violation of: Output stream is open";

        out.println("      </p>");
        out.println("    </div>");
        out.println("  </body>");
        out.println("</html>");
    }

    /**
     * Calculates the size of the word that is being put in the tagcloud based
     * on the amount of times it occurs in the input file.
     *
     * @param value
     * @param n
     * @param maxword
     * @return calcSize
     */
    private static int calcSize(int n, int value, int maxword) {
        assert value > 0 : "Violation of: value is greater than 0";
        assert maxword > 0 : "Violation of: maxword is greater than 0";

        //create a value within the range of values based on the size of the
        //most common word
        int increment = (maxword / SIZES) + 1;
        int size = MINSIZE;
        int i = 1;
        boolean sized = false;

        //give the word a size
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
     * Adds pairs of all words and their counts to a sorting machine that sorts
     * them by the amount of times each word shows up.
     *
     * @param file
     *            The file which is to be read for all of the words and their
     *            counts.
     * @param s
     *            The sorting machine which sorts the pairs by the word count.
     * @requires The file string isn't empty and the sorting machine is in
     *           insertion mode.
     * @ensures Sorting machine s is filled with pairs of all the words and
     *          their counts
     */
    private static void addToIntMachine(String file,
            SortingMachine<Map.Pair<String, Integer>> s) {
        assert file.length() > 0 : "Violation of file is not empty";
        assert s.isInInsertionMode() : "Violation of s is in insertion mode";

        //create the map for words and their counts and a simple reader reading
        //the file with the text
        Map<String, Integer> words = new Map1L<>();
        SimpleReader in = new SimpleReader1L(file);

        //create a set with separators
        Set<Character> separators = new Set1L<>();
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
                if (words.hasKey(word)) {
                    words.replaceValue(word, words.value(word) + 1);
                } else if (!separators.contains(word.charAt(0))) {
                    words.add(word, 1);
                }
                position += word.length();
            }
        }

        //while the map isn't empty, add the pairs to the integer sorting machine
        while (words.size() > 0) {
            Map.Pair<String, Integer> p = words.removeAny();
            s.add(p);
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
     *            the sorting machine with all pairs of words and word counts in
     *            sorted order.
     * @param sStrs
     *            the sorting machine which is to have all of the top n words
     * @return max the largest word count
     * @requires sInts is not empty and sStrs is empty. sInts is not in
     *           insertion mode and sStrs is.
     */
    private static int addToStringMachineReturnMax(int n,
            SortingMachine<Map.Pair<String, Integer>> sInts,
            SortingMachine<Map.Pair<String, Integer>> sStrs) {
        assert sInts.size() > 0 : "Violation of sInts is not empty";
        assert !sInts.isInInsertionMode() : "Violation of sInts in extraction";
        assert sStrs.size() == 0 : "Violation of sStrings is empty";
        assert sStrs.isInInsertionMode() : "Violation of sStrs in insertion";

        int max = 0;
        Map.Pair<String, Integer> p = sInts.removeFirst();
        max = p.value();

        for (int i = 0; i < n; i++) {
            sStrs.add(p);
            p = sInts.removeFirst();

        }

        return max;
    }

    /**
     * Calls all of the methods to print the main html file.
     *
     * @param n
     * @param file
     * @param outFile
     */
    private static void printAll(int n, String file, String outFile) {
        assert !file.isEmpty() : "Violation of: file is not null";
        assert !outFile.isEmpty() : "Violation of: outFile is not null";

        SimpleWriter out = new SimpleWriter1L(outFile);

        Comparator<Map.Pair<String, Integer>> intOrder = new IntOrder();
        SortingMachine<Map.Pair<String, Integer>> sortedInts;
        sortedInts = new SortingMachine1L<Map.Pair<String, Integer>>(intOrder);
        addToIntMachine(file, sortedInts);

        Comparator<Map.Pair<String, Integer>> strOrder = new StrOrder();
        SortingMachine<Map.Pair<String, Integer>> sortedStrs;
        sortedStrs = new SortingMachine1L<Map.Pair<String, Integer>>(strOrder);

        printHeader(n, out, file);

        int maxword = 0;
        sortedInts.changeToExtractionMode();
        if (sortedInts.size() > 0) {
            maxword = addToStringMachineReturnMax(n, sortedInts, sortedStrs);
        }

        for (int i = 0; i < n; i++) {
            if (sortedStrs.size() > 0) {
                if (sortedStrs.isInInsertionMode()) {
                    sortedStrs.changeToExtractionMode();
                }
                Map.Pair<String, Integer> pStr = sortedStrs.removeFirst();
                int size = calcSize(n, pStr.value(), maxword);
                printTags(size, pStr.value(), pStr.key(), out);
            }
        }

        printFooter(out);
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        SimpleReader in = new SimpleReader1L();

        //prompt the user for a file name and location if needed
        out.println("Input the location/file name:");
        String file = in.nextLine();

        //prompt user for a tag cloud size, if it is invalid reprompt them
        out.println("How many words would you like in the tag cloud:");
        int n = in.nextInteger();
        while (!(n > 0)) {
            out.println("Invalid word number. Input a new number: ");
            n = in.nextInteger();
        }

        //prompt the user for output file location and name
        out.println("Input the output location/file name:");
        String outFile = in.nextLine();

        //print the file
        printAll(n, file, outFile);

        //close input and output streams
        in.close();
        out.close();
    }

}
