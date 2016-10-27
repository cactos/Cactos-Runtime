angular.module('AjaxApp',['indexList', 'ngSanitize']);

function WrapperController($scope, dataService, $timeout) {


    console.log("called wrapper controller");

    /*
    var self = this;

    self.$postLink = function () {
        var el = document.getElementById("vmLineChart");
        console.info(el);
    }
    */
    this.$postLink = function () {
        var el = document.getElementById("vmLineChart");
        console.info(el);
    }

    this.$onChanges = function (changesObj) {
        console.log(changesObj);
    };


}


// Register `` component, along with its associated controller and template

angular.module('AjaxApp').component('wrapperOverview', {
    templateUrl: 'wrapper.template.html',
    //templateUrl: '/api/cluster/cluster.template.html',
    controller: WrapperController

});
