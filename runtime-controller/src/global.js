
var config = {
	'cactoopt' 	: { "status" : "on" },
        'monitors' 	: { "status" : "on" },
        'apps' 		: { "status" : "on" },

	'metrics'	: {
		"status" : "off",
		"url"	 : "http://example.com",
	},
	'cluster' 	: { "status" : "on" },
	'nodes' 	: { "status" : "on" },
	'vms' 	: { "status" : "on" },
	'history' 	: { "status" : "on" },
    };

var staticConf = {
	'cactoopt' : { 
		"path" : "/cactoopt/index.html",
		"name" : "CactoOpt Configurator"
	},
	'apps' : {
		"path" : "/apps/index.html",
		"name" : "Application Controller",
	},
	'monitors' : {
		"path" : "/dc/index.html",
		"name" : "Datacentre Controller",
	},

	'metrics' : {
		"path" : "/metrics/index.html",
		"name" : "Monitoring Overview",
	},
	'cluster' : {
		"path" : "/dashboard/cluster/index.html",
		"name" : "Cluster Overview",
	},
	'nodes' : {
		"path" : "/dashboard/computenodes/index.html",
		"name" : "Computenodes",
	},
	'vms' : {
		"path" : "/dashboard/vms/index.html",
		"name" : "Virtual Machines",
	},
	'history' : {
		"path" : "/dashboard/rangehistory",
		"name" : "History",
	},
};


var copyConfig = function() {
	return {
		"config" : JSON.parse(JSON.stringify(config)),
		"static" : JSON.parse(JSON.stringify(staticConf)),
	};
};


module.exports = {
     getConfig : copyConfig,
}; 

