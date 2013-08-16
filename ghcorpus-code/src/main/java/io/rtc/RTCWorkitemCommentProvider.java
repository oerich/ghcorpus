package io.rtc;

import io.DocumentHandler;
import io.DocumentProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RTCWorkitemCommentProvider implements DocumentProvider<String> {

	private Connection connection;
	private PreparedStatement statement;

	public void init(String query) throws ClassNotFoundException, SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager
					.getConnection("jdbc:postgresql://127.0.0.1:5432/jazz-extraction");
		}
		if (query != null)
			this.statement = this.connection.prepareStatement(query);
	}

	public void close() throws SQLException {
		if (connection != null)
			connection.close();
	}

	public void iterate(DocumentHandler<String> handler, String query) {
		try {
			// Get a statement

			// get all relevant documents
			ResultSet rs = null;
			if (this.statement == null) {
				rs = connection.createStatement().executeQuery(query);
			} else {
				rs = this.statement.executeQuery();
			}

			// iterate over them
			while (rs.next()) {
				handler.handle(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// clean up

		}
	}

}
