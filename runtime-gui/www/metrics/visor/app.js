/***
 * Excerpted from "Seven Web Frameworks in Seven Weeks",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/7web for more book information.
***/
(function(app) {
  // define factories, controllers, etc. on app
  app.factory("Metric", function($resource) {
    return $resource("/api/monitor/gaston/monitors/:id", {id:"@id"});
  });
  app.factory("metrics", function(Metric) {
    return Metric.query();
  });
  app.factory("deleteMetric", function(metrics) {
    return function(metric) {
      var index = metrics.indexOf(metric);
      metrics.$delete();
      metrics.splice(index, 1);
    };
  });
  app.service("state", function(Metric) {
    this.formMetric = {metric:new Metric()};
    this.formMetric.metric.interval = {'period' : 1, 'unit' : 'MINUTES'};
    this.formMetric.metric.type = 'sensor';
    this.formMetric.metric.name = 'unknown';

    this.clearForm = function() {
      this.formMetric.metric = new Metric();
      this.formMetric.metric.interval = {'period' : '1', 'unit' : 'MINUTES'};
    };
  });
  app.factory("editMetric", function(state) {
    return function(metric) {
      state.formMetric.metric = metric;
    };
  });
  app.factory("saveMetric", function(metrics, state) {
    return function(metric) {
      if (!metric.id) {
        metrics.push(metric);
      }
      metric.$save();
      state.clearForm();
    };
  });

  app.controller("MetricListController",
    function($scope, metrics, deleteMetric, editMetric) {
      $scope.metrics = metrics;
      $scope.deleteMetric = deleteMetric;
      $scope.editMetric = editMetric;
    }
  );
  app.controller("MetricFormController",
    function($scope, state, metrics, saveMetric) {
      $scope.formMetric = state.formMetric;
      $scope.saveMetric = saveMetric;
      $scope.clearForm = state.clearForm;
    }
  );
})(
  angular.module("App_visor", ["ngResource"])
);
