package io;


public interface DocumentHandler<T> {

	public void handle(T document);

	public String getWord();
	
	public void setWord(String word);
	
}
