
/*
angular.module('AjaxApp',['indexList', 'ngSanitize']);
*/

angular.module('AjaxApp').service('dataService', ['$http', '$q', function($http, $q){

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


function ClusterController($scope, dataService, $timeout, $http) {


    var self = this;

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

}


// Register `` component, along with its associated controller and template

angular.module('AjaxApp').component('clusterOverview', {
    templateUrl: 'cluster.template.html',
    //templateUrl: '/api/cluster/cluster.template.html',
    controller: ClusterController

});


/*
angular.module('cluster').controller('ClusterController', ['$scope', 'dataService', '$compile', function ($scope, dataService, $compile) {

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

}]);

angular.module('cluster').directive('clusterOverview', ['$compile', function ($compile) {
    return {
        templateUrl: 'cluster.template.html',
        //restrict: 'A',
        link: function(scope, element, attrs){
            //main();
            console.log('"link" function inside directive vk called, "element" param is: ', element)
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


                    // compile the new DOM and link it to the current
                    // scope.
                    // NOTE: we only compile .childNodes so that
                    // we don't get into infinite loop compiling ourselves
                    $compile(element.contents())(scope);
                    main();
                }

            );
        };
    }]);




/*
 .directive('clusterOverview', function (){
 return{
 //templateUrl: 'cluster.template.html'
 //templateUrl: '/api/cluster/cluster.template.html',
 //controller: ClusterController
 template: 'cluster.template.html'

 }*/
