import { updateTreeGraph } from "./tree.js";

const API = "http://localhost:6666/api/";

/**
 * 调用后端控制器（Controller.java）里的方法，
 * HttpAdapter.java 用于接收请求，并转换为方法调用。
 *
 * @param name 方法名
 * @param args 方法参数
 * @return 成功返回 ResultView，出错返回 ErrorView
 */
async function invokeAPI(name, args = {}) {
	const request = new Request(API + name, {
		method: "POST",
		body: JSON.stringify(args),
	});
	const response = await fetch(request);
	const body = await response.json();
	return { status: response.status, body };
}

/**
 * 刷新左下方的执行时间和 SQL 等信息。
 *
 * @param body API 返回的结果（ResultView）
 */
function showSQLInfo(body) {
	const { sqls, time } = body

	const combined = sqls
		.filter(s => s.includes("category_tree"))
		.map(s => s + ";")
		.join("\n");

	const sqlHTML = Prism.highlight(combined, Prism.languages.sql, "sql");

	document.getElementById("time").textContent = time;
	document.getElementById("sql").innerHTML = sqlHTML;
}

/**
 * 刷新左上方的树图表，在每个修改操作之后都会调用。
 */
function refreshTreeView() {
	document.getElementById("shell-loading").style.opacity = "1";
	invokeAPI("getAll").then(result => {
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
let methodName;

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
		const t = tabMap[methodName];
		methodName = api;

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

methodName = "getPath";
refreshTreeView();

{
	const { button, panel } = tabMap[methodName];
	panel.classList.add("active");
	button.classList.add("active");
}

document.getElementById("invoke").onclick = async () => {
	const { handler, panel } = tabMap[methodName];

	const form = new FormData(panel);
	for (const checkbox of panel.querySelectorAll("input[type=checkbox]")) {
		form.append(checkbox.name, checkbox.checked);
	}
	const args = Object.fromEntries(form);

	const { status, body } = await invokeAPI(methodName, args);

	if (status === 200) {
		handler(body.data);
		showSQLInfo(body);
	} else {
		showErrorResult(body);
	}
}
