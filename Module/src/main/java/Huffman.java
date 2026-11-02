import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Project 3: Huffman Encoding / Decoding.
 *
 * Implement the methods below to compress and decompress text files using
 * Huffman coding. See the assignment write-up for the full specification.
 *
 *  - Treat characters as 8-bit values in the range [0, 255].
 *  - When writing output files, write the newline character explicitly as
 *    '\n' (do not rely on System.lineSeparator() / println()), so your
 *    output is identical across Windows/Mac/Linux. A BufferedWriter over
 *    a FileWriter, writing "\n" yourself, works well for this.
 *  - Encoded files store the code as literal '0'/'1' characters (one byte
 *    each), not as packed bits, so the "encoded" file will actually be
 *    larger than the original -- that's expected for this assignment.
 *  - createHuffTree() is built on top of addNode()/removeMin(), which
 *    operate generically over a java.util.Collection<HuffmanNode>.
 *  - HuffmanNode is a record, so its components are read with
 *    node.charAscii(), node.freq(), node.left(), and node.right() -- no
 *    "get" prefix.
 */
public class Huffman {

    /**
     * Number of possible 8-bit character values.
     */
    public static final int NUM_CHARS = 256;

    /**
     * Opens the text file named filename and counts the frequency of
     * occurrence of every character in it.
     * Returns an int array of size NUM_CHARS, indexed by ASCII value, with
     * the count of each character (0 for characters that do not occur).
     */
    public static int[] cntFreq(String filename) throws IOException {
        int[] freq = new int[NUM_CHARS];
        try {
            FileReader fileReader = new FileReader(filename);
            int i;
            while ((i = fileReader.read()) != -1) {
                freq[i] += 1;
            }
            fileReader.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return freq;
    }

    /**
     * Adds node to collection, preserving whatever ordering your chosen
     * implementation of collection relies on to make the minimum element
     * easy to find.
     * <p>
     * You choose what kind of Collection to pass in when you call this
     * from createHuffTree(). For example, an ArrayList kept sorted by
     * HuffmanNode.comesBefore() as you insert (an "ordered list"), or a
     * PriorityQueue. How you implement this method depends on which you pick.
     */
    public static void addNode(Collection<HuffmanNode> collection, HuffmanNode node) {
        Collection<HuffmanNode> lst = new ArrayList<>();
        boolean added = false;
        for (HuffmanNode current_node : collection) {
            if (!added && HuffmanNode.comesBefore(node, current_node)) {
                lst.add(node);
                added = true;
            }
            lst.add(current_node);
        }
        if (!added) {
            lst.add(node);
        }
        collection.clear();
        collection.addAll(lst);
    }


    /**
     * Removes and returns the smallest HuffmanNode in collection, using
     * HuffmanNode.comesBefore() (or compareTo(), which relies on it) to
     * determine "smallest". Returns null if collection is empty.
     * <p>
     * This must work for ANY Collection passed in -- not just the one you
     * use inside createHuffTree(). For example, if you back createHuffTree()
     * with a PriorityQueue, poll() it; for any other collection, find the
     * minimum node and remove it.
     */
    public static HuffmanNode removeMin(Collection<HuffmanNode> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        } else if (collection instanceof PriorityQueue) {
            PriorityQueue<HuffmanNode> queue = (PriorityQueue<HuffmanNode>) collection;
            return queue.poll();
        } else {
            HuffmanNode min = null;
            for (HuffmanNode node : collection) {
                if (min == null || HuffmanNode.comesBefore(node, min)) {
                    min = node;
                }
            }
            collection.remove(min);
            return min;
        }
    }


    /**
     * Builds a Huffman tree from an array of character frequencies (as
     * produced by cntFreq()) and returns the root HuffmanNode of the tree.
     * <p>
     * Use addNode()/removeMin() above to manage the collection of
     * not-yet-combined nodes as you build the tree -- pick whichever
     * concrete Collection type you want to back them with (e.g. a sorted
     * ArrayList, or a PriorityQueue). The rest of this method, and the
     * resulting tree, should not depend on which one you chose.
     * <p>
     * Special cases:
     * - If freq contains no non-zero counts (empty input file), return null
     * - If freq contains exactly one non-zero count (input file has only
     * one unique character), return a single leaf HuffmanNode for that
     * character (no combine() call needed).
     */
    public static HuffmanNode createHuffTree(int[] freq) {
        Collection<HuffmanNode> lst = new ArrayList<>();
        for (int i = 0; i < freq.length; i += 1) {
            if (freq[i] > 0) {
                HuffmanNode node = new HuffmanNode(i, freq[i], null, null);
                addNode(lst, node);
            }
        }
        while (lst.size() > 1) {
            HuffmanNode node1 = removeMin(lst);
            HuffmanNode node2 = removeMin(lst);
            addNode(lst, HuffmanNode.combine(node1, node2));
        }
        return removeMin(lst);
    }

