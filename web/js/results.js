const filterEl = document.getElementById("tree-only");
let currentResult = document.getElementById("simple-result");

let result;

export function setResult(value) {
	result = value;
	refreshInfoSection();
}

/**
 * 刷新左下方的执行时间和 SQL 等信息，在结果和选项改变后都要调用。
 */
function refreshInfoSection() {
	let { sqls, time } = result;

	if (filterEl.checked) {
		sqls = sqls.filter(s => s.includes("category_tree"));
	}
	sqls = sqls.map(s => s + ";").join("\n");

	const sqlHTML = Prism.highlight(sqls, Prism.languages.sql, "sql");
	document.getElementById("sql").innerHTML = sqlHTML;
	document.getElementById("time").textContent = `${time}ms`;
}

filterEl.oninput = refreshInfoSection;

function switchResultPanel(id) {
	currentResult.style.display = "none";
	currentResult = document.getElementById(id);
	currentResult.style.display = "block";
}

export function showSimpleResult(text) {
	switchResultPanel("simple-result");
	currentResult.textContent = text;
}

export function showErrorResult(ex) {
	const title = document.createElement("p");
	title.textContent = "执行失败，错误：" + ex.type;

	const message = document.createElement("p");
	message.textContent = ex.message;

	switchResultPanel("error-result");
	currentResult.innerHTML = "";
	currentResult.append(title, message);
}

export function updateTable(list) {
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
