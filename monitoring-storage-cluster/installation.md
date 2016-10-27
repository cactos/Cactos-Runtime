# Installing Monitoring Cluster

Simply follow the HDFS [1][2] and the HBase guides [3].

[1] http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html
[2] http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/ClusterSetup.html
[3] http://hbase.apache.org/book.html#quickstart

## Step-by-step - new cluster setup
On a fresh centos 7 VM, do the following steps. 

If you want a multi-node setup, make sure to setup key-based ssh authentication between all nodes first. Also, please set up and /etc/hosts correctly on all nodes.

```
mkdir cactoscale
export GITCLONE=./cactoscale
```

1. Prepare the setup on first node

```
# install required packages first
yum install epel-release wget vim java-openjdk

# download hadoop and hbase binaries
cd ~
wget http://mirror.softaculous.com/apache/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz
tar xfzv hadoop-2.6.0.tar.gz
wget https://archive.apache.org/dist/hbase/1.1.1/hbase-1.1.1-bin.tar.gz
tar xfzv hbase-1.1.1-bin.tar.gz

# copy helper scripts
cp $GITCLONE/hCluster/bin/* .
chmod +x ./*.sh

```

2. Configure the setup

```

# place the config files from this repo
cp $GITCLONE/hCluster/conf/hadoop/* ~/hadoop-2.6.0/etc/hadoop/
cp $GITCLONE/hCluster/conf/* ~/hbase-1.1.1/conf/
```

Change the following configuration files as needed:
* hadoop: core-site.xml, line 46, property "fs.default.name" value "hdfs://monitoring01:8020"
* hadoop: dfs-hosts, line 1, add hostname of namenode(s)
* hadoop: hdfs-site.xml, line330, property "dfs.https.address" value "monitoring01:50470"
* hadoop: slaves, add hostnames for data nodes

* hbase: hbase-site.xml, line 25, property "hbase.rootdir" value "hdfs://192.168.0.3:8020/hbase"
* hbase: hbase-site.xml, line 37, property "hbase.zookeeper.quorum" value "list of all hbase nodes"
* hbase: regionservers, add hostnames for region servers

```
# format the hdfs root dir
cd ~/hadoop-2.6.0
bin/hdfs namenode -format
```

Thats it. An additional node.

## Step-by-step - add new node to existing cluster

1. Prepare the node
Log in to the new node. Set up key-based ssh login and /etc/hosts.
```
yum install epel-release wget vim java-openjdk
```

2. Add new node to configuration
Log in to the first node. Edit the settings:
* hadoop: slaves, add hostnames for data nodes
* hbase: hbase-site.xml, line 37, property "hbase.zookeeper.quorum" value "list of all hbase nodes"
* hbase: regionservers, add hostnames for region servers

3. Copy setup from first node to new node
Log in to the first node. Use the helper script.

```
# edit the distribute script 
~/distribute.sh hostname_of_new_node
```
Thats it. Binaries and configuration are copied and extracted now.
You can use start/stop now to restart the cluster, with the new node.

## Forward requests to port through the monitoring-gateway vm to a node

1. Make sure to have iptables installed, if not run 
```
yum install iptables-services
```

2. Execute the following rules in a terminal
```
sysctl net.ipv4.ip_forward=1
iptables -t nat -A PREROUTING -p tcp --dport port -j DNAT --to-destination ip:port
iptables -t nat -A POSTROUTING -j MASQUERADE
iptables -I FORWARD -p tcp --dport 8080 -j ACCEPT
```