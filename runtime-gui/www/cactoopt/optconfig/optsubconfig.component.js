angular.module('optconfig').factory('CactoOptSubConfigurationOption', function($resource){
	return $resource('/api/cactoopt/config/which/options/what/subconfig');
	// config/optimisation/options/causa/subconfig
});

//angular.module('optconfig').factory('CactoOptOptimisationConfigurationOption', function($resource){
//	return $resource('/api/cactoopt/config/optimisation/options/:id');
//});

function OptSubConfigController(CactoOptSubConfigurationOption, $resource) {
	var z = false;
	var resource = $resource;
	this.remoteUri = null;
	var remote = null;
	this.remoteElements = null;
	this.subSelection = ""; 

 this.subConfigAvailable = function() {
	 return this.subConfig && this.subConfig.length > 0
 };

 /*this.postprocessQuery = function() {
	alert(this.remoteElements);
	if((!this.subSelection || 
		this.subSelection.length == 0) &&
		this.remoteElements[0]) {
		this.subSelection = this.remoteElements[0]['option'];
		alert("subSelection: " + this.subSelection);
	}
 }*/

 this.computeRemote = function () {
	 if(this.subConfigAvailable() ) {
		this.remoteUri = '/api/cactoopt/config/' + 
			this.subConfigPrefix + '/options/' + 
			this.subConfig + '/subconfig';
		this.remote = resource(this.remoteUri);
		this.remoteElements = this.remote.query({}, this.postprocessQuery);
	 } else {
		this.remoteUri = null;
		remote = null;
		this.remoteElements = null;
		this.subSelection = null;
	 }
	// this.prefix + "---" + this.subConfig;
 };

/* this.$onInit = function (){
	this.computeDerived(); 
 };*/

 this.$postLink = function() {
	 z = true;
 };

 this.$onChanges = function (changesObj) {
	 if(z === false) return;

	 if(changesObj.subConfig) {
		 //this.prefix = changesObj.subConfig.currentValue;
		 // x.derived = changesObj.prefix.currentValue + "---" + this.subConfig;
		 this.computeRemote();
	 }
 };

 this.update = function() {
	 this.onChange({'type' : this.subConfigPrefix, 'value' : this.subSelection});
 }
}

// Register `` component, along with its associated controller and template
angular.module('optconfig').component('subConfigOptions', {
         templateUrl: 'optconfig/optsubconfig.template.html',
         controller: OptSubConfigController,
	 bindings : {
		 subConfig : '<',
		 subConfigPrefix: '<',
		 parentConfig: '<',
		 onChange: '&',
	 }
});
