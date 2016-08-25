//Lets require/import the HTTP module
var child_process = require('child_process');
var global = require('./global');
var optConfig = require('./opt_options');
var colosseum = require('./colosseum');
var http = require('http');
var dispatcher = require('httpdispatcher');

//Lets define a port we want to listen to
const PORT=8080; 
const SERVERNAME="localhost"

//We need a function which handles requests and send response
function handleRequest(request, response){
 try {
    dispatcher.dispatch(request, response);
   // response.end('It Works!! Path Hit: ' + request.url);
 } catch(err) {
    console.log(err);
 }
}

var doWriteAction = function(algType, kind, value) {
	console.log("writing configs: " + algType + "; " + kind + ";" + value);
	var result = runProcess(algType, kind, 'set', value);
	if(result.status === 0) {
		console.log("writing success: " + processProcessResult(result, 1));
	} else {
		console.log("writing failure: " + processProcessResult(result, 1));
	}
};

var executeWrites = function(algType, mainValue, subOption, subValue) {
	console.log("writing configs: " + mainValue + "; " + subOption + ";" + subValue);
	if(subOption != null && subOption !== false && subOption !== "") {
		doWriteAction(algType, subOption, subValue);
	}
	doWriteAction(algType, 'main', mainValue);
};

var writeHelper = function(body, algType, res) {

	console.log('post: ' + JSON.stringify(body));
	var data = '';
	if(body.main === undefined || body.main == null || body.main === "") {
		res.writeHead(400, {'Content-Type' : 'application/json'});
		data = '{error : "no main element found"' + body.main + '}';
	} else { // here we check if this is a valid option // 
		var option = optConfig.findOption(algType, body.main);
		if(option == null) {
			res.writeHead(400, {'Content-Type' : 'application/json'});
			data = '{error : "no optimisation branch ' + body.main + ': not found"}';
		} else if (option === false) {
			res.writeHead(400, {'Content-Type' : 'application/json'});
			data = '{error : "option ' + body.main + ' not found"}';
		} else { // now we check if a valid sub-option has been set //
			var suboption = optConfig.findSubOption(algType, option, body.sub);
			if(suboption === false) {
				res.writeHead(400, {'Content-Type' : 'application/json'});
				data = '{error : "sub-option ' + body.sub + ': not found"}';
			} else {
				res.writeHead(200, {'Content-Type' : 'application/json'});
				executeWrites(algType, body.main, option, body.sub);
				data = '{"status" : "success"}';
			}
		}
	}
	return data;
};

var runProcess = function(algType, kind, operation, value) {
	console.log("running '" + operation + "' process: " + algType + ", " + kind);
	var result = child_process.spawnSync(__dirname + "/../bin/config_operator.sh",
				[operation,
				 filenameBuilder(algType, kind),
				 propertyBuilder(algType, kind),
				 value,
				], 
				{});
	console.log("running result: " + result.status);
	return result;
};

var subReadHelper = function(algType, option) {
	if(option === "") {
		console.log("skipping subOption, as value is ''");
		return "";
	} else {
		var result = runProcess(algType, option, 'get', "");
		// assumption: first time it worked, it has to work here as well //
		if(result.status === 0) {
			var confOption = processProcessResult(result, 2);
			console.log("found subOption '" + confOption + "'");
			return confOption;
		} else {
			console.log("unable to identify sub-option: " + processProcessResult(result, 1));
			return "";
		}
	}
};

var readHelper = function(algType, res) {
	var data = { "main" : "", "sub" : ""};
	var result = runProcess(algType, 'main', 'get', '');

	if(result.status === 0){
		// algorithm is written to stderr //
		var confOption = processProcessResult(result, 2);
		var option = optConfig.findOption(algType, confOption);
		console.log("found confOption '" + confOption + "' and option '" + option + "'.");
		if(option === false) { // not a valid algorithm configuration
			res.writeHead(200, {'Content-Type' : 'application/json'});
			console.log("'" + confOption + "' is not known: leaving main config empty");
		} else {
			res.writeHead(200, {'Content-Type' : 'application/json'});
			data.main = confOption;
			data.sub = subReadHelper(algType, option);
		}
	} else {
		data = {};
		res.writeHead(500, {'Content-Type' : 'application/json'});
		data["execution"] = "failed";
		data["statuscode"] = result.status;
		data["output"] = processProcessResult(result, 1);
	}
	return data;
}

dispatcher.onGet("/dc/configuration", function(req, res) {
	res.writeHead(200, {'Content-Type' : 'application/json'});
	var config = colosseum.getConfig();
	res.end(JSON.stringify(config));
});

var filenameBuilder = function(branchName, fileType) {
	var filename = optConfig.configDir + "/" + 
		optConfig[branchName]['files'][fileType]['filename'];
	console.log('getting filename: ' + filename);
	return filename;
};

var propertyBuilder = function(branchName, fileType) {
	var property = optConfig[branchName]['files'][fileType]['propertyName'];
	console.log('getting property: ' + property);
	return property;
};

var processProcessResult = function(result, index) {
	var x = result.output[index];
	if(x == null) return null;
	if(Buffer.isBuffer(x)) return x.toString();
	return x;
}

dispatcher.onGet("/cactoopt/config/optimisation", function(req, res){
	console.log("path: " + __dirname);
	var data = readHelper('optimisation', res);
	res.end(JSON.stringify(data));
});

dispatcher.onPost("/cactoopt/config/optimisation", function(req, res){
	var body = JSON.parse(req.body);
	var data = writeHelper(body, 'optimisation', res);
	res.end(JSON.stringify(data));
});

dispatcher.onGet("/cactoopt/config/placement", function(req, res){
	console.log("path: " + __dirname);
	var data = readHelper('placement', res);
	res.end(JSON.stringify(data));
});

dispatcher.onPost("/cactoopt/config/placement", function(req, res){
	var body = JSON.parse(req.body);
	var data = writeHelper(body, 'placement', res);
	res.end(JSON.stringify(data));
});

/* placement options */
dispatcher.onGet("/cactoopt/config/placement/options", function(req, res){
	res.writeHead(200, {'Content-Type' : 'application/json'});
	var options = optConfig.placement["options"];
	res.end(JSON.stringify(options));
});

dispatcher.onGet("/cactoopt/config/placement/options/causa/subconfig", function(req, res){
	res.writeHead(200, {'Content-Type' : 'application/json'});
	var options = optConfig.placement['sub-options']['causa'];
	res.end(JSON.stringify(options));
});

/* optimisation options */
dispatcher.onGet("/cactoopt/config/optimisation/options", function(req, res){
	res.writeHead(200, {'Content-Type' : 'application/json'});
	var options = optConfig.optimisation["options"];
	res.end(JSON.stringify(options));
});

dispatcher.onGet("/cactoopt/config/optimisation/options/causa/subconfig", function(req, res){
	res.writeHead(200, {'Content-Type' : 'application/json'});
	var options = optConfig.optimisation['sub-options']['causa'];
	res.end(JSON.stringify(options));
});

dispatcher.onGet("/global/config", function(req, res){
	res.writeHead(200, {'Content-Type' : 'application/json'});
	var data = global.getConfig();
	res.end(JSON.stringify(data));
});

//Create a server
var server = http.createServer(handleRequest);

//Lets start our server
server.listen(PORT, function(){
    //Callback triggered when server is successfully listening. Hurray!
    console.log("Server listening on: http://%s:%s", SERVERNAME, PORT);
});


