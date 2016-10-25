// Define the `phonecatApp` module

function AppDetailsController($routeParams, colAccessor, colIdGetter, $filter, $location, $q, $resource, dcConfig){
	var ApplicationStarter = $resource("/api/apps/deployment/:appName", {appName : '@appName'});

	var search = $location.search();
	var self = this;

	this.applicationId = $routeParams.appName;
	this.applicationName = (search === null || search['name'] === undefined) ? "unknown" : search['name'];

	this.appInstances = colAccessor.AuthColosseum.query({'path' : 'applicationInstance'});
	this.appComponents = colAccessor.AuthColosseum.query({'path' : 'ac'});

	this.appCompInstances = colAccessor.AuthColosseum.query({'path' : 'instance'});
	this.lifecycleComponents = colAccessor.AuthColosseum.query({'path' : 'lifecycleComponent'});
	this.virtualMachines = colAccessor.AuthColosseum.query({'path' : 'virtualMachine'});
	this.flavours = colAccessor.AuthColosseum.query({'path' : 'hardware'});
	this.ips = colAccessor.AuthColosseum.query({'path' : 'ip'});
	this.offers = colAccessor.AuthColosseum.query({'path' : 'hardwareOffer'});
	this.images = colAccessor.AuthColosseum.query({'path' : 'image'});
	this.locations = colAccessor.AuthColosseum.query({'path' : 'location'});

	var x = null;

	var cactosTenantId = null;
	$q.all([self.locations.$promise, dcConfig.locations]).then(function(res) {
			var result = $filter('filter')(
					self.locations, 
					{"name" : dcConfig.locations.defaultCactosTenantName}
				);
			 var endRes = checkForOneInstance(result, null);
			 if(typeof endRes == "string") {
				 alert("did not find location");
				 cactosTenantId = false;
			 } else {
				 var x = self.getId(endRes);
				 cactosTenantId = x;
			 }
	});

	this.components = [];
	this.deployment = {};
	this.deployment.components = $q.all([this.appComponents.$promise, 
					this.lifecycleComponents.$promise]).then(
			function(res) {
				var result = $filter('filter')(
						self.appComponents,
						{"application" : self.applicationId}
						);
				
				var comps = {};
				var compInformation = [];
				angular.forEach(result, function(component){
					var compId = component.component;
					if(comps[compId] === undefined) {
						comps[compId] = true;
						var name = self.findLcComponentName(compId);
						compInformation.push({"name" : name, "instances" : 1});
					} else {
						// ignore //
					}
				});

			self.deployment.components = compInformation;
	});

	this.deployApplication = function() {
		var temp = JSON.parse(JSON.stringify(this.deployment.components));
		var currentAppName = this.applicationName.toLowerCase();

		angular.forEach(temp, function(element){
			element.idCloud = dcConfig.locations.defaultCloudId;
			element.idLocation = cactosTenantId;
		});

		ApplicationStarter.save({appName : currentAppName}, temp);
	
		this.clearForm();
	};

	this.clearForm = function() {

		angular.forEach(this.deployment.components, function(component) {
			component.instances = 1;
			component.idHardware = "";
			component.idImage = "";
			component.idCloud = "";
			component.idLocation = "";
			angular.forEach(self.deployment.metadata[self.applicationName][component.name].metadata,
					function(data){
						component[data.property_name] = "";
					});
		});
	};

	this.deployment.flavours = $q.all([this.flavours.$promise,
       						this.offers.$promise]).then(function(res){
		var result = $filter('filter')(
				self.flavours,
				{	"cloud" : dcConfig.locations.defaultCloudId, 
					"location" : dcConfig.locations.defaultLocationId}
				);
		var flavourInfo = [];
		angular.forEach(result, function(flavour){
			var flavourId = self.getId(flavour);
			var offerId = flavour['hardwareOffer'];
			var offer = self.findOfferWithId(offerId);
			flavourInfo.push({"id" : flavourId, 
					"offer" : offer});
		});
		self.deployment.flavours = flavourInfo;
	});

	this.deployment.metadata = { // todo: move to configuration server 
		"Molpro" : {
			"MolproComponent" : {
				"metadata" : [
					{"name" : "Molpro RAM",
					 "property_name" : "molpro_size",	
					 "options" : [
					 	{"name"  : "8 GB (950 words)",
						 "value" : "950"},
					 	{"name"  : "16 GB (1900 words)",
						 "value" : "1900"},
					 	{"name"  : "32 GB (3800 words)",
						 "value" : "3800"},
					 	{"name"  : "60 GB (7600 words)",
						 "value" : "7600"},
					 	{"name"  : "100 GB (12500 words)",
						 "value" : "12500"},
					 ]},
					{"name" : "Configuration",
					 "property_name" : "molpro_input",
					 "options" : [
					 	{"name" : "dft",
						 "value" : "dft"},
					 	{"name" : "lccsd-SP01",
						 "value": "lccsd-SP01"},
					 	{"name" : "lccsd-SP02",
						 "value": "lccsd-SP02"},
					 	{"name" : "lccsd-SP03",
						 "value": "lccsd-SP03"},
					 	{"name" : "lccsd-SP04",
						 "value": "lccsd-SP04"},
					 	{"name" : "lccsd-SP05",
						 "value": "lccsd-SP05"},
					 ]},
				],
				"minInstances" : 1,
				"maxInstances" : 1,
				"images" : "molpro",
			}
		},
		"DataPlay" : {
			"LoadBalancer" : {
				"minInstances" : 1,
				"maxInstances" : 1,
				"images" : "Ubuntu",
			},
			"Master" : {
				"minInstances" : 1,
				"maxInstances" : 10,
				"images" : "Ubuntu",
			},
			"Frontend" : {
				"minInstances" : 1,
				"maxInstances" : 4,
				"images" : "Ubuntu",
			},
			"Cassandra" : {
				"minInstances" : 1,
				"maxInstances" : 1,
				"images" : "Ubuntu",
			},
			"Postgresql" : {
				"minInstances" : 1,
				"maxInstances" : 5,
				"images" : "Ubuntu",
			},
			"Pgpool" : {
				"minInstances" : 1,
				"maxInstances" : 1,
				"images" : "Ubuntu",
			},
			"Redis" : {
				"minInstances" : 1,
				"maxInstances" : 1,
				"images" : "Ubuntu",
			},
		},
	};

	var checkForOneInstance = function(result, value) {
		if(result.length == 0) {
			return "!unknown!";
		} else if(result.lenth > 1) {
			return "too many";
		} else if(value == null){
			return result[0];
		} else {
			return result[0][value];
		}

	}

	this.findOfferWithId = function(offerId) {
		var uri = "http://134.60.64.254:9000/api/hardwareOffer/";
		var result = $filter('filter')(
				this.offers,
				{"link" : {"href" : uri + offerId }});
		return checkForOneInstance(result, null);
	}

	this.findIpAddress = function(compInstance) {
		var vmId = compInstance['virtualMachine'];
		var result = $filter('filter')(
				this.ips,
				{"ipType" : "PUBLIC", 
				 "virtualMachine" : vmId});
		return checkForOneInstance(result, 'ip');
	}

	this.getIdAsInt = function(object) {
		return parseInt(colIdGetter.getColosseumId(object), 10);
	};

	this.getId = function(object) {
		return colIdGetter.getColosseumId(object);
	}

	this.findLcComponentName = function (componentId) {
		var uri = "http://134.60.64.254:9000/api/lifecycleComponent/";
		var result = $filter('filter')(
				this.lifecycleComponents,
				{"link" : {"href" : uri + componentId }}	
			);
		return checkForOneInstance(result, 'name');
	}

	this.orderInstancesByComponents = function(theId) {
		
	}
}

angular.module('appdetails').component('applicationDetails', {
	         templateUrl: 'app-details/app-details.template.html',
	          controller: AppDetailsController
});

