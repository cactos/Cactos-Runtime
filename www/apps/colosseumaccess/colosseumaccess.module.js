angular.module('colosseumaccess', ['ngResource']);

/*angular.module('colosseumaccess').
	service('colAuthentication', function() {

});
*/


angular.module('colosseumaccess').
	service('dcConfig', function($resource) {

	this.Config = $resource('/api/dc/configuration/');
	this.colosseumPort = "";
	this.colosseumIp = "";
	this.authentication = {
	}
	this.locations = {
	};
	
	var self = this;
	var configP = this.Config.get({}).$promise;
	this.authentication = configP.then(function(config) {
			self.colosseumPort = config.colosseumPort;
			self.colosseumIp = config.colosseumIp;
			self.authentication = {data : {}};
			self.authentication.data.email = config.colosseumUser;
			self.authentication.data.password = config.colosseumPassword;
			self.authentication.data.tenant = config.colosseumTenant;
			return self.authentication;
		});
	this.locations = configP.then(function(config) {
			self.locations.defaultCloudId = config.defaultCloudId;
			self.locations.defaultLocationId = config.defaultLocationId;
			self.locations.defaultCactosTenantName = config.defaultCactosTenantName;
			//return self.locations;
		});
	});

angular.module('colosseumaccess').
	service('colAccessor', function($resource) {
	this.headerManipulator = function(){};
	this.Colosseum = $resource('/api/apps/colosseum/api/:path');
});

angular.module('colosseumaccess').
	service('colLogin', function($resource, dcConfig, colAccessor) {

	var self = this;
	this.tokenP = dcConfig.authentication.then(function(authentication){
		var params = JSON.parse(JSON.stringify(authentication.data));
	 	var prom = colAccessor.Colosseum.save({'path' : 'login'}, params).$promise;
		return prom.then(function(token) {
				self.token = token;
				// this is tremendously error prone //
				colAccessor.AuthColosseum = $resource(
						'/api/apps/colosseum/api/:path', null, {
						query : {
							method : 'GET',
							isArray : true,
							headers : {'X-Auth-Token' : token.token,
							'X-Auth-UserId' : token.userId,
							'X-Tenant' : authentication.data.tenant},
						}});
				return self.token;
	 	});
	});
});

angular.module('colosseumaccess').
	service('colIdGetter', function($document) {

	this.getColosseumId = function(object) {
		var link = object['link'];
		var href = link[0]['href'];
		var element = document.createElement('a')
		element.href = href;
		var split = element.pathname.split('/');
		return split[split.length - 1];
	}
});

angular.module('colosseumaccess').
	service('col', function($resource, dcConfig, colAccessor) {
	});

