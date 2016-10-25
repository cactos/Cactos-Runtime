
function ApplistController($routeParams, colAccessor, $resource, colLogin, colIdGetter){

	this.applicationName = $routeParams.appName;
	this.applications = [];
	self = this;

	this.applications = colLogin.tokenP.then(function(token){
		self.applications = colAccessor.AuthColosseum.query({'path' : 'application'});
	});

	this.getId = function(object) {
		return colIdGetter.getColosseumId(object);
	}
}

angular.module('applist').component('applicationList', {
	                 templateUrl: 'app-list/app-list.template.html',
	                   controller: ApplistController
});


