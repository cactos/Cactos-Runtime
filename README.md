# runtime-user-interface

This file captures the css, javascript and HTML files for the client user interface that enables steering the 
CACTOS runtime toolkit. In particular, it supports control over the monitors installed in the testbed, over
the configuration of CactoOpt, and supports the deployment of new applications.

## Installation and Configuration

The installation process captures the steps of cloning the sources to the server and further to
install and configure an apache web server as well as several forwarding rules.

## Installation of Web Files

You can basically pick any directory to install the Web files. The only important thing is
that later the web server can read from this directory. For now, assume we use ```/opt/cactos/```
as root directory.

Then, install the files with the following commands.
```
mkdir /opt/cactos
cd /opt/cactos/
git clone https://github.com/cactos/runtime-user-interface.git
```

## Proxy Installation and Configuration

This section describes how to prepare the Web Server to host the CACTOS services. It assumes that there is no
other Apache installation running on the same machine. In the following, we assume that the server shall be
bound to the DNS name ```cactos.example.com``` and port ```8008``` (using http).

### Installation of Apache Web Server

We will use the apache web server to host the user interface files and to function as a proxy to
the downstream services provided CACTOS and used by the user interface. In CentOS 7, the Apache 
web server can be installed and enabled by the following command: 
```
sudo yum -y install httpd
systemctl enable httpd
```

### Configuration of Apache Web Server

The configuration of the server comprises the enabling of the proxy module, 
the definition of a new virtual host, and the configuration of CACTOS forwarding 
rules.

#### Enable Proxy Module
Per default in Centos 7, the proxy modules are already enabled for Apache webserver in ```/etc/httpd/conf.modules.d/00-proxy.conf```. 
Yet, with SELinux enabled, the httpd needs the permission open network sockets: ```setsebool -P httpd_can_network_connect 1```

#### Create New Site

We suggest to run a new Virtual Host for the CACTOS proxy. In order to facilitate the configuration of the
web server, this project comes with a config file template for such a virtual host. Copy the file to the 
apache configuration directory:
````
cp /opt/cactos/runtime-user-interface/000-default.conf /etc/http/conf.d/15-cactos_vhost.conf
```

#### Configure New Site

Open the file ```15-cactos_vhost.conf``` and edit the following lines:
```
Listen 8008

<VirtualHost *:8008>
  ServerName cactos-runtime.example.com:8008
  Define runtimeController	<set value:8080>
  Define runtimeManagement	<set value:9090>
  Define monitoringService	<set value:8081>	
  Define colosseumServer	<set value:9000>
  
  DocumentRoot "/tmp/runtime-user-interface/www"
  <Directory "/tmp/runtime-user-interface/www">
...
```

Set the value of ```Listen``` the desired port (above, we decided to go for ```8080```). Make sure to adapt the 
port of the ```VirtualHost``` and the port of the ```ServerName``` accordingly. Update the DocumentRoot to match your checkout location of this repository.
