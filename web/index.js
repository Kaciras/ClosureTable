import { addButton, updateList, updateMetadata } from "./workspace.js";
import { update } from "./tree.js";

const API = "http://localhost:6666/api/";

function invokeSQL(name, args = {}) {
	const request = new Request(API + name, {
		method: "POST",
		body: JSON.stringify(args),
	});
	return fetch(request).then(r => r.json());
}

const tabTemplate = document.getElementById("tab-template");

const Type = {
	Modify: 0,
	QueryValue: 1,
	QueryList: 2,
}

const buttons = [
	{
		content: "查询全部分类",

	},
	{
		content: "查询全部分类",
		type: Type.QueryList,
		async onClick() {
			const { sql, time, data } = await invokeSQL("getPath", { id: 10, ancestor: 1 });
			updateList(data);
			updateMetadata({ sql, time, data });
		}
	},
	{
		content: "分类间的路径",
		async onClick() {
			addButton();
		}
	},
	{
		content: "查询分类的级别"

	},
];

const container = document.getElementById("console");
const button = document.createElement("button");
button.textContent = "查询全部分类";
button.onclick = async () => {
	addButton();
	// const { sql, time, data } = await invokeSQL("getPath", 10, 1);
	//
	// refreshTable(data);
	// refreshTreeView(data);
	//
	// document.getElementById("time").textContent = time;
	// document.getElementById("sql").textContent = sql;
};
container.append(button);

invokeSQL("getAll").then(r => update(r.data));

// 虽然纯 CSS 也能做 TabPanel，但比 JS 麻烦所以不用
class ConsoleTabPanels {

	constructor() {
		this.buttons = [];
	}

	addTab(name, panel) {
		const index = this.buttons.length;


		this.buttons.push(button);
	}

	mount() {
		this.buttons[0].firstElementChild.checked = true;
		this.buttons[0].classList.add("active");

		const div = document.getElementById("console-head");
		this.buttons.forEach(b => div.append(b));
	}
}

const tabMap = {};
let currentTab;

function addTab(name, api) {
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
		currentTab.button.classList.remove("active");
		currentTab.panel.classList.remove("active");
		currentTab = tabMap[name];
	});

	tabMap[name] = { button, panel };
	document.getElementById("console-head").append(button);
}

addTab("插入", "create");
addTab("更新", "update");
addTab("删除", "delete");
addTab("移动", "move");
addTab("查询级别", "getLevel");
addTab("查询路径", "getPath");
addTab("查询子层", "getSubLayer");
addTab("查询子树", "getTree");

currentTab = tabMap["查询路径"];
currentTab.button.classList.add("active");
currentTab.panel.classList.add("active");
