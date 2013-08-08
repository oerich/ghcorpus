package oerich.git_comment_corpus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;

import oerich.nlputils.NLPProperties;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

public class MongoDBJsonReaderTest {

	private MongoDBJsonReader jr;

	@Before
	public void setUp() throws Exception {
		this.jr = new MongoDBJsonReader();
		NLPProperties.getInstance().setResourcePath("/");
	}

	@Test
	public void testRead() throws IOException, ParseException {		
//		this.jr.setCorpus(new CorpusDBDummy());
//		this.jr.setCounter(new WordCountDummy());
		
		ICorpusDB corpus = this.jr.read("ghtorrent", -1);
		assertNotNull(corpus);

		assertTrue(corpus.contains("the"));
		assertEquals(27, corpus.getFrequency("the"));
	}

//	@Test (expected=RuntimeException.class)
//	public void notInitialized() throws IOException, ParseException {
//		InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
//				"2013-07-02-12.json.gz");
//		this.jr.read(resourceAsStream);
//	}
	
	@Test
	public void testTop10Projects() throws UnknownHostException {
		String[] result = this.jr.getTopRepos();
		assertEquals(10, result.length);
		assertEquals("coolsoft-13", result[0]);
	}
}
