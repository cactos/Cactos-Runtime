# OpenStack Beta - hCluster

This project describes the setup of the current hCluster, deployed in virtual machines on top of the Omistack Beta. 

## Setup description

Four virtual machines currently exist: 
1. _monitoring gateway_ used for accessing the monitoring cluster. This is the only publicly accessible vm!
2. _monitoring01_ HDFS namenode, HDFS datanode, HBase master
3. _monitoring02_ HDFS secondarynamenode, HDFS datanode, HBase regionserver
4. _monitoring03_ HDFS datanode, HBase regionserver
All vms have key-based ssh access to each other (at monitoring gateway user "centos", others user "root").

Details about the installation and adding new nodes are documented in the [installation.md](installation.md).

## Accessing the Monitoring Cluster
Example ssh-config:

```
Host monitoring-gateway
	User centos
	Hostname <monitoring-gateway-ip>
	IdentityFile ~/.ssh/id_rsa
	DynamicForward 9999
Host monitoring0*
	User root
	IdentityFile ~/.ssh/id_rsa
	ProxyCommand ssh monitoring-gateway -W %h:%p
```

When connecting to monitoring-gateway with the previous configuration, you can use e.g. Firefox, configure to use SOCK5 on 127.0.0.1:9999, and access the web dashboards.

## Start / Stop the Monitoring Cluster
1. Log in to the _master node_ via ssh.
2. `~/startHStuff.sh` will start the local master services AND the slave services via ssh
3. `~/stopHStuff.sh` will stop the local master services AND the slave services via ssh
