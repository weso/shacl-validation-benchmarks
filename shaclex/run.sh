#!/bin/bash

cd shaclex

start=`date +%s`

sbt "run --data ../../data/1-lubm.ttl \
         --schema ../../data/lubm.ttl \
         --engine shaclex" > /dev/null

end=`date +%s`

runtime=$((end-start))

echo $runtime

cd -