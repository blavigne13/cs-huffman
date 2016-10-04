/**
 * Accepts input (first argument) and output (second argument) file names from
 * the command line, then decompresses the input file, and saves the
 * decompressed result in the output file.
 * 
 * @author lavigb23
 * @version 11/18/2013
 */
public class Enbigify {
	public static void main(String[] args) {
		// ensure two arguments were supplied
		if (args.length < 2) {
			System.out.println("Usage:");
			System.out.println("Enbigify <input file> <output file>");
		} else {
			String input = args[0];
			String output = args[1];
			DecodeHuffman decoder;
			try {
				decoder = new DecodeHuffman(input, output);
				System.out.println("Decoding " + input);
				decoder.decompress();
				System.out.println("...Saved as " + output);
			} catch (Exception e) {
				System.out.println("Unable to complete operation:");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}