    /**
     * Traverses the Huffman tree rooted at root and builds the Huffman
     * code for every character that appears in it. Going left appends
     * '0' to the code, going right appends '1'.
     * <p>
     * Returns a String array of size NUM_CHARS, indexed by ASCII value.
     * Characters that do not appear in the tree have an empty string ""
     * at their index. The same is true of the lone character in a
     * single-node tree (the only-one-unique-character case) - it has no
     * left/right branch to build a code out of; huffmanEncode/huffmanDecode
     * handle that case entirely through the header.
     */
    public static String[] createCode(HuffmanNode root) {
        String[] codes = new String[NUM_CHARS];
        Arrays.fill(codes, "");
        pathCodes(root, "", codes);
        return codes;
    }

    public static String[] pathCodes(HuffmanNode root, String path, String[] codes) {
        if (root == null) {
            return null;
        }
        if (root.left() == null && root.right() == null) {
            codes[root.charAscii()] = path;
        }
        pathCodes(root.left(), path + "0", codes);
        pathCodes(root.right(), path + "1", codes);
        return codes;
    }



    /**
     * Given a frequency array (as produced by cntFreq()), returns a
     * header string listing the ASCII values and their associated
     * frequencies, separated by single spaces, in ascending order of
     * ASCII value. Only characters with a non-zero count are included.
     *
     * Example: for the text "aaabbbbcc" this returns "97 3 98 4 99 2".
     */
    public static String createHeader(int[] freq) {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < freq.length; i += 1) {
            if (freq[i] > 0) {
                header.append(i).append(" ").append(freq[i]).append(" ");
            }
        }
        if (header.length() > 0) {
            int end = header.length() - 1;
            header.deleteCharAt(end);
        }
        return header.toString();
    }


    /**
     * Reads the input text file inFile and writes to outFile:
     *   1. A header line (see createHeader), followed by a newline
     *   2. The Huffman-encoded contents of inFile, using the codes from
     *      createCode() -- UNLESS inFile has only one unique character,
     *      see below.
     * If outFile already exists, its old contents are overwritten.
     *
     * Special cases:
     *  - Empty input file: outFile should end up empty too
     *  - Input file with only one unique character: should contain only the
     *    header line (e.g. "97 5") and nothing else - no second line of code bits.
     */
    public static void huffmanEncode(String inFile, String outFile) throws IOException {
        int[] freq_table = cntFreq(inFile);
        HuffmanNode root = createHuffTree(freq_table);
        if (root == null){
            // write an empty file
        }
        String [] codes = createCode(root);
        String header = createHeader(freq_table);
        outFile = header + "\n" + codes;

    }

    /**
     * Parses a header string (in the format produced by createHeader) and
     * returns the corresponding frequency array of size NUM_CHARS, indexed
     * by ASCII value, i.e. the same format returned by cntFreq().
     *
     * Example: for the header "97 3 98 4 99 2", freq[97, 98, 99] == {3, 4, 2}.
     */
    public static int[] parseHeader(String headerString) {
        int[] freq = new int[NUM_CHARS];
        if (headerString == null){
            return freq;
        }
        String[] header_array = headerString.split(" ");
        for (int i = 0; i < header_array.length; i += 2){
            int ascii = Integer.parseInt(header_array[i]);
            int frequency = Integer.parseInt(header_array[i+1]);
            freq[ascii] = frequency;
        }
        return freq;
    }

    /**
     * Reads an encoded file (in the format written by huffmanEncode) and
     * writes the decoded text to decodeFile.
     *
     * Recreate the frequency array from the header with parseHeader(),
     * rebuild the Huffman tree with createHuffTree(), then walk the tree
     * for each bit of the second line: '0' -> left child, '1' -> right
     * child. When a leaf is reached, emit its character and restart at
     * the root.
     *
     * Special case: if the recreated tree is a single leaf (the
     * only-one-unique-character case), there is no second line to read
     * The header's count alone tells you how many times that character occurred,
     * so write it out that many times directly.
     */
    public static void huffmanDecode(String encodedFile, String decodeFile) throws IOException {
        Path path = Path.of(encodedFile);
        String encoded = Files.readString(path);
        String[] lines = encoded.split("\n"); // breaks up header, lines

        int[] header = parseHeader(lines[0]); // following lines create the codes
        HuffmanNode tree = createHuffTree(header);
        String[] codes = createCode(tree);
        StringBuilder string = new StringBuilder(); // string that is being read
        FileWriter file = new FileWriter(decodeFile); // file being written to
        for (int i = 1; i < lines.length; i += 1) { // excludes header, puts rest of file back together
            string.append(lines[i]);
        }

        StringBuilder code = new StringBuilder(); // decoding
        HuffmanNode spot = tree;
        for (int i = 0; i < string.length(); i += 1) {
            if (spot.left() == null || spot.right() == null) {
                try {
                    String chr = "";
                    for (i = 0; i < codes.length; i += 1) {
                        if (code.equals(codes[i])) {
                            chr = String.valueOf((char) i);
                        }
                    }
                    file.write(chr);
                    code = new StringBuilder();
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e.getMessage());
                }
                if (string.charAt(i) == 0) {
                    spot = spot.left();
                    code.append(0);
                } else {
                    spot = spot.right();
                    code.append(1);
                }
            }
            // TODO: Decoding
            /** files need to be in chunks of 8;
             * we can just decode the length of the original string
             * and then add zeros after */
        }
    }
}
