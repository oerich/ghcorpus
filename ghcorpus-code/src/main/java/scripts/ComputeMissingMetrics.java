package scripts;

import io.DocumentHandler;

public class ComputeMissingMetrics implements DocumentHandler<String> {

	public class PreprocessHandler implements DocumentHandler<String> {

		public void handle(String d) {
			// assume, we get a line from our file.
		}

		public String getWord() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setWord(String word) {
			// TODO Auto-generated method stub

		}

	}

	public void handle(String d) {
		// assume we get one line from the classifier db

		// compute

	}

	public String getWord() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setWord(String word) {
		// TODO Auto-generated method stub

	}

}
