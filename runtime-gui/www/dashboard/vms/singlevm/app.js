
angular.module('singlevm',['indexList', 'ngSanitize']);


angular.module('singlevm').service('dataService', ['$http', '$q', '$location', function($http, $q, $location){


    var usrURL = $location.$$absUrl;

    var vmId = usrURL.substr(usrURL.lastIndexOf('/') + 1);

    var deferObject,
        myMethods = {

            getPromise: function() {
                var promise       =  $http.get('/api/monitoring/snapshots/vm/' + vmId, {params: {"headless": true}}),
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

angular.module('singlevm').controller('SingleVmController', ['$scope', 'dataService', '$compile', '$http', '$timeout', '$location', function ($scope, dataService, $compile, $http, $timeout, $location) {

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

        },
        // OnFailure function
        function(response) {
            alert(response);
            $scope.error = true;
        }
    )


    //singlevm.js controller logic part
    var usrURL = $location.$$absUrl;
    var url = '/api/monitoring/virtual/' + usrURL.substr(usrURL.lastIndexOf('/') + 1);
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

angular.module('singlevm').directive('singleVm', ['$compile', function ($compile) {
    return {
        templateUrl: '/dashboard/vms/singlevm/singlevm.template.html',
        //restrict: 'A',
        link: function(scope, element, attrs){

        }
    };
}]);

angular.module('singlevm').directive('compile', ['$compile', function ($compile) {
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
                buildSingleVM();


                // compile the new DOM and link it to the current
                // scope.
                // NOTE: we only compile .childNodes so that
                // we don't get into infinite loop compiling ourselves
                $compile(element.contents())(scope);

            }

        );
    };
}]);

