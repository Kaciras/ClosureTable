async function invokeSQL(name, ...args) {
	const response = await fetch(`http://localhost:6666/${name}`, {
		method: "POST",
		body: JSON.stringify(args),
	});
	return await response.json();
}

function showNodes(list) {
	const [tBody] = document.getElementById("table").tBodies;

	for (const item of list) {
		const tRow = document.createElement("tr");

		const id = document.createElement("td");
		tRow.append(id);
		id.textContent = item.id;

		const name = document.createElement("td");
		tRow.append(name);
		name.textContent = item.name;

		tBody.append(tRow);
	}
}

const container = document.getElementById("console");
const button = document.createElement("button");
button.textContent = "查询全部分类";
button.onclick = async () => {
	const { sql, time, data } = invokeSQL("selectDescendant", 0);
	showNodes(data);
	document.getElementById("time").textContent = time;
	document.getElementById("sql").textContent = sql;
};
container.append(button);
