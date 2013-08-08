package oerich.git_comment_corpus;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

public class GzJsonReaderTest {

	private GzJsonReader jr;

	@Before
	public void setUp() throws Exception {
		this.jr = new GzJsonReader();
	}

	@Test
	public void testRead() throws IOException, ParseException {
		InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"2013-07-02-12.json.gz");
		assertNotNull(resourceAsStream);
		
		this.jr.setCorpus(new CorpusDBDummy());
		this.jr.setCounter(new WordCountDummy());
		
		ICorpusDB corpus = this.jr.read(resourceAsStream);
		assertNotNull(corpus);

	}

//	@Test (expected=RuntimeException.class)
//	public void notInitialized() throws IOException, ParseException {
//		InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
//				"2013-07-02-12.json.gz");
//		this.jr.read(resourceAsStream);
//	}
}
