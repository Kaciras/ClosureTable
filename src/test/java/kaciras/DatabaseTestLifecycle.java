package kaciras;

import kaciras.setup.DBManager;
import kaciras.setup.SimpleDataset;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.extension.*;

/**
 * 每个测试都要配置数据库，所以就把这部分逻辑提出来了。
 */
public final class DatabaseTestLifecycle implements
		BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	private final DBManager manager;

	private final SqlSession session;
	private final Repository repository;

	public DatabaseTestLifecycle() throws Exception {
		manager = DBManager.open();
		session = Utils.createSqlSession(new TrackingDataSource(manager.getConnection()));

		var mapper = session.getMapper(CategoryMapper.class);
		repository = new Repository(Category.mapper = mapper);
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		try (var importer = manager.createTable("closure.sql")) {
			try (var ds = new SimpleDataset()) {
				while (ds.hasNext()) {
					importer.importData(ds.next());
				}
			}
		}
	}

	@Override
	public void afterEach(ExtensionContext context) {
		session.rollback(true);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		manager.dropTables();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		var instance = context.getRequiredTestInstance();
		inject(instance, "session", session);
		inject(instance, "manager", manager);
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
