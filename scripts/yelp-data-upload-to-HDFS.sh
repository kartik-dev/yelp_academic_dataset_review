#!/bin/bash

echo "Cleanup....."

rm -rf /tmp/yelp-dataset

if [ "$1" = "" ]
then
  echo "Please specify the tar file path"
  exit 1
fi

echo $1

echo "Setup tmp directory to extract the dataset"

mkdir /tmp/yelp-dataset

echo " Extract tar file"

tar xf $1 -C /tmp/yelp-dataset/

echo "Creating HDFS directory"

hdfs dfs -mkdir /yelp-dataset

echo "Uploading files to HDFS"

hdfs dfs -copyFromLocal /tmp/yelp-dataset/*.json /yelp-dataset

echo "Uploaded json file to HDFS"

