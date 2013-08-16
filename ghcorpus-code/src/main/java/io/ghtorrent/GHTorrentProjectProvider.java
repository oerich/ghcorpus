package io.ghtorrent;

import io.DocumentHandler;
import io.DocumentProvider;

import java.util.LinkedList;
import java.util.List;

import scripts.ComputeGithubInversProjectFrequency;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class GHTorrentProjectProvider implements DocumentProvider<String[]> {

	private MongoClient mongoClient;
	private DBCollection rawDataCollection;
	private int n;

	public void init(String query) throws Exception {
		if (this.mongoClient == null) {
			this.mongoClient = new MongoClient("localhost", 27017);
			DB rawDB = this.mongoClient.getDB("ghtorrent");
			this.rawDataCollection = rawDB.getCollection("issue_comments");

			// compute N
			DBObject groupFields = new BasicDBObject("_id", "$repo");
			DBObject group = new BasicDBObject("$group", groupFields);
			this.n = 0;
			for (DBObject o : this.rawDataCollection.aggregate(group).results())
				n++;
		}

	}

	public void close() throws Exception {

	}

	public void iterate(DocumentHandler<String[]> handler, String query) {
		if (handler.getWord() == null || "".equals(handler.getWord()))
			return;

		// Some fields that we need.
		long time = System.currentTimeMillis();
		List<String> tmp = new LinkedList<String>();
		// we are only interested in the body (reduce data)
		DBObject projection = new BasicDBObject("body", 1);
		// mongo can preselect candidates (\\\b might make it even better)
		DBObject regex = new BasicDBObject("$regex", handler.getWord());
		regex.put("$options", "i");
		DBObject crit = new BasicDBObject("body", regex);
		DBObject match = new BasicDBObject("$match", crit);
		DBObject groupFields = new BasicDBObject("_id", "$repo");
		DBObject group = new BasicDBObject("$group", groupFields);

		// Lets do it
		System.out.print("{word: \t\"" + handler.getWord() + "\", ");
		if (handler instanceof ComputeGithubInversProjectFrequency) {
			((ComputeGithubInversProjectFrequency) handler).setN(this.n);
		}

		for (DBObject o : this.rawDataCollection.aggregate(match, group)
				.results()) {
			String repo = o.get("_id").toString();

			// db.issue_comments.find({repo : "LEPTON_2_BlackCat", body :
			// {$regex : "\\bwe\\b"}}, {body : 1})
			DBObject repoquery = new BasicDBObject("repo", repo);
			repoquery.put("body", regex);

			DBCursor cursor = this.rawDataCollection
					.find(repoquery, projection);

			while (cursor.hasNext()) {
				tmp.add(cursor.next().get("body").toString());

			}
			handler.handle(tmp.toArray(new String[0]));
			tmp.clear();

		}
		if (handler instanceof ComputeGithubInversProjectFrequency) {
			System.out.print(", df : "
					+ ((ComputeGithubInversProjectFrequency) handler).getDf());
			;
		}
		System.out.println(", "
				+ timemillisToString(System.currentTimeMillis() - time) + "}");
	}

	private String timemillisToString(long milis) {
		long diff = milis;
		long secondInMillis = 1000;
		long minuteInMillis = secondInMillis * 60;
		long hourInMillis = minuteInMillis * 60;
		long dayInMillis = hourInMillis * 24;

		long elapsedDays = diff / dayInMillis;
		diff -= elapsedDays * dayInMillis;
		long elapsedHours = diff / hourInMillis;
		diff -= elapsedHours * hourInMillis;
		long elapsedMinutes = diff / minuteInMillis;
		diff -= elapsedMinutes * minuteInMillis;
		long elapsedSeconds = diff / secondInMillis;
		diff -= elapsedSeconds * secondInMillis;

		String ret = "{";
		if (elapsedDays > 0)
			ret += "d: " + elapsedDays + ", ";
		if (elapsedHours > 0)
			ret += "h: " + elapsedHours + ", ";
		if (elapsedMinutes > 0)
			ret += "min: " + elapsedMinutes + ", ";

		return ret + "sec: " + elapsedSeconds + ", ms: " + diff + "}";
	}
}
