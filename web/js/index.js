import { updateTreeGraph } from "./tree.js";
import { addForm, onSubmit, setCurrentTab } from "./input.js";
import { setResult, showErrorResult, showSimpleResult, updateTable } from "./results.js";

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
 * 刷新左上方的树图表，在每个修改操作之后都会调用。
 */
function refreshTreeView() {
	document.getElementById("shell-loading").style.opacity = "1";
	invokeAPI("getAll").then(result => {
		updateTreeGraph(result.body.data);
		document.getElementById("shell-loading").style.opacity = "0";
	});
}

addForm({
	api: "create",
	name: "插入",
	handler: value => {
		refreshTreeView();
		showSimpleResult(`新增分类的 ID：${value.id}`);
	},
	fields: [
		{
			name: "parentId",
			label: "插入到哪个分类下面",
			type: "number",
		},
		{
			name: "name",
			label: "分类名",
			value: "新建分类"
		}
	]
});
addForm({
	api: "update",
	name: "更新",
	handler: refreshTreeView,
	fields: [
		{
			name: "id",
			label: "分类的 ID",
			type: "number",
			value: "1"
		},
		{
			name: "newName",
			label: "新的名字",
			value: "新的名字哦"
		}
	]
});
addForm({
	api: "delete",
	name: "删除",
	handler: refreshTreeView,
	fields: [
		{
			name: "id",
			label: "分类的 ID",
			type: "number",
			value: "7"
		},
		{
			name: "single",
			label: "仅单个节点而非整个子树",
			type: "checkbox",
		},
	]
});
addForm({
	api: "move",
	name: "移动",
	handler: refreshTreeView,
	fields: [
		{
			name: "id",
			label: "分类的 ID",
			value: "2",
			type: "number"
		},
		{
			name: "parent",
			label: "新的父分类",
			value: "7",
			type: "number"
		},
		{
			name: "single",
			label: "仅单个节点而非整个子树",
			type: "checkbox",
		},
	]
});
addForm({
	api: "getLevel",
	name: "查询级别",
	handler: showSimpleResult,
	fields: [{
		name: "id",
		label: "节点的 ID",
		value: "7",
		type: "number"
	}]
});
addForm({
	api: "getPath",
	name: "查询路径",
	handler: updateTable,
	fields: [
		{
			name: "ancestor",
			label: "上级分类的 ID",
			value: "0",
			type: "number"
		},
		{
			name: "descendant",
			label: "下级分类的 ID",
			value: "8",
			type: "number"
		},
	]
});
addForm({
	api: "getTree",
	name: "查询子树",
	handler: updateTable,
	fields: [{
		name: "id",
		label: "节点的 ID",
		value: "7",
		type: "number"
	}]
});
addForm({
	api: "getSubLayer",
	name: "查询子层",
	handler: updateTable,
	fields: [
		{
			name: "id",
			label: "节点的 ID",
			value: "7",
			type: "number"
		},
		{
			name: "depth",
			label: "距离 N",
			value: "1",
			type: "number"
		},
	]
});

refreshTreeView();
setCurrentTab("getPath");

onSubmit(async (def, args) => {
	const { api, handler } = def;
	const { status, body } = await invokeAPI(api, args);

	if (status === 200) {
		setResult(body);
		handler(body.data);
	} else {
		showErrorResult(body);
	}
});
