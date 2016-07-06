ProcessChart = function(callback) {

	// graph
	// Set up an SVG group so that we can translate the final graph.
	var application = callback;
	var svg = d3.select("#svg-processes");

	var margin = {
		top : 20,
		right : 20,
		bottom : 30,
		left : 40
	}, width = $("#svg-processes").width() - margin.left - margin.right, height = $(
			"#svg-processes").height()
			- margin.top - margin.bottom;
	console.log("SVG-size", svg.style('width'), svg.style('height'), width,
			height);

	var x = d3.scale.ordinal().rangeBands([ 0, width ], .1);

	var y = d3.scale.pow().exponent(.3).range([ height, 0 ]);

	var xAxis = d3.svg.axis().scale(x).orient("bottom");

	var yAxis = d3.svg.axis().scale(y).orient("left").ticks(3).tickFormat(
			function(d) {
				var prefix = d3.formatPrefix(d);
				return prefix.scale(d) + prefix.symbol;
			});

	var tip = d3.tip().attr('class', 'd3-tip').offset([ -10, 0 ]).html(
			function(d) {
				return "<strong>ID:</strong> <span style='color:red'>"
						+ d.footprint + "</span>";
			})

	svg = svg.attr("cursor", "move").append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	svg.call(tip);
	svg.append("g").attr("class", "x axis").attr("transform",
			"translate(0," + height + ")").call(xAxis);

	svg.append("g").attr("class", "y axis").call(yAxis);

	// clipping
	svg.append("defs").append("clipPath").attr("id", "mask").append("rect")
			.attr("width", width).attr("height", height);

	var clip = svg.append("g").attr("clip-path", "url(#mask)").attr("cursor", "pointer").append("g");
	
	this.setData = function(data) {
		x.rangeBands([ 0, Math.max(width, 10 * data.length) ], .1);
		x.domain(data.map(function(d) {
			return d.footprint;
		}));
		var maxNoCases = d3.max(data, function(d) {
			return d.noCases;
		});
		y.exponent(maxNoCases > 1000 ? .3 : .5).domain([ 0, maxNoCases ]);

		svg.selectAll("g.y.axis").call(yAxis);

		console.time("updateVariant");
		
		var selection = clip.selectAll(".bar").data(data);
		selection.exit().remove();
		selection
				.enter()
				.append("rect")
				.attr("class", "bar")
				.attr("x", function(d) {
					return x(d.footprint);
				})
				.attr("width", x.rangeBand())
				.attr("y", function(d) {
					return y(d.noCases);
				})
				.attr("height", function(d) {
					return height - y(d.noCases);
				})
				.on(
						"click",
						function(data) {
							console.log("CLICK ??", data, d3.event);
							if (d3.event.ctrlKey) {
								if (!d3.event.target.classList
										.contains('dke-selected')) {
									application.selectModels(application
											.getMergedModel().footprints,
											data.footprint);
								} else {

									application.selectModels(application
											.getMergedModel().footprints
											.filter(function(val) {
												return val != data.footprint;
											}));

								}
							} else {
								application.selectModels(data.footprint);
							}
						})
				.on(
						'mouseover',
						function() {
							tip.show.apply(this, arguments);
							var fpClass = dke.util
									.fpCss(d3.select(this).data()[0].footprint);
							// console.log( d3.select(this).data()[0], fpClass);
							d3.selectAll('#svg-canvas .' + fpClass).classed(
									'dke-selected', true);
						})
				.on(
						'mouseout',
						function() {
							tip.hide.apply(this, arguments);
							var fpClass = dke.util
									.fpCss(d3.select(this).data()[0].footprint);
							d3.selectAll('#svg-canvas .' + fpClass).classed(
									'dke-selected', false);
						});

		selection.transition().attr("x", function(d) {
			return x(d.footprint);
		}).attr("width", x.rangeBand()).attr("y", function(d) {
			return y(d.noCases);
		}).attr("height", function(d) {
			return height - y(d.noCases);
		});

		clip.selectAll(".bar").each(function() {
			var elt = d3.select(this);
			elt.classed(dke.util.fpCss(elt.data()[0].footprint), true);
		});

		console.timeEnd("updateVariant");
		
		// Set up zoom support
		var zoom = d3.behavior.zoom();
		zoom.on("zoom", function() {
			var x = Math.max( d3.event.translate[0], (width - (data.length * 10)));
			x = Math.min( x, 0);
			
			console.log("ZOOM", clip, d3.event.translate, zoom);
			zoom.translate([ x, d3.event.translate[1] ])
			clip.attr("transform", "translate(" + x + ")");
		});
		d3.select("#svg-processes").call(zoom);
	}
	
	this.setData([]);
}

