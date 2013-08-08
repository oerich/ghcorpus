package oerich.git_comment_corpus;

import java.util.HashMap;
import java.util.Map;

import oerich.nlputils.text.StopWordFilterFactory;
import oerich.nlputils.tokenize.ITokenizer;

import org.json.simple.JSONObject;

import com.mongodb.DBObject;

public class WordCountDummy implements IWordCount {

	public Map<String, Integer> getWordFrequencies(JSONObject object) {
		if ("PullRequestEvent".equals(object.get("type"))) {
			JSONObject payload = (JSONObject) object.get("payload");
			JSONObject pullRequest = (JSONObject) payload.get("pull_request");
			// System.out.println(pullRequest.get("body"));
		}

		return null;
	}

	public Map<String, Integer> getWordFrequencies(DBObject object) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		
		Object body = object.get("body");
		ITokenizer tokenizer = StopWordFilterFactory.createTokenizer();
		String[] words = tokenizer.tokenize(body.toString());
		
		for (String w : words) {
			Integer i = ret.get(w);
			if (i == null)
				i = 1;
			else 
				i++;
			ret.put(w, i);
		}
		
		return ret;
	}

}
