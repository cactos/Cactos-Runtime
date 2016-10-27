
angular.module('indexList').controller('IndexListController', function($scope, $resource) {
	$scope.metric = {};
	var self = this;
	this.globalConfig = $resource('/api/global/config').get({}, {}, function(res){

			if(self.globalConfig['config']['metrics']['status'] == 'on'){
				$scope.values.metricUri = self.globalConfig['config']['metrics']['url'];
			}

	});

	$scope.values = {};
	$scope.values.metricUri = "";
	
});

angular.module('indexList').component('indexListComponent', {
         templateUrl: '/global/global.template.html',
         controller: 'IndexListController',
});
