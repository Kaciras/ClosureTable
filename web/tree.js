let svg;

const container = document.getElementById("tree-view");

/**
 *
 * @param list
 * @see https://github.com/xswei/d3-hierarchy/blob/master/README.md
 */
export function updateTreeView(list) {
	if (svg) {
		svg.remove();
	}
	svg = d3.select("#tree-view")
		.append("svg")
		.attr("width", container.clientWidth)
		.attr("height", container.clientHeight);

	const zoom = d3.zoom()
		.scaleExtent([1, 8])
		.on('zoom', event => g.attr('transform', event.transform));

	svg.call(zoom);

	const g = svg.append("g");

	/*
	 * stratify() 能将列表转为层次结构数据。
	 */
	const treemap = d3.tree().size([700, 600]);
	const nodes = treemap(d3.stratify()(list));

	const link = g.selectAll(".link")
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
		.attr("class", d => "node " + d.children ? "internal" : "leaf")
		.attr("transform", d => "translate(" + d.x + "," + d.y + ")");

	node.append("circle").attr("r", 10);

	node.append("text")
		.attr("dy", ".35em")
		.attr("y", d => d.children ? -20 : 20)
		.style("text-anchor", "middle")
		.text(d => d.data.name);
}
