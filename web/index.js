import { updateTreeGraph } from "./tree.js";

const API = "http://localhost:6666/api/";

async function invokeSQL(name, args = {}) {
	const request = new Request(API + name, {
		method: "POST",
		body: JSON.stringify(args),
	});
	const response = await fetch(request);
	const body = await response.json();
	return { status: response.status, body };
}

function processSql(sqls) {
	return sqls.filter(s => s.includes("category_tree")).map(s => s + ";").join("\n");
}

function refreshTreeView() {
	document.getElementById("shell-loading").style.opacity = "1";
	invokeSQL("getAll").then(result => {
		updateTreeGraph(result.body.data);
		document.getElementById("shell-loading").style.opacity = "0";
	});
}

let currentResult = document.getElementById("simple-result");

function switchResultPanel(id) {
	currentResult.style.display = "none";
	currentResult = document.getElementById(id);
	currentResult.style.display = "block";
}

function showErrorResult(data) {
	const title = document.createElement("p");
	title.textContent = "执行失败，错误：" + data.type;

	const message = document.createElement("p");
	message.textContent = data.message;

	switchResultPanel("error-result");
	currentResult.innerHTML = "";
	currentResult.append(title, message);
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

	switchResultPanel("list-result");
}

const tabMap = {};
let currentTab;

// 虽然纯 CSS 也能做 TabPanel，但比 JS 麻烦所以不用
function addTab(api, name, handler) {
	const radio = document.createElement("input");
	radio.type = "radio";
	radio.name = "tab";
	radio.className = "hide";

	const button = document.createElement("label");
	button.textContent = name;
	button.className = "tab-button";
	button.append(radio);

	const panel = document.getElementById(api);

	radio.addEventListener("change", () => {
		const t = tabMap[currentTab];
		currentTab = api;

		t.button.classList.remove("active");
		t.panel.classList.remove("active");

		panel.classList.add("active");
		button.classList.add("active");
	});

	tabMap[api] = { button, panel, handler };
	document.getElementById("console-head").append(button);
}

addTab("create", "插入", value => {
	refreshTreeView();
	switchResultPanel("simple-result");
	currentResult.textContent = `新增分类的 ID：${value.id}`;
});
addTab("update", "更新", refreshTreeView);
addTab("delete", "删除", refreshTreeView);
addTab("move", "移动", refreshTreeView);
addTab("getLevel", "查询级别", value => {
	switchResultPanel("simple-result");
	currentResult.textContent = value;
});
addTab("getPath", "查询路径", updateTable);
addTab("getTree", "查询子树", updateTable);
addTab("getSubLayer", "查询子层", updateTable);

currentTab = "getPath";
refreshTreeView();

{
	const { button, panel } = tabMap[currentTab];
	panel.classList.add("active");
	button.classList.add("active");
}

document.getElementById("invoke").onclick = async () => {
	const { handler, panel } = tabMap[currentTab];
	const form = new FormData(panel);
	for (const checkbox of panel.querySelectorAll("input[type=checkbox]")) {
		form.append(checkbox.name, checkbox.checked);
	}
	const args = Object.fromEntries(form);
	const { status, body } = await invokeSQL(currentTab, args);

	if (status !== 200) {
		showErrorResult(body);
	} else {
		const { sqls, time, data } = body
		handler(data);
		document.getElementById("time").textContent = time;
		const sqlHTML = Prism.highlight(processSql(sqls), Prism.languages.sql, 'sql');
		document.getElementById("sql").innerHTML = sqlHTML;
	}
}
