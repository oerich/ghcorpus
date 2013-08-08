package oerich.git_comment_corpus;

import java.util.Map;

public class CorpusAnalyzer implements ICorpusDB {

	private ICorpusDB corpus;
	private ICorpusDB[] corpora;

	public void addFrequencies(Map<String, Integer> wordFrequencies) {

	}

	public int getFrequency(String word) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getRelativeFrequency(String word) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean contains(String word) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCorpus(ICorpusDB corpus) {
		this.corpus = corpus;
	}

	public void setCorpora(ICorpusDB[] corpora) {
		this.corpora = corpora;
	}

	public double getTermFrequency(String word) {
		return this.corpus.getRelativeFrequency(word);
	}

	public double getInverseDocumentFrequency(String word) {
		// we can get the number of projects that have issue comments with this
		// word with the following query:
		// db.issue_comments.aggregate({$match : {body : /the/i }}, {$group : {_id: "$repo"}}).result.length
		
		// The total number of projects can be retrieved with the following query:
		// db.issue_comments.aggregate({$group : {_id: "$repo"}}).result.length
		
		// idf = log ((total number) / (number of projects with term))
		return -1;
	}
}
