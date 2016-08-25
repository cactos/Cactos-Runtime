# runtime-controller

A node.js based backend service run steer the data centre and control its configuration.

# Installation

This component is stand alone. It can be installed without any other component of the CACTOS Runtime Toolkit available. 
Nevertheless, in order to make it work properly and have its configuration take effect on the runtime system, several 
other tools need to be installed (see below).

The installation and operation has been tested on CentOS 7.

## install node js

```
yum install epel-release
yum install nodejs
```
## create runtime directory

Pick any directory on the server /runtime controller/ is supposed to run and create it ```mkdir -p /opt/rtc```. 
Then, move there: ```cd /opt/rtc```. Next, clone this repo:

```
git clone git@github.com:cactos/runtime-controller.git
```

## Configuration Options

The runtime controller supports multiple configuration options that are used either by itself or the the
[runtime user interface](https://github.com/cactos/runtime-user-interface).

### Port Number

The port number used by the application is configurable in the ```server.js``` file. Update the folllowing 
line to the desired number.
```
//Lets define a port we want to listen to
const PORT=8080; 
```

###  Enabled Features
The file ```global.js``` contains switches to turn on/off features of the user interface.
```
var config = {
        'cactoopt'      : { "status" : "on" },
        'monitors'      : { "status" : "on" },
        'apps'          : { "status" : "on" },
        'metrics'       : { "status" : "on" },
    };
```
Setting the respective values to anything different from ```on``` will disable the feature in the user interface.

### Application-specific Configuration

When Runtime Controller is supposed to be used for deploying new applications, ```colosseum.js``` provides the 
necessary configuration options:
```
        'colosseumIp' : '',
        'colosseumPort' : '9000',
        'colosseumUser' : 'john.doe@example.com',
        'colosseumTenant' : 'admin',
        'colosseumPassword' : 'admin',
        'defaultCloudId' : "1",
        'defaultLocationId' : "2",
        'defaultCactosTenantName' : "RegionOne/cactos-testing",
```

It is mandatory to fill-in ```colosseumIp```. This as well as ``colosseumPort``` as well as the other colosseum 
properties depend on the configuration of the [Runtime Toolkit](http://#).
