package oerich.git_comment_corpus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oerich.nlputils.text.StopWordFilterFactory;
import oerich.nlputils.tokenize.ITokenizer;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDBJsonReader {

	private static final DBObject GROUP_BY_REPO_FIELDS = new BasicDBObject(
			"_id", "$repo");
	private static final DBObject GROUP_BY_REPO = new BasicDBObject("$group",
			GROUP_BY_REPO_FIELDS);

	private ICorpusDB corpus = new MongoDBCorpus("total");
	private IWordCount counter = new MongoDBCounter();
	private Map<String, ICorpusDB> subcorpora;
	private Map<String, IWordCount> subcounter;
	private boolean cancelled;
	private boolean running;
	private MongoClient mongoClient;
	private DBCollection rawDataCollection;
	private DBCollection freqDataCollection;

	private void init() throws UnknownHostException {
		running = false;
		this.subcorpora = new HashMap<String, ICorpusDB>();
		this.subcounter = new HashMap<String, IWordCount>();

		for (String repo : getTopRepos()) {
			this.subcorpora.put(repo, new MongoDBCorpus(repo));
			this.subcounter.put(repo, new MongoDBCounter());
		}
	}

	private MongoClient getMongoClient() throws UnknownHostException {
		if (this.mongoClient == null)
			this.mongoClient = new MongoClient("localhost", 27017);
		return this.mongoClient;
	}

	private DBCollection getRawDataCollection() throws UnknownHostException {
		if (this.rawDataCollection == null) {
			DB rawDB = getMongoClient().getDB("ghtorrent");
			this.rawDataCollection = rawDB.getCollection("issue_comments");
		}
		return this.rawDataCollection;
	}

	private DBCollection getFreqDataCollection() throws UnknownHostException {
		if (this.freqDataCollection == null) {
			DB rawDB = getMongoClient().getDB("ghcorpus");
			this.freqDataCollection = rawDB.getCollection("frequencies");
		}
		return this.freqDataCollection;
	}

	public double getInverseDocumentFrequency(String word) {

		try {
			// we can get the number of projects that have issue comments with
			// this
			// word with the following query:
			// db.issue_comments.aggregate({$match : {body : /the/i }}, {$group
			// :
			// {_id: "$repo"}}).result.length
			BasicDBObject regex = new BasicDBObject("$regex", "\\b" + word + "\\b");
			regex.append("$options", "i");

			DBObject crit = new BasicDBObject("body", regex);
			DBObject match = new BasicDBObject("$match", crit);

			// XXX is there no way to get the length of the result in Java?
			int din = 0;
			for (Object o : getRawDataCollection().aggregate(match,
					GROUP_BY_REPO).results()) {
				din++;
			}

			// The total number of projects can be retrieved with the following
			// query:
			// db.issue_comments.aggregate({$group : {_id:
			// "$repo"}}).result.length
			int totalProjects = 0;
			for (Object o : getRawDataCollection().aggregate(GROUP_BY_REPO)
					.results()) {
				totalProjects++;
			}

			// System.out.println(word + " appears in " + din + "/" +
			// totalProjects + " projects");

			// Now compute the idf:
			// idf = log ((total number) / (number of projects with term))
			return Math.log((double) totalProjects / (double) din);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}

	String[] getTopRepos() throws UnknownHostException {
		DBObject groupFields = new BasicDBObject("_id", "$repo");
		groupFields.put("number", new BasicDBObject("$sum", 1));
		BasicDBObject group = new BasicDBObject("$group", groupFields);

		DBObject sortFields = new BasicDBObject("number", -1);
		BasicDBObject sort = new BasicDBObject("$sort", sortFields);

		BasicDBObject limit = new BasicDBObject("$limit", 10);

		AggregationOutput aggregate = getRawDataCollection().aggregate(group,
				sort, limit);
		System.out.println(aggregate);
		Iterable<DBObject> results = aggregate.results();
		List<String> tmp = new LinkedList<String>();
		for (DBObject dbo : results) {
			tmp.add((String) dbo.get("_id"));
		}
		return tmp.toArray(new String[0]);
	}

	public ICorpusDB read(String database) throws IOException, ParseException {
		return read(database, -1);
	}

	public ICorpusDB read(String database, int limit) throws IOException,
			ParseException {
		init();

		DBCursor cursor = getRawDataCollection().find();
		int i = 0;

		while (cursor.hasNext() && (limit == -1 || i < limit)) {
			DBObject next = cursor.next();
			Object repo = next.get("repo");
			this.corpus.addFrequencies(this.counter.getWordFrequencies(next));
			if (repo != null && this.subcorpora.containsKey((String) repo)) {
				// System.out.println(repo);
				this.subcorpora.get(repo).addFrequencies(
						this.subcounter.get(repo).getWordFrequencies(next));
			}

			if (limit != -1)
				i++;
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

	public class MongoDBCounter implements IWordCount {

		public Map<String, Integer> getWordFrequencies(JSONObject object) {
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

	public class MongoDBCorpus implements ICorpusDB {

		private String field;
		private BasicDBObject sortByField;

		public MongoDBCorpus(String field) {
			this.field = field;
			sortByField = new BasicDBObject(this.field, -1);
		}

		public void addFrequencies(Map<String, Integer> wordFrequencies) {
			DBObject query = new BasicDBObject();
			DBObject fieldAmount = new BasicDBObject();
			DBObject inc = new BasicDBObject("$inc", fieldAmount);

			for (Entry<String, Integer> e : wordFrequencies.entrySet()) {
				// We are not interested in some words:
				if (e.getKey().length() < 1024) {
					query.put("_id", e.getKey());
					fieldAmount.put(this.field, (int) e.getValue());
					// DBObject update = ;
					try {
						getFreqDataCollection().findAndModify(query, null, /* fields */
						null /* sort */, false, inc, true, true);
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					}
				}
			}

		}

		public int getFrequency(String word) {
			DBObject query = new BasicDBObject("_id", word);
			try {
				DBCursor c = getFreqDataCollection().find(query);
				if (c.hasNext())
					return (Integer) c.next().get(this.field);
				else
					return 0;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			return -1;
		}

		public boolean contains(String word) {
			return getFrequency(word) > 0;
		}

		public double getRelativeFrequency(String word) {
			// db.frequencies.find().sort({total:-1}).limit(5)
			int maxFreq = 0;

			// get the max frequency for this corpus:
			try {
				DBCursor c = getFreqDataCollection().find()
						.sort(sortByField).limit(1);
				if (c.hasNext()) {
					maxFreq = (Integer) c.next().get(field);
					return (double) getFrequency(word) / (double) maxFreq;
				}

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			return -1;
		}

	}

	public static void main(String[] args) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader("words.txt"));

		MongoDBJsonReader md = new MongoDBJsonReader();

		String line = r.readLine();
		while (line != null) {
			System.out.println(line + "\t"
					+ md.getInverseDocumentFrequency(line));
			line = r.readLine();
		}
		r.close();
	}
}
