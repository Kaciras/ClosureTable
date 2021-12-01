const button = document.getElementById("invoke");

const tabMap = {};
let methodName;

export function setCurrentTab(name) {
	const { button, panel } = tabMap[name];
	methodName = name;
	panel.classList.add("active");
	button.classList.add("active");
}

export function onSubmit(handler) {
	button.onclick = () => {
		const { def, panel } = tabMap[methodName];

		const data = new FormData(panel);

		panel.querySelectorAll("input[type=checkbox]")
			.forEach(i => data.append(i.name, i.checked));

		handler(def, Object.fromEntries(data));
	};
}

// 虽然纯 CSS 也能做 TabPanel，但比 JS 麻烦所以不用
export function defineForm(...definition) {
	for (const def of definition) {
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
			const t = tabMap[methodName];
			methodName = api;

			t.button.classList.remove("active");
			t.panel.classList.remove("active");

			panel.classList.add("active");
			button.classList.add("active");
		});

		tabMap[api] = { button, panel, def };
		document.getElementById("forms").insertBefore(panel, document.getElementById("actions"));
		document.getElementById("console-head").append(button);
	}
}
