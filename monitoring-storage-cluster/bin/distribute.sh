#!/bin/bash
tar cfz hStuff.tgz hbase-1.1.1/ hadoop-2.6.0/
scp hStuff.tgz $1:/root/
ssh $1 'tar xfz hStuff.tgz; rm -f hStuff.tgz'
rm -f hStuff.tgz
