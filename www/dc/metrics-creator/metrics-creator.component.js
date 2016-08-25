
function MetricsFormController($scope, Metric, metricsState) {
 var initialised = false;
 var self = this;
 this.clearForm = metricsState.clearForm;
 this.formMetric = metricsState.formMetric;
 this.tempKV = {"key" : null, "value" : null};
 this.saveMetric = function() {
	 metricsState.saveMetric(this.computenodename);
 };

 this.available = metricsState.availableConfig;
 this.computenodename = "abcd"; 

 this.$postLink = function() {
	initialised = true;
 };

 this.$onChanges = function(changesObj) {
	if(initialised === false) return;
	if(changesObj.cnode) {
		updateHost(changesObj.cnode.currentValue)
	}
 };

 this.addTempKV = function() {
	 var tempKV = this.tempKV;
	 metricsState.addKeyValue(tempKV.key, tempKV.value);
	 this.tempKV.key = null;
	 this.tempKV.value = null;
 };

 this.deleteSensorConfig = function(key) {
	metricsState.deleteKeyValue(key);
 };

 var updateHost = function(cnode) {
	 //self.clearForm();
	 self.computenodename = cnode[0];
	 //alert("setting computenodename: " + cnode[0]);
	 // self.computenodes = Metric.query({computenode : cnode[0]});
	 metricsState.reinitMetriclist(cnode[0]);
 };

 this.select = function(computenode) {
	 this.selected = computenode;
	 this.onSelect({node : computenode});
 };

}

// Register `` component, along with its associated controller and template
angular.module('metrics').component('metricsCreator', {
         templateUrl: '/dc/metrics-creator/metrics-creator.template.html',
         controller: MetricsFormController,
	 bindings: {
		 onSelect: '&',
		 cnode: '<'
	 }
});
