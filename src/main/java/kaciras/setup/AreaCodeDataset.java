package kaciras.setup;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.input.BoundedInputStream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class AreaCodeDataset implements Iterator<DataRow>, AutoCloseable {

	// 代码总共 12 位，省 2 位，市 2 位，县 2 位，镇 3 位，村街道 3 位。
	private static final long[] SHIFTS = {1, 1000, 1000000, 100000000, 10000000000L};

	@Getter
	private final long total;

	private final BoundedInputStream stream;
	private final BufferedReader reader;

	private String line;

	public AreaCodeDataset() throws Exception {
		var loader = AreaCodeDataset.class.getClassLoader();
		var uri = loader.getResource("area_code_2022.csv");
		if (uri == null) {
			throw new FileNotFoundException("area_code_2022.csv");
		}

		var path = Paths.get(uri.toURI());
		stream = BoundedInputStream.builder().setPath(path).get();
		total = Files.size(path);
		reader = new BufferedReader(new InputStreamReader(stream));
	}

	@SneakyThrows
	@Override
	public boolean hasNext() {
		return (line = reader.readLine()) != null;
	}

	@Override
	public DataRow next() {
		return new AreaCodeRow(line);
	}

	public long getProgress() {
		return stream.getCount();
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}

	static class AreaCodeRow implements DataRow {

		private final String[] columns;

		public AreaCodeRow(String line) {
			columns = line.split(",");
		}

		@Override
		public String getName() {
			return columns[1];
		}

		@Override
		public long getId() {
			return Long.parseLong(columns[0]);
		}

		@Override
		public long[] getAncestorIds() {
			var ids = new long[Integer.parseInt(columns[2])];
			var code = getId();
			for (int i = 0; i < ids.length; i++) {
				var shift = SHIFTS[5 - ids.length + i];
				ids[i] = code / shift * shift;
			}
			return ids;
		}
	}
}
