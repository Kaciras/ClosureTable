let svg;

const container = document.getElementById("tree-view");

/**
 * 更新树状图，其实就是删了原来的并重建一个。
 *
 * @param list 分类列表
 * @see https://github.com/xswei/d3-hierarchy/blob/master/README.md
 */
export function updateTreeGraph(list) {
	if (svg) {
		svg.remove();
	}
	svg = d3.select("#tree-view")
		.append("svg")
		.attr("width", container.clientWidth)
		.attr("height", container.clientHeight);

	const zoom = d3.zoom()
		.scaleExtent([1, 4])
		.on("zoom", event => g.attr("transform", event.transform));

	svg.call(zoom);

	const g = svg.append("g");

	/*
	 * stratify() 能将列表转为层次结构数据。
	 */
	const treemap = d3.tree().size([700, 600]);
	const nodes = treemap(d3.stratify()(list));

	g.selectAll(".link")
		.data(nodes.descendants().slice(1))
		.enter()
		.append("path")
		.attr("class", "link")
		.attr("d", d => "M" + d.x + "," + d.y
			+ "C" + d.x + "," + (d.y + d.parent.y) / 2
			+ " " + d.parent.x + "," + (d.y + d.parent.y) / 2
			+ " " + d.parent.x + "," + d.parent.y
		);

	const node = g.selectAll(".node")
		.data(nodes.descendants())
		.enter().append("g")
		.attr("class", d => "node " + (d.children ? "internal" : "leaf"))
		.attr("transform", d => "translate(" + d.x + "," + d.y + ")");

	node.append("circle").attr("r", 14);

	node.append("text")
		.attr("class", "id")
		.attr("text-anchor","middle")
		.attr("y", 5)
		.text(d => d.data.id);

	node.append("text")
		.attr("class", "name")
		.attr("dy", ".35em")
		.attr("y", d => d.children ? -25 : 25)
		.style("text-anchor", "middle")
		.text(d => d.data.name);
}
