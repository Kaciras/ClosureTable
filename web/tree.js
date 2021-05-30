const treeData = {
	"name": "Top Level",
	"children": [
		{
			"name": "Level 2: A",
			"children": [
				{ "name": "Son of A" },
				{ "name": "Daughter of A" },
			]
		},
		{ "name": "Level 2: B" },
	]
};

const treeData2 = {
	"name": "Top Level",
	"children": [
		{
			"name": "Level 2: A",
		},
		{ "name": "Son of A" },
		{ "name": "Daughter of A" },
		{ "name": "Level 2: B" },
	]
};

let svg;

update(treeData)

export function update(list = treeData2) {
	if (svg) {
		svg.remove();
	}
	svg = d3.select("#tree-view")
		.append("svg")
		.attr("width", 700)
		.attr("height", 600);

	const g = svg.append("g");

	const treemap = d3.tree().size([700, 600]);
	const nodes = treemap(d3.hierarchy(list));

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

	const zoom = d3.zoom()
		.scaleExtent([1, 8])
		.on('zoom', event => g.attr('transform', event.transform));

	svg.call(zoom);
}
