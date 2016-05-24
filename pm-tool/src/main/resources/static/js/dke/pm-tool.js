"use strict";
/**
 * 
 */
// global namespace
var dke = dke || {};

dke.Application = function() {
	// event handling
	this._observer = {};

	// processinformation
	this._processData = null;
	// modelselection
	this._selection = [];
	this._mergedModel = null;
}

dke.Application.prototype = {
	/*
	 * Initialisierung mit JSON Daten
	 */
	init : function(json) {

		this._processData = {
			id : json.id,
			name : json.name,
			eventTypes : json.eventTypes,
			models : [],
			modelIds : {},
			totalCases : 0
		};

		// Model Objekte anlegen
		this._processData.models = json.models.map(function(obj) {
			console.log("init", obj);
			return new dke.Model(obj);
		});
		// Index schreiben, cases aufsummieren
		this._processData.models.forEach(function(obj, idx) {
			this._processData.modelIds[obj.footprint] = obj;
			this._processData.totalCases += obj.noCases;
		}, this);

		this._fire(dke.Event.INIT, new dke.Event(dke.Event.INIT, this));

		this.selectModels(0);
	},

	getEventTypes : function() {
		return this._processData.eventTypes;
	},

	getMergedModel : function() {
		return this._mergedModel;
	},

	selectModels : function() {
		console.log("arguments", arguments.length, arguments);
		
		//keine Daten vorhanden
		if (!this._processData) {
			return -1;
		}
		
		var models = [];
		if (typeof arguments[0] === 'function') {
			var fn = arguments[0];
			var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
			var data = this._processData;
			//filterfunction (element, index, array)
			models =  this._processData.models.filter(function(element, index, array){
				return fn.call(thisArg, element, index, array, data);
			});
		} else {
			//ids, footprints wurden übergeben
			var mIds = [];
			for (var i = 0; i < arguments.length; i++) {
				if (Array.isArray(arguments[i])) {
					Array.prototype.push.apply(mIds, arguments[i]);
				} else {
					mIds.push(arguments[i]);
				}
			}

			console.log("arguments", mIds);

			mIds.forEach(function(obj, idx) {
				if (!isNaN(obj)) {
					var idx = parseInt(obj);
					console.log('INT', idx);
					if (this._processData && this._processData.models[idx]) {
						models.push(this._processData.models[idx]);
					}
				} else {
					console.log('String', obj);
					if (this._processData && this._processData.modelIds[obj]) {
						models.push(this._processData.modelIds[obj]);
					}
				}
			}, this);
		}
		console.log("selected", mIds, models);
		// TODO Update lock
		// TODO mindestens ein model muss selektiert sein ...
		if (models.length > 0) {
			this._selection = models.map(function(obj) {
				return obj.footprint;
			});
			this._mergedModel = dke.ModelUtils.merge(models);

			this._fire(dke.Event.SELECTION_UPDATE, new dke.Event(
					dke.Event.SELECTION_UPDATE, this));
		}
		return models.length;
	},

	getSelectedIds : function() {
		return this._selection;
	},

	/*
	 * eventhandling
	 */
	on : function(type, fn, context) {
		type = type || dke.Event.ANY;
		fn = typeof fn === 'function' ? fn : context[fn];

		var observer = this._observer[type] || (this._observer[type] = []);

		observer.push({
			fn : fn,
			context : context || this
		});
	},

	remove : function(type, fn, context) {
		type = type || dke.Event.ANY;
		fn = typeof fn === 'function' ? fn : context[fn];

		var observer = this._observer[type] || (this._observer[type] = []);

		this._observer[type] = observer.filter(function(item) {
			if (item.fn !== fn) {
				return item;
			}
		});
	},

	_fire : function(type, event) {
		type = type || dke.Event.ANY;
		console.log('fireEvent', type, event);

		var fired = {};

		this._observer[type]
				&& this._observer[type].forEach(function(obj, idx) {
					obj.fn.call(obj.context, event);
					fired[obj.fn] = obj;
				});
		// any
		this._observer[dke.Event.ANY]
				&& this._observer[dke.Event.ANY].forEach(function(obj, idx) {
					fired[obj.fn] || obj.fn.call(obj.context, event);
				});

	}
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
 * Event
 */
dke.Event = function(type, source, args) {
	this._type = type;
	this._source = source;
	this._args = args;
}

dke.Event.ANY = 'any';
dke.Event.INIT = 'init';
dke.Event.SELECTION_UPDATE = 'selectionUpdate';

dke.Event.prototype = {
	getType : function() {
		return this._type;
	},
	getSource : function() {
		return this._source;
	},
	getArgs : function() {
		return this._args;
	}
}
Object.freeze(dke.Event);

/*
 * Model Utils
 */
dke.ModelUtils = {
	/*
	 * merge multiple models
	 */
	merge : function(models) {

		console.time("merger");
		
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
			console.group(e.footprint);
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
								// index: the ordinal position of the key within
								// the
								console.log("prop", key, n.data[key]);
								if (key == 'count') {
									tmp.data[key] = n.data[key]
											+ (tmp.data[key] || 0);
								} else if (key == 'duration') {
									var count = n.data.count || 1;
									tmp.data[key] = moment
											.duration(n.data[key])
											.asMilliseconds()
											* count + (tmp.data[key] || 0);
								} else {
									tmp.data[key] = n.data[key];
								}
							});
					console.log(' tmp', tmp.id, tmp.data);

				});
			}

			console.groupEnd();
		});

		target.footprint = target.footprints.join(' ');

		console.log('durations', avgSeconds, maxSeconds, minSeconds);

		target.avgDuration = moment.duration(avgSeconds / target.noCases);
		target.maxDuration = moment.duration(Math.max.apply(null, maxSeconds));
		target.minDuration = moment.duration(Math.min.apply(null, minSeconds));

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
						//duration auf m genau ...
						tmp.data.duration = moment.duration(Math.trunc((tmp.data.duration
								/ tmp.data.count) / 60000)*60000);
					}
					target.processNet.edges.push(tmp);
				});

		// TODO: muss der graph update event handler machen
		// target.graph = dke.ModelUtils.createGraph(target.processNet);
		console.log('target', target);

		console.timeEnd("merger");
		
		return target;
	},

	// TODO: metadaten includieren, erst auf summierte daten ausführen
	createGraph : function(json) {
		// dagre implementierung
		var g = new dagreD3.graphlib.Graph({
			"directed" : true,
			"multigraph" : false,
			"compound" : false
		}).setGraph({});
		json.nodes.forEach(function(n) {
			if (n.data.count) {
				n.data.label += ' ' + n.data.count;
			}
			g.setNode(n.id, n.data);
		});
		json.edges.forEach(function(e) {
			e.data.label += ' ' + e.data.count;
			if (e.data.duration) {
				e.data.label += ' ' + e.data.duration.toISOString();
			}
			g.setEdge(e.source, e.target, e.data);
		});
		return g;
	}
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

}

dke.Model.prototype.foo = function() {
	console.log("I'm FOO");
}