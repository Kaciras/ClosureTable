import { updateTreeView } from "./tree.js";

const API = "http://localhost:6666/api/";

function invokeSQL(name, args = {}) {
	const request = new Request(API + name, {
		method: "POST",
		body: JSON.stringify(args),
	});
	return fetch(request).then(r => r.json());
}

function refreshTreeView() {
	invokeSQL("getAll").then(r => updateTreeView(r.data));
}

function updateSimple(value) {
	document.getElementById("list-result").style.display = "none";
	document.getElementById("simple-result").style.display = "block";
	document.getElementById("simple-result").textContent = value;
}

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

	document.getElementById("simple-result").style.display = "none";
	document.getElementById("list-result").style.display = "block";
}

const ActionType = {
	Modify: 0,
	QueryValue: 1,
	QueryList: 2,
}

const tabMap = {};
let currentTab;

// 虽然纯 CSS 也能做 TabPanel，但比 JS 麻烦所以不用
function addTab(api, name, type) {
	const radio = document.createElement("input");
	radio.type = "radio";
	radio.name = "tab";
	radio.className = "hide";

	const button = document.createElement("label");
	button.className = "tab-button";
	button.textContent = name;
	button.append(radio);

	const panel = document.getElementById(api);

	radio.addEventListener("change", () => {
		panel.classList.add("active");
		button.classList.add("active");
		tabMap[currentTab].button.classList.remove("active");
		tabMap[currentTab].panel.classList.remove("active");
		currentTab = api;
	});

	tabMap[api] = { button, panel, type };
	document.getElementById("console-head").append(button);
}

addTab("create", "插入", ActionType.Modify);
addTab("update", "更新", ActionType.Modify);
addTab("delete", "删除", ActionType.Modify);
addTab("move", "移动", ActionType.Modify);
addTab("getLevel", "查询级别", ActionType.QueryValue);
addTab("getPath", "查询路径", ActionType.QueryList);
addTab("getSubLayer", "查询子层", ActionType.QueryList);
addTab("getTree", "查询子树", ActionType.QueryList);

currentTab = "getPath";
refreshTreeView();

{
	const { button, panel } = tabMap[currentTab];
	panel.classList.add("active");
	button.classList.add("active");
}

document.getElementById("invoke").onclick = async () => {
	const { type, panel } = tabMap[currentTab];
	const body = Object.fromEntries(new FormData(panel));
	const { sql, time, data } = await invokeSQL(currentTab, body);

	switch (type) {
		case ActionType.Modify:
			refreshTreeView();
			break;
		case ActionType.QueryValue:
			updateSimple(data);
			break;
		case ActionType.QueryList:
			updateTable(data);
			break;
	}

	document.getElementById("time").textContent = time;
	document.getElementById("sql").textContent = sql
		.filter(line => line.includes("category_tree")).join(";\n");
}
