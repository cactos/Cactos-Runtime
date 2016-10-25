
angular.module('metrics').factory('Metric', function($resource){
        return $resource('/api/dc/monitor/:computenode/monitors/:uuid', {uuid : "@uuid"},
			{
			  'update': { method:'PUT' }}
			);
});

angular.module('metrics').service("metricsState", function(Metric, $filter) {
   self = this;
   this.formMetric = {};
   this.computenodeHolder = {node : "not set"};
   this.metricListHolder = {list : []};

   this.clearForm = function() {
        this.formMetric.metric = new Metric();
        this.formMetric.metric.interval = {'period' : 10, 'timeUnit' : 'MINUTES'};
        this.formMetric.metric.sensorClassName = 
		"de.uniulm.omi.cloudiator.visor.sensors.SystemMemoryUsageSensor";
        this.formMetric.metric['@type'] = 'sensor';
        this.formMetric.metric.metricName = 'set automatically';
	this.formMetric.metric.sensorConfiguration = {};
   };

   this.addKeyValue = function(key, value){
	   this.formMetric.metric.sensorConfiguration[key] = value;
   };

  this.deleteKeyValue = function(key) {
	  this.formMetric.metric.sensorConfiguration[key] = undefined;
	  delete this.formMetric.metric.sensorConfiguration[key];
  };

   this.saveMetric = function(node) {
      var metric = this.formMetric.metric;
      if (!metric.uuid) {
	      // new element
	     metric.metricName = node + "-" + 
		     this.nameForValue('sensors', metric.sensorClassName) +
		     "-" + (new Date().getTime());
             this.metricListHolder.list.push(metric);
      	     this.formMetric.metric.$save({computenode : node});
      } else {
      	     this.formMetric.metric.$update({computenode : node});
      }
      this.clearForm();
   };

   this.availableConfig = { //fixme: get from remote //
         sensors : [{
                value : "de.uniulm.omi.cloudiator.visor.sensors.SystemCpuUsageSensor",
                name : "CPU",
         },{
                value : "de.uniulm.omi.cloudiator.visor.sensors.SystemMemoryUsageSensor",
                name : "RAM",
         }],
	units : [{
		value : "MINUTES",
		name : "minutes",
	},{
		value : "SECONDS",
		name : "seconds",
	},{
		value : "HOURS",
		name : "hours",
	}],
		types : [{
			"name" : "Sensor",
			"value" : "sensor",
	},{
			"name" : "Reporter",
			"value" : "Push",
	}],
   };

  this.nameForValue = function(type, value) {
	var x = this.availableConfig[type];
	var y = $filter('filter')(x, {"value" : value});
	if(y.length != 1) return "unknown " + JSON.stringify(y);
	return y[0].name;
  };

  this.reinitMetriclist = function(cnode) {
	var self = this;
    	this.metricListHolder.list = Metric.query({computenode : cnode});
	this.computenodeHolder.node = cnode;
  };

  this.deleteMetric = function(metric) {
	var index = this.metricListHolder.list.indexOf(metric);
	metric.$delete({computenode : this.computenodeHolder.node});
	this.metricListHolder.list.splice(index, 1);
  };

   this.clearForm();
});


