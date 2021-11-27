package kaciras;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.extension.*;

import javax.sql.DataSource;

/**
 * 每个测试都要配置数据库，所以就把这部分逻辑提出来了。
 */
public final class DatabaseTestLifecycle implements
		BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	private final DBManager dbManager;

	private final DataSource dataSource;
	private final SqlSession session;
	private final Repository repository;

	public DatabaseTestLifecycle() throws Exception {
		dbManager = DBManager.open();

		dataSource = dbManager.getDataSource();
		session = Utils.createSqlSession(dataSource);

		var mapper = session.getMapper(CategoryMapper.class);
		Category.mapper = mapper;
		repository = new Repository(mapper);
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		dbManager.importData();
	}

	@Override
	public void afterEach(ExtensionContext context) {
		session.rollback(true);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		session.close();
		dbManager.dropTables();
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
			// 依赖注入是可选的，没有相应的字段就不注入。
		}
	}
}
