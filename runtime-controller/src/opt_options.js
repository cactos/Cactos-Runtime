
const optConfigDir = "/tmp/eu.cactosfp7.configuration/";


// placement options
var placement = {
 	"options" : [
                {"option" : "causa",
                         "name" : "Causa",
                         "sub-config" : "causa"},
                {"option" : "firstFit",
                        "name"  : "First Fit",
                        "sub-config" : ""},
                {"option" : "bestFitMemory",
                       "name" : "Best Fit (Memory)",
                       "sub-config" : ""},
                {"option" : "bestFitCpu",
                       "name" : "Best Fit (CPU)",
                       "sub-config" : ""},
                {"option" : "consolidation",
                       "name" : "Consolidation",
                       "sub-config" : ""},
        ],
	"files" : {
		"main" : {
			"filename" : "cactoopt_placement.cfg",
			"propertyName" : "placementName",
		},
		"causa" : {
			"filename" : "cactoopt_placement_causa.cfg",
			"propertyName" : "algorithm",
		},
	},
	"sub-options" : {
		"causa" : [
			{"option" : "NONE", "name" : "switched off"},
			{"option" : "BEST_FIT", "name" : "best fit"},
			{"option" : "LOAD_BALANCING_RAM", "name" : "load balanced (RAM)"},
			{"option" : "CONSOLIDATION_RAM", "name" : "consolidation (RAM)"},
			{"option" : "CONSOLIDATION", "name" : "consolidation"},
			{"option" : "FRAGMENTATION", "name" : "fragmentation"},
			{"option" : "ENERGY_EFFICIENCY", "name" : "energy efficiency"},
			{"option" : "MOLPRO_BEST_FIT", "name" : "Best-fit (Molpro)"},
			{"option" : "MOLPRO_LOAD_BALANCING_RAM", 
				"name" : "Load balanced (Molpro, RAM)"},
			{"option" : "MOLPRO_CONSOLIDATION_RAM", 
				"name" : "Consolidation (Molpro, RAM)"},
		],
	}
	// add other stuff for placement //
};

var optimisation = {
        "options" : [
                {"option" : "Causa",
                         "name" : "Causa",
                         "sub-config" : "causa"},
                {"option" : "Random",
                        "name"  : "Random",
                        "sub-config" : ""},
                {"option" : "LoadBalancing",
                       "name" : "Load Balancing",
                       "sub-config" : ""},
                {"option" : "Consolidation",
                       "name" : "Consolidation",
                       "sub-config" : ""},
                {"option" : "LinKernighan",
                       "name" : "Lin-Kernighan",
                       "sub-config" : ""},
        ],
	"files" : {
		"main" : {
			"filename" : "cactoopt_optimisationalgorithm.cfg",
			"propertyName" : "optimisationName",
		},
		"causa" : {
			"filename" : "cactoopt_opt_causa.cfg",
			"propertyName" : "algorithm",
		},
	},
	"sub-options" : {
		"causa" : [
                {"option" : "NONE", 
                         "name" : "switched off",
                         "sub-config" : ""},
                {"option" : "LOAD_BALANCING",
                        "name"  : "Load Balancing",
                        "sub-config" : ""},
                {"option" : "CONSOLIDATION",
                       "name" : "Consolidation",
                       "sub-config" : ""},
                {"option" : "ENERGY_EFFICIENCY",
                       "name" : "Energy Efficiency",
                       "sub-config" : ""},
                {"option" : "FRAGMENTATION",
                       "name" : "Fragmentation",
                       "sub-config" : ""},
                {"option" : "CP_LOAD_BALANCING",
                       "name" : "Load Balancing (CP)",
                       "sub-config" : ""},
                {"option" : "CP_CONSOLIDATION",
                       "name" : "Consolidation (CP)",
                       "sub-config" : ""},
                {"option" : "GD_CONSOLIDATION",
                       "name" : "Consolidation (GD)",
                       "sub-config" : ""},
                {"option" : "GD_LOAD_BALANCING",
                       "name" : "Load Balancing (GD)",
                       "sub-config" : ""},
                {"option" : "HIGH_TO_LOW_LOAD_BALANCING",
                       "name" : "Load Balancing (high to low)",
                       "sub-config" : ""},
                {"option" : "SINGLE_MIGRATION_LOAD_BALANCING",
                       "name" : "Load Balancing (single migration)",
                       "sub-config" : ""},
                {"option" : "SINGLE_MIGRATION_CONSOLIDATION",
                       "name" : "Consolidation (single migration)",
                       "sub-config" : ""},
        	],
	},
	// add other stuff for optimisation
};

var all = {
	"placement" : placement,
	"optimisation" : optimisation,
};

var findOptionDispatcher = function(branchName, optionValue) {
	console.log("searching option: '" + optionValue + "' in branch '" + branchName + "'");
 var tree = all[branchName];
 if(tree == null) return null;
 var options = tree["options"];
 if(options == null || !Array.isArray(options)) {
 	throw "tree has no options branch or not an array";
 }
 var optionLength = options.length;
 for(var i = 0; i < optionLength; i++) {
	var opt = options[i];
	console.log("checking " + opt.option + " against " + optionValue);
	if(opt.option == optionValue) {
		return opt['sub-config'];
	}
 }
 console.log("option " + optionValue + " not found in " + JSON.stringify(options));
 return false;
}

var findSubOptionDispatcher = function(branchName, subconfigName, subValue) {
 // assumption: findOptionDispatcher was called before
 // we can be sure that branchName exists
 var tree = all[branchName]['sub-options'][subconfigName];
 if(tree == null) { // make sure that subValue is also null or empty.
	if(subValue == null || subValue === "") {
		console.log("no sub value for " + branchName + " -> " + subconfigName);
		return true;
	} else {
		return false;
	}
 } else if(Array.isArray(tree)) {
	if(subValue == null || subValue === "") {
		return false;
	} else {
		var optionLength = tree.length;	
		for(var i = 0; i < optionLength; i++) {
			var opt = tree[i];
			if(opt.option === subValue) {
				return true;
			}
		}
		return false;
	}
 } else {
 	throw "tree has no options branch or not an array";
 }
};

module.exports = {
 placement : placement,
 optimisation  : optimisation,
 configDir : optConfigDir,
 findOption : findOptionDispatcher,
 findSubOption : findSubOptionDispatcher,
}; 

