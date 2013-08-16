package io;

public interface DocumentProvider<T> {

	public void init(String query) throws Exception;
	
	public void close() throws Exception;
	
	public void iterate(DocumentHandler<T> handler, String query);

}
