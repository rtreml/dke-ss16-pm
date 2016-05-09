"use strict";
/**
 * 
 */
// global namespace
var dke = dke || {};

dke.Application = function() {
	// processinformation
	this._processData = null;
	// modelselection
	this._selection = [];
	this._mergedModel = null;
}

/*
 * Initialisierung mit JSON Daten
 */
dke.Application.prototype.init = function(json) {

	this._processData = {
		id : json.id,
		name : json.name,
		eventTypes : json.eventTypes,
		models : [],
		modelIds : {}
	};

	// Model Objekte anlegen
	this._processData.models = json.models.map(function(obj) {
		console.log("init", obj);
		return new dke.Model(obj);
	});
	// Index schreiben
	this._processData.models.forEach(function(obj, idx) {
		this._processData.modelIds[obj.footprint] = obj;
	}, this);

	this.selectModels(0);
	// select idx 0
	// this._selection = [ 0 ];
	// this._mergedModel = processData.models[0];
	// dke.ModelUtils.merge(this._processData.models.slice(0, 2));
}

dke.Application.prototype.getEventTypes = function() {
	return this._processData.eventTypes;
}

dke.Application.prototype.getMergedModel = function() {
	return this._mergedModel;
}

dke.Application.prototype.selectModels = function(modelIds) {
	console.log("arguments", arguments.length, arguments);
	var mIds = [];
	for (var i = 0; i < arguments.length; i++) {
		if (Array.isArray(arguments[i])) {
			Array.prototype.push.apply(mIds, arguments[i]);
		} else {
			mIds.push(arguments[i]);
		}
	}

	console.log("arguments", mIds);

	var models = [];

	mIds.forEach(function(obj, idx) {
		if (!isNaN(obj)) {
			var idx = parseInt(obj);
			console.log('INT', idx);
			if (this._processData && this._processData.models[idx]) {
				models.push(this._processData.models[idx]);
			}
		} else {
			console.log('String', obj);
			if (this._processData && this._processData.modelIds[idx]) {
				models.push(this._processData.modelIds[idx]);
			}
		}
	}, this);

	console.log("select", models);
	// TODO Update lock
	this._selection = mIds;
	this._mergedModel = dke.ModelUtils.merge(models);

	return models.length;
}

// debug
dke.Application.prototype.print = function() {
	for (i = 0; i < this._processData.eventTypes.length; i++) {
		console.log('type: ', i, ' ', this._processData.eventTypes[i]);
	}
}

// debug
dke.Application.prototype.classcheck = function() {
	console.log('check:', Object.prototype.toString
			.call(this._processData.models[0]));
	var m = new dke.Model();
	m.foo();
	console.log('check:', m);
	var jm = $.extend(new dke.Model(), this._processData.models[0]);
	jm.foo();
	console.log('check:', jm);
	var om = this._processData.models[0];
	om.om.foo();
}

/*
 * Model Utils
 */
