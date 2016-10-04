import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/**
 * Encodes a file using Huffman encoding. Accepts input and output file names in
 * constructor.
 * 
 * @author lavigb23
 * @version 11/18/2013
 */
public class EncodeHuffman {
	// simple node class
	private class Node {
		char value = 0;
		int freq = 0;
		Node left = null;
		Node right = null;
	}

	// node comparator for the priority queue
	private class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node a, Node b) {
			if (a.freq < b.freq) {
				return -1;
			} else if (a.freq > b.freq) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private String inputFileName;
	private int blockSize;
	private int[] charFreq;
	private String[] charCode;
	private Comparator<Node> comparator;
	private PriorityQueue<Node> priQ;
	private BufferedReader in;
	private FileOutputStream out;

	/**
	 * Creates a Huffman encoder using the specified input and output files.
	 * 
	 * @param input
	 *            the file to be compressed
	 * @param output
	 *            compressed data file
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public EncodeHuffman(String input, String output)
			throws FileNotFoundException, UnsupportedEncodingException {
		// use 8-bit chars
		blockSize = 8;
		charFreq = new int[256];
		charCode = new String[256];
		inputFileName = input;
		in = new BufferedReader(new FileReader(inputFileName));
		out = new FileOutputStream(output);
		comparator = new NodeComparator();
		priQ = new PriorityQueue<Node>(1, comparator);
	}

	/**
	 * Initiates the encoding process.
	 * 
	 * @throws Exception
	 */
	public void compress() throws Exception {
		getHash();
		charCount();
		in.close();
		generateTree();
		writeTree();
		writeData();
		in.close();
		out.close();
	}

	// generate MD5 hash of original file
	private void getHash() throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		InputStream is = Files.newInputStream(Paths.get(inputFileName));
		DigestInputStream dis = new DigestInputStream(is, md);
		// read contents of file into digest stream
		while (dis.read() != -1) {
		}
		byte[] currentHash = md.digest();
		// write 16-byte hash to output file
		for (int i = 0; i < 16; i++) {
			out.write(currentHash[i]);
		}
	}

	// count frequency for each 8-bit char
	private void charCount() throws Exception {
		int ch = 0;
		while ((ch = in.read()) != -1) {
			// is charCount[ch] about to overflow?
			if (charFreq[ch] == Integer.MAX_VALUE) {
				System.out.println("WARNING: count of " + String.valueOf(ch)
						+ " has overflowed!");
				System.out.println("...Compression may not be optimized.");
			}
			charFreq[ch]++;
		}
		// account for <End of Text> char inserted at end
		charFreq[3]++;
	}

	// create a tree based upon character frequency
	private void generateTree() throws IOException {
		Node temp = null;
		// parse frequency array
		for (int i = 0; i < charFreq.length; i++) {
			// add node for chars that occur in input file
			if (charFreq[i] > 0) {
				temp = new Node();
				temp.value = (char) i;
				temp.freq = charFreq[i];
				priQ.add(temp);
			}
		}
		// write number of leaves to beginning of output file
		out.write((byte) priQ.size());
		// create huffman tree based on frequency data
		while (priQ.size() > 1) {
			temp = new Node();
			temp.left = priQ.remove();
			temp.right = priQ.remove();
			temp.freq = temp.left.freq + temp.right.freq;
			priQ.add(temp);
		}
	}

	// write huffman tree nodes to file
	private void writeTree() throws IOException {
		Node charTree = priQ.remove();
		writeTree_r(charTree, "");
	}

	private void writeTree_r(Node temp, String code) throws IOException {
		// write char defined by this node
		out.write(temp.value);
		// code = string representation of tree path for each node
		// then store in array for reference when encoding input file
		charCode[(int) temp.value] = code;
		if (temp.left != null) {
			writeTree_r(temp.left, code + "0");
		}
		if (temp.left != null) {
			writeTree_r(temp.right, code + "1");
		}
	}

	// encode input chars and write bytes to file
	private void writeData() throws IOException {
		// reset in to beginning of input file
		in = new BufferedReader(new FileReader(inputFileName));
		String outBits = "";
		int ch = 0;
		// read input file one char at a time
		while ((ch = in.read()) != -1) {
			// add moar bits!
			outBits += charCode[ch];
			// are there are enough bits to encode yet?
			if (outBits.length() >= blockSize) {
				out.write(toBlock(outBits));
				// remove bits which were encoded
				outBits = outBits.substring(blockSize);
			}
		}
		// add <End of Text> char at end of file
		outBits += charCode[3];
		// loop in case adding <EOT> results in length > 8
		while (outBits.length() > 8) {
			out.write(toBlock(outBits));
			// remove bits which were encoded
			outBits = outBits.substring(blockSize);
		}
		// write last block if any bits remain
		if (outBits.length() > 0) {
			out.write(toBlock(outBits));
		}
	}

	// encode first blockSize bits from string representation
	private char toBlock(String s) {
		int b = 0;
		// encode bits from end of block down to 0
		for (int i = blockSize - 1; i >= 0; i--) {
			b = (b << 1);
			// short circuit to avoid index out of bounds
			if (i < s.length() && s.charAt(i) == '1') {
				b++;
			}
		}
		return (char) b;
	}
}