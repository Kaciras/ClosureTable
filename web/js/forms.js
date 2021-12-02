const tabList = document.getElementById("tab-list");
const formPanel = document.getElementById("forms");
const buttonGroup = document.getElementById("actions");

const tabMap = {};
let methodName;

export function setCurrentTab(name) {
	const { button, panel } = tabMap[name];
	const prev = tabMap[methodName];

	methodName = name;
	panel.classList.add("active");
	button.classList.add("active");

	if (prev) {
		prev.button.classList.remove("active");
		prev.panel.classList.remove("active");
	}
}

/**
 * 注册表单处理函数，在点击执行按钮时调用。
 * 处理函数的两个参数分别是当前面板，以及当前表单的数据。
 *
 * @param handler 处理函数
 */
export function onSubmit(handler) {
	document.getElementById("invoke").onclick = () => {
		const { def, panel } = tabMap[methodName];
		const data = new FormData(panel);

		panel.querySelectorAll("input[type=checkbox]")
			.forEach(i => data.append(i.name, i.checked));

		handler(def, Object.fromEntries(data));
	};
}

// 虽然纯 CSS 也能做 TabPanel，但比 JS 还麻烦所以不用。

export function addForm(def) {
	const { api, name, fields } = def;

	const radio = document.createElement("input");
	radio.type = "radio";
	radio.name = "tab";
	radio.className = "hide";

	const button = document.createElement("label");
	button.textContent = name;
	button.className = "tab-button";
	button.append(radio);

	const panel = document.createElement("form");
	for (const field of fields) {
		const { label, ...attrs } = field;
		const wrapper = document.createElement("label");
		wrapper.className = "field";

		const el = document.createElement("input");
		wrapper.append(label, Object.assign(el, attrs));
		panel.append(wrapper);
	}

	radio.addEventListener("change", () => {
		setCurrentTab(api);
	});

	tabMap[api] = { button, panel, def };
	tabList.append(button);
	formPanel.insertBefore(panel, buttonGroup);
}
