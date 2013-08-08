package oerich.git_comment_corpus;

import java.util.Map;

public interface ICorpusDB {

	public void addFrequencies(Map<String, Integer> wordFrequencies);

	public int getFrequency(String word);

	/**
	 * Get the frequency for this word over the frequency of the word with the
	 * highest frequency in this corpus.
	 * 
	 * @param word
	 * @return
	 */
	public double getRelativeFrequency(String word);

	public boolean contains(String word);
}
