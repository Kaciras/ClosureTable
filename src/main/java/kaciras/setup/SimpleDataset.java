package kaciras.setup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleDataset implements Iterator<DataRow>, AutoCloseable {

	private final Map<Long, SimpleRow> map = new HashMap<>();
	private final Iterator<SimpleRow> iterator;

	public SimpleDataset() throws IOException {
		var loader = AreaCodeDataset.class.getClassLoader();
		var stream = loader.getResourceAsStream("simple_categories.csv");
		if (stream == null) {
			throw new FileNotFoundException("simple_categories.csv");
		}
		try (var reader = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				var columns = line.split(",");
				var id = Long.parseLong(columns[0]);
				map.put(id, new SimpleRow(id, columns[1], Long.parseLong(columns[3])));
			}
		}
		iterator = map.values().iterator();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public DataRow next() {
		return iterator.next();
	}

	@Override
	public void close() {}

	@RequiredArgsConstructor
	@Getter
	class SimpleRow implements DataRow {

		final long id;
		final String name;
		final long parent;

		@Override
		public long[] getAncestorIds() {
			var list = new ArrayList<Long>();
			for (var p = id; p != 0; p = map.get(p).parent) {
				list.add(p);
			}
			return list.stream()
					.mapToLong(Long::longValue)
					.toArray();
		}
	}
}
