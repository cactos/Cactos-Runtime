
var config = {
	'colosseumIp' : '',
        'colosseumPort' : '9000',
        'colosseumUser' : 'john.doe@example.com',
        'colosseumTenant' : 'admin',
        'colosseumPassword' : 'admin',
        'defaultCloudId' : "1",
        'defaultLocationId' : "2",
        'defaultCactosTenantName' : "RegionOne/cactos-testing",
    };


var copyConfig = function() {
 return JSON.parse(JSON.stringify(config));
};


module.exports = {
     getConfig : copyConfig,
}; 

