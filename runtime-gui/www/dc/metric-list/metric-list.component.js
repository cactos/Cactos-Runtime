
function MetricsListController($scope, Metric, metricsState) {
	
	this.computenodename = metricsState.computenodeHolder;
	this.metricList = metricsState.metricListHolder;
	self = this;

	this.nameForValue = function(type, value){
		return metricsState.nameForValue(type, value);
	};

	this.loadInForm = function(metric) {
		metricsState.formMetric.metric = metric;
	};

	this.hasSensorConfig = function(metric) {
		if(Object.keys != null) {
			var keys = Object.keys(metric.sensorConfiguration);
			return keys.length > 0;
		}
		alert("not possible");
		return true;
	};

	this.deleteMetric = function(metric) {
		metricsState.deleteMetric(metric);
	};
}

// Register `` component, along with its associated controller and template
angular.module('metrics').component('metricList', {
         templateUrl: '/dc/metric-list/metric-list.template.html',
         controller: MetricsListController,
});
