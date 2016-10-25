angular.module('datacentre', ['computenodes', 'metrics', 'indexList',]);

angular.module('datacentre').controller('DcOverviewController', function($scope) {
		$scope.cnode = ["not set"];
		$scope.updated = function(cnode) {
			$scope.cnode = cnode;
		};
	}
);

