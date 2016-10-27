
angular.module('cnsnapshots',['indexList', 'ngSanitize', 'ngRoute']);


angular.module('cnsnapshots').service('dataService', ['$http', '$q', '$routeParams', '$location', function($http, $q, $routeParams, $location){

    var usrURL = $location.$$absUrl;

    var computenodeName = usrURL.substr(usrURL.lastIndexOf('/') + 1);


    var deferObject,
        myMethods = {

            getPromise: function() {
                var promise       =  $http.get('/api/monitoring/snapshots/' + computenodeName, {params: {"headless": true}}),
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

angular.module('cnsnapshots').controller('SingleComputenodeController', ['$scope', 'dataService', '$compile', '$http', '$timeout', '$location', function ($scope, dataService, $compile, $http, $timeout, $location) {

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


    //singlecomputenode.js controller logic part

    var usrURL = $location.$$absUrl;
    var urlCompute = '/api/monitoring/' +usrURL.substr(usrURL.lastIndexOf('/') + 1);
    var timeout = "";
    var poller = function(){
        $http.get(urlCompute).success(function(response){
            $scope.es = response;
            chartUpdate(response);
            timeout = $timeout(poller, 10000);
        });
    };


    var urlVirtual =  '/api/monitoring/virtual/' + usrURL.substr(usrURL.lastIndexOf('/') + 1);
    var timeout2 = "";
    var poller2 = function(){
        $http.get(urlVirtual).success(function(response2){
            $scope.e = response2;
            chartUpdateVM(response2);
            timeout2 = $timeout(poller2, 10000);
        });
    };
    poller();
    poller2();

}]);

angular.module('cnsnapshots').directive('singleComputenode', ['$compile', function ($compile) {
    return {
        templateUrl: 'singlecomputenode.template.html',
        //restrict: 'A',
        link: function(scope, element, attrs){

        }
    };
}]);

angular.module('cnsnapshots').directive('compile', ['$compile', function ($compile) {
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
                //buildCharts();
                buildSingleCN();


                // compile the new DOM and link it to the current
                // scope.
                // NOTE: we only compile .childNodes so that
                // we don't get into infinite loop compiling ourselves
                $compile(element.contents())(scope);

            }

        );
    };
}]);

