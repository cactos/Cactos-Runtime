angular.module('optconfig').factory('CactoOptPlacementConfigurationOption', function($resource){
	return $resource('/api/cactoopt/config/placement/options/:id');
});

angular.module('optconfig').factory('CactoOptOptimisationConfigurationOption', function($resource){
	return $resource('/api/cactoopt/config/optimisation/options/:id');
});

angular.module('optconfig').factory('CactoOptConfigurationStore', function($resource){
	return $resource('/api/cactoopt/config/:option');
});


function OptConfigController($scope, 
			CactoOptPlacementConfigurationOption, 
			CactoOptOptimisationConfigurationOption,
			CactoOptConfigurationStore) {
  var ctrl = this;

  this.placement = {selection : {}};
  this.placement.current = CactoOptConfigurationStore.get({option : "placement"},
		  function(res){
			  ctrl.placement.selection.main = ctrl.placement.current.main;
			  ctrl.placement.selection.sub = ctrl.placement.current.sub;
		  });
  this.placement.selection.main = "causa";
  this.placement.selection.sub = "";
  this.placement.options = CactoOptPlacementConfigurationOption.query();

  this.optimisation = { selection: {}};
  this.optimisation.selection.main = "LinKernighan";
  this.optimisation.selection.sub = "";
  this.optimisation.current = CactoOptConfigurationStore.get({option : "optimisation"},
		  function(res){
			  ctrl.optimisation.selection.main = ctrl.optimisation.current.main;
			  ctrl.optimisation.selection.sub = ctrl.optimisation.current.sub;
		  });
  this.optimisation.options = CactoOptOptimisationConfigurationOption.query();

  this.saveConfiguration = function(configOption) {
	  alert(configOption + ": " + ctrl[configOption].selection['main'] + 
			  " --- " + ctrl[configOption].selection['sub']);

	  CactoOptConfigurationStore.save({option : configOption}, 
					  this[configOption].selection);
  };

  this.changeSubConfig = function(type, value) {
	  if('optimisation' === type || 'placement' === type) {
		ctrl[type].selection.sub = value;	
	  } else {
		alert("unknown type: " + type);
	  }
  };
}

// Register `` component, along with its associated controller and template
angular.module('optconfig').component('configOptions', {
         templateUrl: 'optconfig/optconfig.template.html',
         controller: OptConfigController
});
