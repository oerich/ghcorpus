package scripts;

import io.DocumentHandler;
import io.DocumentProvider;
import io.ghtorrent.GHTorrentProjectProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import oerich.nlputils.NLPProperties;
import oerich.nlputils.text.StopWordFilterFactory;
import oerich.nlputils.tokenize.ITokenizer;

public class ComputeGithubInversProjectFrequency implements
		DocumentHandler<String[]> {

	private String word;
	private int n;
	private int df;
	private ITokenizer tokenizer = StopWordFilterFactory.createTokenizer();

	public void handle(String[] d) {
		for (String comment : d) {
			for (String w : this.tokenizer.tokenize(comment)) {
				if (this.word.equals(w)) {
					this.df++;
					return;
				}
			}
		}
	}

	public int getDf() {
		return df;
	}

	public void setDf(int df) {
		this.df = df;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public void setWord(String word) {
		this.n = 0;
		this.df = 0;
		this.word = word;
	}

	public double getIDF() {
		if (this.n == 0)
			return -1;

		return Math.log10((double) this.n / (1 + (double) this.df));
	}

	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		System.out
				.println("Computing inverse project frequencies (ghtorrent)...");

		NLPProperties.getInstance().setResourcePath("/");

		ComputeGithubInversProjectFrequency ipf = new ComputeGithubInversProjectFrequency();

		// 1. Get a DocumentHandler for GHTorrent
		DocumentProvider<String[]> ghRepoProvider = new GHTorrentProjectProvider();
		ghRepoProvider.init(null);

		// 2. For each word we are interested in
		String inFileName = "classifier-db-with-df_rtc.csv";
		String outFileName = "classifier-db-with-df_rtc-idf_gh.csv";
		BufferedReader r = new BufferedReader(new FileReader(inFileName));
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
			ipf.setWord(word);

			// 2.2 Iterate over all documents
			ghRepoProvider.iterate(ipf, null);

			// 2.3 write the frequency to file
			// System.out.println(word + "\t" + dfHandler.getDF());
			w.write(line + ';' + ipf.getIDF());
			w.newLine();
			w.flush();
			line = r.readLine();

			i++;
			if (i % 1000 == 0)
				System.out.println(" - Processed " + i + " words in "
						+ (System.currentTimeMillis() - time) / 1000 + " sec.");
		}

		// 3. Clean up
		ghRepoProvider.close();
		r.close();
		w.close();
		System.out.println("Done.\n - Processed " + i + " words in "
				+ (System.currentTimeMillis() - time) / 1000 + " sec.");
	}

	public String getWord() {
		return this.word;
	}
}
