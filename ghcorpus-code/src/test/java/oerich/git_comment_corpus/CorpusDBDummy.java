package oerich.git_comment_corpus;

import java.util.Map;
import java.util.Map.Entry;

public class CorpusDBDummy implements ICorpusDB {

	private Map<String, Integer> frequencies;

	public void addFrequencies(Map<String, Integer> wordFrequencies) {
		if (this.frequencies == null) {
			this.frequencies = wordFrequencies;
			return;
		}
		for (Entry<String, Integer> e : wordFrequencies.entrySet()) {
			Integer f = this.frequencies.get(e.getKey());
			if (f == null)
				this.frequencies.put(e.getKey(), e.getValue());
			else {
				this.frequencies.put(e.getKey(), f + e.getValue());
			}
		}
	}

	public String[] getWords() {
		return this.frequencies.keySet().toArray(new String[0]);
	}

	public int getFrequency(String word) {
		return this.frequencies.get(word);
	}

	public boolean contains(String word) {
		return this.frequencies.containsKey(word);
	}

	public double getRelativeFrequency(String word) {
		int maxFreq = 0;
		
		for (int i : frequencies.values())
			if (i > maxFreq)
				maxFreq = i;
		
		return (double) getFrequency(word) / (double) maxFreq;
	}

}
