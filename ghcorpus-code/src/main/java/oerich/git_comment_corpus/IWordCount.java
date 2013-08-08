package oerich.git_comment_corpus;

import java.util.Map;

import org.json.simple.JSONObject;

import com.mongodb.DBObject;

public interface IWordCount {

	public Map<String, Integer> getWordFrequencies(JSONObject object);

	public Map<String, Integer> getWordFrequencies(DBObject object);

}
