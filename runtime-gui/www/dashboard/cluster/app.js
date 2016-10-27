
angular.module('cluster',['indexList', 'ngSanitize']);


angular.module('cluster').service('dataService', ['$http', '$q', function($http, $q){
    var deferObject,
        myMethods = {

            getPromise: function() {
                var promise       =  $http.get('/api/monitoring/overview', {params: {"headless": true}}),
                    deferObject =  deferObject || $q.defer();

                promise.then(
                    // OnSuccess function
                    function(answer){
                        // This code will only run if we have a successful promise.
                        deferObject.resolve(answer);
                    },
                    // OnFailure function
                    function(reason){
                        // This code will only run if we have a failed promise.
                        deferObject.reject(reason);
                    });

                return deferObject.promise;
            }
        };

    return myMethods;

}]);

angular.module('cluster').controller('ClusterController', ['$scope', 'dataService', '$compile', '$http', '$timeout', function ($scope, dataService, $compile, $http, $timeout) {

    $scope.cluster = false;
    $scope.html = false;
    $scope.error = false;

    var askForPromise = dataService.getPromise();

    askForPromise.then(
        // OnSuccess function
        function(response) {
            $scope.cluster = response;
            $scope.html = response.data;


            $scope.success = true;
            //main();
        },
        // OnFailure function
        function(response) {
            alert(response);
            $scope.error = true;
        }
    )


    //cactos gui part
    var url = "/api/monitoring/ajax";
    var timeout = "";
    var poller = function(){

        $http.get(url).success(function(response){
            $scope.e = response;
            chartUpdate(response);
            timeout = $timeout(poller, 10000);

        });
    };
    poller();

}]);

angular.module('cluster').directive('clusterOverview', ['$compile', function ($compile) {
    return {
        templateUrl: 'cluster.template.html',
        //restrict: 'A',
        link: function(scope, element, attrs){

        }
    };
}]);

angular.module('cluster').directive('compile', ['$compile', function ($compile) {
    return function(scope, element, attrs) {
        scope.$watch(

            function(scope) {
                // watch the 'compile' expression for changes
                return scope.$eval(attrs.compile);
            },
            function(value) {
                // when the 'compile' expression changes
                // assign it into the current DOM
                element.html(value);

                //console.log(value);


                //call buildCharts to reload js content into the html
                buildCharts();



                // compile the new DOM and link it to the current
                // scope.
                // NOTE: we only compile .childNodes so that
                // we don't get into infinite loop compiling ourselves
                $compile(element.contents())(scope);

            }

        );
    };
}]);