dke.ModelUtils = function() {
}
dke.ModelUtils.merge = function(models) {

	if (models == null)
		return null;

	var mArr = Array.isArray(models) ? models : [ models ];

	var target = new dke.Model();
	console.log('target', target);

	target.footprints = [];

	var avgSeconds = 0;
	var maxSeconds = [];
	var minSeconds = [];

	var netIds = [];
	var netNodes = {};
	var netEdges = {};

	mArr.forEach(function(e) {
		console.log('a-merge: ', e);

		target.processId = e.processId;

		target.footprints.push(e.footprint);

		avgSeconds += moment.duration(e.avgDuration).asMilliseconds()
				* e.noCases;
		maxSeconds.push(moment.duration(e.maxDuration).asMilliseconds());
		minSeconds.push(moment.duration(e.minDuration).asMilliseconds());

		target.noCases += e.noCases;

		// TODO:processNet
		if (e.processNet) {
			netIds.push(e.footprint);
			// nodes
			e.processNet.nodes.forEach(function(n) {
				var tmp = netNodes[n.id];
				if (!tmp) {
					tmp = netNodes[n.id] = {
						id : n.id,
						data : {}
					};
				}
				console.log(e.footprint, 'node', n.id, n.data, tmp.data);
				Object.keys(n.data).forEach(function(key, index) {
					// key: the name of the object key
					// index: the ordinal position of the key within the
					console.log("prop", key, n.data[key]);
					if (key == 'count') {
						tmp.data[key] = n.data[key] + (tmp.data[key] || 0);
					} else {
						tmp.data[key] = n.data[key];
					}
				});
				console.log(' tmp', tmp.id, tmp.data);

			});
			// edges
			e.processNet.edges.forEach(function(n) {
				var id = n.source + '-' + n.target;
				var tmp = netEdges[id];
				if (!tmp) {
					tmp = netEdges[id] = {
						source : n.source,
						target : n.target,
						data : {}
					};
				}
				console.log('edge', id, n.data);
				Object.keys(n.data).forEach(
						function(key, index) {
							// key: the name of the object key
							// index: the ordinal position of the key within the
							console.log("prop", key, n.data[key]);
							if (key == 'count') {
								tmp.data[key] = n.data[key]
										+ (tmp.data[key] || 0);
							} else if (key == 'duration') {
								var count = n.data.count || 1;
								tmp.data[key] = moment.duration(n.data[key])
										.asMilliseconds()
										* count + (tmp.data[key] || 0);
							} else {
								tmp.data[key] = n.data[key];
							}
						});
				console.log(' tmp', tmp.id, tmp.data);

			});
		}

	});

	target.footprint = target.footprints.join(' ');

	console.log('durations', avgSeconds, maxSeconds, minSeconds);

	target.avgDuration = moment.duration(avgSeconds / target.noCases);
	target.maxDuration = moment.duration(Math.max.apply(null, maxSeconds));
	target.minDuration = moment.duration(Math.max.apply(null, minSeconds));

	target.processNet = {
		id : netIds,
		nodes : [],
		edges : []
	};
	Object.keys(netNodes).forEach(function(key, idx) {
		console.log('copy Node', key, idx, netNodes[key].data);
		target.processNet.nodes.push(netNodes[key]);
	});
	Object.keys(netEdges).forEach(
			function(key, idx) {
				console.log('copy Edge', key, idx, netEdges[key].data);
				var tmp = netEdges[key];
				if (tmp.data.duration) {
					tmp.data.duration = moment.duration(tmp.data.duration
							/ tmp.data.count);
				}
				target.processNet.edges.push(tmp);
			});

	// TODO: muss der graph update event handler machen
	// target.graph = dke.ModelUtils.createGraph(target.processNet);
	console.log('target', target);

	// for( i = 0; i < m.length; i++) {
	// var tmp = m[i];
	// console.log('merge: ', tmp);
	// }

	return target;
}

// TODO: metadaten includieren, erst auf summierte daten ausführen
dke.ModelUtils.createGraph = function(json) {
	// dagre implementierung
	var g = new dagreD3.graphlib.Graph({
		"directed" : true,
		"multigraph" : false,
		"compound" : false
	}).setGraph({});
	json.nodes.forEach(function(n) {
		g.setNode(n.id, n.data);
	});
	json.edges.forEach(function(e) {
		g.setEdge(e.source, e.target, e.data);
	});
	return g;
}

dke.Model = function(json) {

	this.processId = json && json.processId;

	this.footprint = json && json.footprint;

	this.avgDuration = moment.duration(json && json.avgDuration);
	this.maxDuration = moment.duration(json && json.maxDuration);
	this.minDuration = moment.duration(json && json.minDuration);
	//
	this.noCases = (json && json.noCases) ? json.noCases : 0;

	this.processNet = (json && json.processNet) ? json.processNet : null;

	// this.graph = (json && json.graph) ? json.graph : null;

}

dke.Model.prototype.foo = function() {
	console.log("I'm FOO");
}