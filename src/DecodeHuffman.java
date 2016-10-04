import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Arrays;

/**
 * Decodes a Huffman encoded file created by the EncodeHuffman class. Accepts
 * input and output file names in constructor.
 * 
 * @author lavigb23
 * @version 11/18/2013
 */
public class DecodeHuffman {
	// simple node class
	private class Node {
		char value = 0;
		Node left = null;
		Node right = null;
	}

	private BufferedInputStream in;
	private PrintWriter out;
	private Node charTree;
	private int mapSize, blockSize;
	private String outputFileName;

	byte[] currentHash = new byte[16];

	public DecodeHuffman(String input, String output)
			throws FileNotFoundException, UnsupportedEncodingException {
		outputFileName = output;
		blockSize = 8;
		in = new BufferedInputStream(new FileInputStream(input));
		out = new PrintWriter(output);
		charTree = null;
		mapSize = 0;
	}

	/**
	 * Creates a Huffman decoder using the specified input and output files.
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void decompress() throws IOException, NoSuchAlgorithmException {
		readHash();
		readTree();
		writeData();
		out.close();
		getHash();
		in.close();

	}

	// recover hash of original file
	private void readHash() throws IOException {
		// read first 16 bytes (128-bit MD5)
		for (int i = 0; i < 16; i++) {
			currentHash[i] = (byte) in.read();
		}
	}

	// generate MD5 hash of original file
	private void getHash() throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		InputStream is = Files.newInputStream(Paths.get(outputFileName));
		DigestInputStream dis = new DigestInputStream(is, md);
		System.out.print("...Testing file integrity (MD5 hash)...");
		// read contents of file into digest stream
		while (dis.read() != -1) {
		}
		byte[] originalHash = md.digest();
		if (Arrays.equals(currentHash, originalHash)) {
			System.out.println("PASSED");
		} else {
			System.out.println("FAILED");
		}
	}

	// recover huffman tree from input file
	private void readTree() throws IOException {
		// get number of leaves in tree
		mapSize = (int) in.read();
		charTree = new Node();
		// assign root first value read from input file
		charTree.value = (char) in.read();
		// values of 0 are not leaves
		if (charTree.value != 0) {
			mapSize--;
		}
		readTree_r(charTree);
	}

	private void readTree_r(Node current) throws IOException {
		// are there still leaves to be read in?
		if (mapSize > 0) {
			// is this node NOT a leaf?
			if (current.value == 0) {
				// add a left child
				if (current.left == null) {
					Node temp = new Node();
					temp.value = (char) in.read();
					current.left = temp;
					readTree_r(temp);
				}
				// add a right child
				if (current.right == null) {
					Node temp = new Node();
					temp.value = (char) in.read();
					current.right = temp;
					readTree_r(temp);
				}
			} else {
				// if it IS a leaf, decrement mapSize
				mapSize--;
			}
		}
	}

	// decode data and write to output file
	private void writeData() throws IOException {
		int code = 0;
		// start at root of huffman tree
		Node temp = charTree;
		// read input file byte by byte
		while ((code = in.read()) != -1) {
			// navigate tree bit by bit
			for (int i = 0; i < blockSize; i++) {
				// are we at a leaf?
				if (temp.value == 0) {
					// if not a leaf continue navigation
					if (code % 2 == 0) {
						// go left if 0
						temp = temp.left;
					} else {
						// go right if 1
						temp = temp.right;
					}
					// shift next bit into 1's place
					code = (code >> 1);
				} else if (temp.value != 3) {
					// node value is not 0 (null char) or <End of Text>
					out.write(temp.value);
					// reset temp to root node
					temp = charTree;
					// counteract i++ since we did not bit shift
					i--;
				}
			}
		}
	}
}