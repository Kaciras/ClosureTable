package kaciras;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.extension.*;

public class DatabaseTestLifecycle implements
		BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	private PooledDataSource dataSource;
	private SqlSession session;
	private Repository repository;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		dataSource = Utils.getDaraSource();
		session = Utils.createSqlSession(dataSource);

		Utils.importData(dataSource, session);

		var mapper = session.getMapper(CategoryMapper.class);
		Category.mapper = mapper;
		repository = new Repository(mapper);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		session.rollback(true);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		Utils.dropTables(session.getConnection());
		session.close();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		var instance = context.getRequiredTestInstance();
		inject(instance, "session", session);
		inject(instance, "dataSource", dataSource);
		inject(instance, "repository", repository);
	}

	private void inject(Object obj, String field, Object value) throws Exception {
		try {
			obj.getClass().getDeclaredField(field).set(obj, value);
		} catch (NoSuchFieldException ignore) {

		}
	}
}
