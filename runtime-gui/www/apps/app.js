// Define the `phonecatApp` module
angular.module('appmanager', [
     'ngRoute', 'ngResource', 'applist', 'appdetails', 'indexList'
     ]);

angular.
  module('appmanager').
  config(['$locationProvider', '$routeProvider',
    function config($locationProvider, $routeProvider) {
      $locationProvider.hashPrefix('!');

      $routeProvider.
        when('/apps/', {
          template: '<application-list></application-list>'
        }).
        when('/apps/:appName', {
          template: '<application-list></application-list><application-details></application-details>'
        }).
        otherwise('/apps/');
    }
  ]);

