
var config = {
	'cactoopt' 	: { "status" : "on" },
        'monitors' 	: { "status" : "on" },
        'apps' 		: { "status" : "on" },
	'metrics'	: {
		"status" : "on",
		"url"	 : "http://example.com",
	},
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

