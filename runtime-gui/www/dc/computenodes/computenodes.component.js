
angular.module('computenodes').factory('Computenode', function($resource){
	// return $resource('/api/dc/computenodes/');
	return $resource('/api/dc/computenodes/CNSnapshot/computenode*/meta:state');
});

function ComputenodesController($scope, Computenode) {
	this.selected = null;
	var self = this;
	this.computenodes = Computenode.get({}, function(res){
		var result = [];
		var response = res['Row'];
		angular.forEach(response, function(value){
			var cellvalue = null;
			angular.forEach(value['Cell'], function(vvalue){
				var col = atob(vvalue['column']);
				col === 'meta:state' ? 
					cellvalue = atob(vvalue['$']) :
					cellvalue = "unknown";
			});
			result.push([
				atob(value['key']), cellvalue
			]);
		});
		// remove for production environment //
		result.push(['gaston', 'running']);
		result.push(['julia', 'off']);
		result.push(['romeo', 'maintenance']);
		result.push(['saeco', 'failure']);
		self.computenodes = result;
	 }, function(res){
		 alert("error: " + res);
	 });

	this.select = function(computenode) {
		 this.selected = computenode;
		 this.onSelect({node : computenode});
	 }
}

// Register `` component, along with its associated controller and template
angular.module('computenodes').component('computeNodes', {
         templateUrl: '/dc/computenodes/computenodes.template.html',
         controller: ComputenodesController,
	 bindings: {
		 onSelect: '&'
	 }
});
