
angular.module('history',['indexList', 'ngSanitize']);


angular.module('history').service('dataService', ['$http', '$q', function($http, $q){
    var deferObject,
        myMethods = {

            getPromise: function() {
                var promise       =  $http.get('/api/monitoring/rangehistory', {params: {"headless": true}}),
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

angular.module('history').controller('HistoryController', ['$scope', 'dataService', '$compile', '$http', '$timeout', function ($scope, dataService, $compile, $http, $timeout) {

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

    //rangehistory controller logic

    $scope.SendData = function (){
        // loading screen
        $scope.loading = true;


        var date = $('input[name="daterange"]').val();
        createLabelString(date);
        initVars();

        // clear content for new request
        var node = document.getElementById("computeNodes");
        while (node.hasChildNodes()) {
            node.removeChild(node.lastChild);
        }
        var appnode = document.getElementById("appDataList");
        while (appnode.hasChildNodes()) {
            appnode.removeChild(appnode.lastChild);
        }
        var clusternode = document.getElementById("clusterDataList");
        while (clusternode.hasChildNodes()) {
            clusternode.removeChild(clusternode.lastChild);
        }

        // get cnListhistory from hbase
        $http.get('/api/monitoring/getCNListHistory/'+date)
            .success(function(response, status){
                clusterCNData = response['cns']['cnAmount']

                initappOverview();
                initAppData(response['apphistory']);

                initClusterOverview();

                //console.log(response);
                initCNOverview(response);
                initClusterData();

                $scope.loading = false;
            })
            .error(function(response, status){
                alert("ERROR! No Data recieved!\n"+"Status: "+status+'\n'+'Response: '+response);
                $scope.loading = false;
            });

    };


}]);

angular.module('history').directive('historyOverview', ['$compile', function ($compile) {
    return {
        templateUrl: 'history.template.html',
        //restrict: 'A',
        link: function(scope, element, attrs){

        }
    };
}]);

angular.module('history').directive('compile', ['$compile', function ($compile) {
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


                //recompile js
                initDatepicker()





                // compile the new DOM and link it to the current
                // scope.
                // NOTE: we only compile .childNodes so that
                // we don't get into infinite loop compiling ourselves
                $compile(element.contents())(scope);

            }

        );
    };
}]);