EventChart = function(callback) {
	// graph
	// Set up an SVG group so that we can translate the final graph.
	var application = callback;
	var tooltips = {};

	this.eventTypes = [];

	var svg = d3.select("#svg-events");

	var margin = {
		top : 20,
		right : 20,
		bottom : 30,
		left : 40
	}, width = $("#svg-events").width() - margin.left - margin.right, height = $(
			"#svg-events").height()
			- margin.top - margin.bottom;
	console.log("SVG-size", svg.style('width'), svg.style('height'), width,
			height);

	var x = d3.scale.ordinal().rangeRoundBands([ 0, width ], .1);

	var y = d3.scale.linear().range([ height, 0 ]); // linear

	var xAxis = d3.svg.axis().scale(x).orient("bottom");

	var yAxis = d3.svg.axis().scale(y).orient("left").ticks(1).tickFormat(
			function(d) {
				var prefix = d3.formatPrefix(d);
				return prefix.scale(d) + prefix.symbol;
			});

	var tip = d3.tip().attr('class', 'd3-tip').offset([ -10, 0 ]).html(
			function(d) {
				console.log("tooltip", d, tooltips);
				return "<strong>ID:</strong> <span style='color:red'>"
						+ tooltips[d.id].name + "</span>";
			})

	svg = svg.append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	svg.call(tip);
	svg.append("g").attr("class", "x axis").attr("transform",
			"translate(0," + height + ")").call(xAxis);

	svg.append("g").attr("class", "y axis").call(yAxis);

	this.init = function(eventTypes) {
		this.eventTypes = eventTypes;
		x.domain(eventTypes.map(function(d) {
			return d.id;
		}));

		svg.selectAll("g.x.axis").call(xAxis);

		tooltips = {};
		// Index schreiben, cases aufsummieren
		eventTypes.forEach(function(obj, idx) {
			tooltips[obj.id] = obj;
		}, this);
	};

	this.setData = function(nodes) {
		console.log("update event chart", nodes);

		data = nodes.filter(function(n) {
			if (n.data.shape == 'rect') {
				return n;
			}
		});

		// TODO nodes auf events matchen um umsortierung zu verhindern
		var tmpNodes = {};
		// Index schreiben, cases aufsummieren
		data.forEach(function(obj, idx) {
			tmpNodes[obj.id] = obj;
		}, this);

		// console.log("map");
		data = this.eventTypes.map(function(d) {
			return (tmpNodes[d.id] || {
				data : {}
			});
		})

		// console.log("normalized", tmpNodes, data, this.eventTypes);
		var yMax = d3.max(data, function(d) {
			return d.data.countPass;
		});

		y.domain([ 0, yMax ]);

		yAxis.ticks(Math.min(yMax, 5));
		svg.selectAll("g.y.axis").call(yAxis);

		var selection = svg.selectAll(".bar").data(data);

		console.log("EXIT", selection.exit());
		selection.exit().remove();

		console.log("ENTER", selection.enter());
		selection.enter().append("rect").attr("class", "bar").attr("x",
				function(d) {
					return x(d.id);
				}).attr("width", x.rangeBand()).attr("y", function(d) {
			return y(d.data.countPass);
		}).attr("height", function(d) {
			return height - y(d.data.countPass);
		}).on('mouseover', tip.show).on('mouseout', tip.hide);

		console.log("TRANSITION", selection.transition());
		selection.transition().attr("x", function(d) {
			return x(d.id);
		}).attr("width", x.rangeBand()).attr("y", function(d) {
			return y(d.data.countPass);
		}).attr("y", function(d) {
			return y(d.data.countPass);
		}).attr("height", function(d) {
			return height - y(d.data.countPass);
		});
	};

	this.setData([]);

}

GraphPanel = function(callback, graph) {
	// styles
	this.nodeStyle = new dke.format.DagreNode();
	this.edgeStyle = new dke.format.DagreEdgeAll();

	this.application = callback;

	this.model = null;

	var render = new dagreD3.render();
	this.graph = null;

	var svg = d3.select("#svg-canvas");
	var svgGroup = svg.select("g");

	this.setModel = function(model) {
		this.model = model;

		if (model === null)
			return;

		var c = this.graph === null;
		this.graph = dke.ModelUtils.createGraph(model, this.nodeStyle,
				this.edgeStyle);

		console.time("render");

		render(d3.select("#svg-canvas g"), this.graph);

		console.timeEnd("render");

		console.time("stroke");

		d3.selectAll('#svg-canvas path.path').each(
				function(p) {
					var strokeWidth = parseFloat(d3.select(this).style(
							'stroke-width'));
					console.log(strokeWidth, d3.select(this.parentNode).select(
							'marker'));
					if (strokeWidth > 5)
						d3.select(this.parentNode).select('marker').attr(
								"markerWidth", 4).attr("markerHeight", 3);
				});

		console.timeEnd("stroke");

		if (c)
			this.center();
	}

	this.setEdgeLabelRenderer = function(val) {
		if (val === null) {
			this.edgeStyle = new dke.format.DagreEdgeAll();
		} else if (val) {
			this.edgeStyle = new dke.format.DagreEdgeTime();
		} else {
			this.edgeStyle = new dke.format.DagreEdgeCount();
		}
		this.setModel(this.model);
	}

	this.setNodeLabelRenderer = function(val) {

		if (val) {
			this.nodeStyle = new dke.format.DagreNodeLong(this.application
					.getEventTypes());
		} else {
			this.nodeStyle = new dke.format.DagreNode();
		}
		this.setModel(this.model);
	}

	this.center = function() {
		var xCenterOffset = ($('#svg-canvas').width() - ((this.graph ? this.graph
				.graph().width
				: 0) * this.zoom.scale())) / 2;
		var yCenterOffset = Math
				.max(20, ($('#svg-canvas').height() - ((this.graph ? this.graph
						.graph().height : 0) * this.zoom.scale())) / 2);

		console.log('center', xCenterOffset, d3.select("#svg-canvas g"),
				this.zoom);

		this.zoom.translate([ xCenterOffset, yCenterOffset ]);
		d3.select("#svg-canvas g").attr(
				"transform",
				"translate(" + xCenterOffset + ", " + yCenterOffset + ")scale("
						+ this.zoom.scale() + ")");
	}

	this.zoom = function(zoom) {
		this.zoom = zoom;
	}
}