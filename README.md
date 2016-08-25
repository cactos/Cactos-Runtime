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
clone https://github.com/cactos/runtime-user-interface.git
```

## Proxy Installation and Configuration

This section describes how to prepare the Web Server to host the CACTOS services. It assumes that there is no
other Apache installation is running on the same machine. In the following, we assume that the serve shall be
bound to the DNS name ```cactos.example.com``` and port ```8080``` (using http).

### Installation of Apache Web Server

We will use the apache web server to host the user interface files and to function as a proxy to
the downstream services provided CACTOS and used by the user interface. In CentOS 7, the Apache 
web server can be installed by the following command: 
```
sudo yum -y install httpd
systemctl enable httpd
```

### Configuration of Apache Web Server

The configuration of the server comprises the enabling of the proxy module, 
the definition of a new virtual host, and the configuration of CACTOS forwarding 
rules.

#### Enable Proxy Module
TODO:

#### Create New Site

We suggest to run a new Virtual Host for the CACTOS proxy. 

#### Configure New Site
