package kaciras.setup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DataImporter implements AutoCloseable {

	private final DBManager manager;
	private final Connection connection;
	private final String postScript;

	DataImporter(DBManager manager, Connection connection, String postScript) {
		this.manager = manager;
		this.connection = connection;
		this.postScript = postScript;
	}

	@Override
	public void close() throws Exception {
		manager.executeScript(connection, postScript);
		connection.close();
	}

	public abstract void importData(DataRow items) throws SQLException;

	static class Closure extends DataImporter {

		private final PreparedStatement attr;
		private final PreparedStatement tree;

		public Closure(DBManager manager, Connection connection, String postScript) throws SQLException {
			super(manager, connection, postScript);
			attr = connection.prepareStatement("INSERT INTO category (id, name) VALUES (?,?)");
			tree = connection.prepareStatement("INSERT INTO category_tree (ancestor, descendant, distance) VALUES (?,?,?)");
		}

		@Override
		public void importData(DataRow row) throws SQLException {
			var ancestor = row.getAncestorIds();

			attr.setLong(1, row.getId());
			attr.setString(2, row.getName());
			attr.execute();

			for (int i = 0; i < ancestor.length; i++) {
				tree.setLong(1, ancestor[i]);
				tree.setLong(2, row.getId());
				tree.setInt(3, i);
				tree.addBatch();
			}

			tree.setLong(1, 0);
			tree.setLong(2, row.getId());
			tree.setInt(3, ancestor.length);
			tree.addBatch();
			tree.executeBatch();
		}
	}

	static class Adjacent extends DataImporter {

		private final PreparedStatement stat;

		public Adjacent(DBManager manager, Connection connection, String postScript) throws SQLException {
			super(manager, connection, postScript);
			stat = connection.prepareStatement("INSERT INTO adjacent (id, parent, name) VALUES (?,?,?)");
		}

		@Override
		public void importData(DataRow row) throws SQLException {
			stat.setLong(1, row.getId());
			stat.setLong(2, row.getAncestorIds()[0]);
			stat.setString(3, row.getName());
			stat.execute();
		}
	}
}
