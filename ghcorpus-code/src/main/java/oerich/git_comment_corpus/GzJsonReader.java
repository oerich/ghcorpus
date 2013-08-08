package oerich.git_comment_corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GzJsonReader {

	private ICorpusDB corpus;
	private IWordCount counter;
	private boolean cancelled;
	private boolean running;
	private JSONParser parser;

	public ICorpusDB read(InputStream stream) throws IOException,
			ParseException {
		running = false;

		InputStream gzipStream = new GZIPInputStream(stream);
		Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader reader = new BufferedReader(decoder);

		String line = reader.readLine();
		while (line != null && !cancelled) {
			if (line.contains("IssueCommentEvent") && line.contains("\"body\""))
				System.out.println(line);
			this.corpus.addFrequencies(this.counter
					.getWordFrequencies((JSONObject) getParser().parse(line)));
			line = reader.readLine();
		}
		running = false;
		return this.corpus;
	}

	public ICorpusDB getCorpus() {
		return corpus;
	}

	public void setCorpus(ICorpusDB corpus) {
		this.corpus = corpus;
	}

	public IWordCount getCounter() {
		return counter;
	}

	public void setCounter(IWordCount counter) {
		this.counter = counter;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	private JSONParser getParser() {
		if (this.parser == null)
			this.parser = new JSONParser();
		return this.parser;
	}

}
