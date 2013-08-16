package scripts;

import io.DocumentHandler;
import io.DocumentProvider;
import io.rtc.RTCWorkitemCommentProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import oerich.nlputils.NLPProperties;
import oerich.nlputils.text.StopWordFilterFactory;
import oerich.nlputils.tokenize.ITokenizer;

public class ComputeDocumentFrequencies implements DocumentHandler<String> {

	private String word;
	private int df;
	// private Pattern pattern;
	private ITokenizer tokenizer = StopWordFilterFactory.createTokenizer();

	public void handle(String d) {
		if (df == -1)
			df = 0;
		// Alternitive I: regex. Faster, but no stopsign filtering, not
		// consistent to the classifier.
		// Matcher matcher = pattern.matcher(d.getText());
		// if (matcher.find())
		// df++;

		// Alternative II: Homemade.
		for (String w : this.tokenizer.tokenize(d)) {
			if (this.word.equals(w)) {
				this.df++;
				return;
			}
		}
	}

	/**
	 * Initializes with this word. Now, it needs to be run over a
	 * DocumentProvider to count the appearances.
	 * 
	 * @param word
	 */
	public void setWord(String word) {
		this.word = word;
		// pattern = Pattern.compile("\\b" + this.word + "\\b",
		// Pattern.CASE_INSENSITIVE);
		this.df = -1;
	}

	/**
	 * The document frequency.
	 * 
	 * @return -1 if no documents where investigated yet, otherwise the number
	 *         of documents that contain this string.
	 */
	public int getDF() {
		return this.df;
	}

	public static void main(String[] args) throws Exception {
		// File stopsigns = new File("stopsigns.txt");
		// File stopwords = new File("stopwords.txt");
		long time = System.currentTimeMillis();
		System.out.println("Computing document frequencies...");

		NLPProperties.getInstance().setResourcePath("/");
		// NLPProperties.getInstance().setStopsignsFileName(stopsigns.getAbsolutePath());
		// NLPProperties.getInstance().setStopsignsFileName(stopwords.getAbsolutePath());
		ComputeDocumentFrequencies inClassDFHandler = new ComputeDocumentFrequencies();
		ComputeDocumentFrequencies ninClassDFHandler = new ComputeDocumentFrequencies();

		// 1. Get a DocumentHandler for RTC project
		// XXX do I need to distinguish between classifications?
		String query = "SELECT id, content"
				+ "  FROM workitemcomment,"
				+ "    (SELECT workitemcommentid,classification FROM commentclassification"
				// All of gpoo's comments
				+ "        WHERE (workitemcommentid IN (SELECT workitemcommentid FROM commentclassification WHERE classifiedby = 'gpoo') AND classifiedby = 'gpoo')"
				// And in addition the few comments from 'eric1' that were not
				// classified by 'gpoo'
				+ "           OR (workitemcommentid IN (SELECT workitemcommentid FROM commentclassification WHERE classifiedby = 'eric1' AND workitemcommentid NOT IN (SELECT workitemcommentid FROM commentclassification WHERE classifiedby = 'gpoo')) AND classifiedby = 'eric1')) classification"
				+ "  WHERE id = workitemcommentid"
		// + "  AND NOT classification LIKE 'clari%'"
		;
		
		String inClassCondition = "  AND classification LIKE 'clari%'";
		String ninClassCondition = "  AND NOT classification LIKE 'clari%'";

		DocumentProvider<String> inClassDocumentProvider = new RTCWorkitemCommentProvider();
		inClassDocumentProvider.init(query + inClassCondition);
		DocumentProvider<String> ninClassDocumentProvider = new RTCWorkitemCommentProvider();
		ninClassDocumentProvider.init(query + ninClassCondition);

		// 2. For each word we are interested in
		String inFileName = "hybrid-classifier.txt";
		String outFileName = "classifier-db-with-df_rtc.csv";
		BufferedReader r = new BufferedReader(new FileReader(
				inFileName));
		File out = new File(outFileName);
		out.delete();
		out.createNewFile();
		BufferedWriter w = new BufferedWriter(new FileWriter(out));

		System.out.println(" - reading words from: " + inFileName);
		System.out.println(" - writing dfs to: " + outFileName);
		
		
		// the first two lines contain meta information
		String line = r.readLine();
		w.write(line);
		w.newLine();
		line = r.readLine();
		w.write(line);
		w.newLine();

		// now get the first line with data
		line = r.readLine();
		int i = 0;
		while (line != null) {
			String word = line.split(";")[0];
			// 2.1 Create a handler
			inClassDFHandler.setWord(word);
			ninClassDFHandler.setWord(word);

			// 2.2 Iterate over all documents
			inClassDocumentProvider.iterate(inClassDFHandler, null);
			ninClassDocumentProvider.iterate(ninClassDFHandler, null);

			// 2.3 write the frequency to file
			// System.out.println(word + "\t" + dfHandler.getDF());
			w.write(line + ';' + inClassDFHandler.getDF() + ';' + ninClassDFHandler.getDF());
			w.newLine();
			line = r.readLine();
			
			i++;
			if (i % 1000 == 0)
				System.out.println(" - Processed " + i + " words in " + (System.currentTimeMillis() - time)/1000 + " sec.");
		}

		// 3. Clean up
		inClassDocumentProvider.close();
		r.close();
		w.flush();
		w.close();
		System.out.println("Done.\n - Processed " + i + " words in " + (System.currentTimeMillis() - time)/1000 + " sec.");
	}

	public String getWord() {
		return this.word;
	}
}
