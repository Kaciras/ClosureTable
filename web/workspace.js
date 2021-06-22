import { update } from "./tree.js";

function updateTable(list) {
	const tBody = document.createElement("tbody");

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

	const el = document.getElementById("table");
	el.replaceChild(tBody, el.tBodies[0]);
}

function groupByParent(list) {
	const result = {};
	for (const category of list) {
		const { parent } = category;
		let children = result[parent];
		if (!children) {
			children = result[parent] = [];
		}
		children.push(category);
	}
	return result;
}

function updateTreeView(list) {
	const root = document.getElementById("tree");

	function addTreeNode(map, id) {
		const el = document.createElement()
	}

	addTreeNode(groupByParent(list), 0);
}

export function updateList(list) {
	updateTable(list);
	updateTreeView(data);
}

export function updateMetadata(result) {
	const { sql, time } = result
	document.getElementById("time").textContent = time;
	document.getElementById("sql").textContent = sql;
}

export function addButton() {
	update();
}
