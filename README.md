## Yelp Dataset Challenge

### Introduction

This application will upload yelp_academic_dataset_review dataset into HDFS for analytics and Spark SQL application to query the data.

Data set consists of Business, reviews, Users, checkins, tips from yelp. yelp-data-upload-to-HDFS.sh script will take dataset tar file as parameter and upload extracted json files to HDFS

Spark Application (YelpGroupReviewsByStars) - Spark driver application will create dataframe out of json file and group the reviews by stars from yelp_academic_dataset_review.json

Another very interesting use-case, is to include web-based notebooks that enables faster interactive data-analytics than the Spark-shell like Zeppelin

Dockerize Spark base and driver program to be launched. (Docker container could be orchestrated and managed by Marathon on Mesos for better resource utilzation, high availability and fault tolerance)

### Solution Architecture

![alt tag](https://github.com/kartik-dev/yelp_academic_dataset_review/blob/master/scripts/Yelp_dataset_challenge_architecture.png)

## Docker Images

kramalingam/spark - Dockerized spark binaries as base image for driver application to use

kramalingam/spark-zeppelin - Dockerized zeppelin for beautiful data-driven, interactive and collaborative documents with SQL, Scala

kramalingam/spark-driver - Dockerized spark driver application with 

## Installation

#### Setup single node SMACK sandbox virtual machine

Clone SMACK vagrant repository

```
git clone https://github.com/kartik-dev/SMACK-Sandbox.git
```

Bring up SMACK single node VM

This might take some time (Approx 10 to 15mins),since it downloads and installs hadoop, mesos, spark, cassandra, nifi, confluent-io platform and Zeppelin

```
cd SMACK-Sandbox

vagrant up
```

Once ready, ssh to smack vm (use putty)

```
ip: 192.168.0.50

username: root

password: vagrant
```

Please refer to https://github.com/kartik-dev/SMACK-Sandbox for more information on SMACK-Sandbox

## Steps to run Yelp Dataset challenge application 

1. Clone yelp_academic_dataset_review repository
```
cd /root

git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git
```

2. Download yelp dataset from https://www.yelp.com/dataset_challenge/dataset

3. Uploading dataset into HDFS:
```
cd /root/yelp_academic_dataset_review

sh scripts/yelp-data-upload-to-HDFS.sh <tar file path>
```

4. Interactive data analysis with Apache Zeppelin:
```
docker pull kramalingam/spark-zeppelin

docker run --rm -p 8080:8080 kramalingam/spark-zeppelin &
```
Zeppelin will be running at http://192.168.0.50:8080 and sample zeppelin notebook scripts/YelpReviewDataset.json

5. Bring up Cassandra
```
export PATH=$PATH:/usr/local/cassandra/bin

cp /vagrant/resources/cassandra/cassandra.yaml /usr/local/cassandra/conf/
 
cassandra -R &
```

setup cassandra tables
```
cqlsh 192.168.0.50 -f scripts/cassandra-query.cql
```

6. To run the spark SQL application

```
docker pull kramalingam/spark-driver

docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpGroupReviewsByStars" kramalingam/spark-driver 


## Rebuild and deploy docker images

-- Rebuild Spark base image
```
cd /root/yelp_academic_dataset_review

docker build -t kramalingam/spark -f SparkBaseDockerImage .
```

-- Rebuild docker image of Spark Driver Application

Note that this will take a while when you start it for the first time since it downloads and installs maven and downloads all the project’s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

The pom.xml contains a very basic Maven configuration. It configures the Spark 2.0 dependencies using a Java 1.8 compiler and creates a fat jar with all the dependencies.

sbt build tool could be used in place of maven. This could be easily be replaced in Dockerfile

To rebuild the spark driver image:
```
cd /root/yelp_academic_dataset_review

mvn clean compile package

docker build -t kramalingam/spark-driver -f SparkDriverDockerImage .
```

Once the image is built, submit spark application. Spark application will be deployed on standalone spark. This could be changed by changing the spark master URL

To rebuild spark-zeppelin image:
```
docker build -t kramalingam/spark-zeppelin -f SparkZeppelinDockerImage .
```

## Use cases:
- Review Rating Prediction 
- Sentiment Analysis
- Natural Language Processing (NLP)
- Social Graph Mining

## Things to improve
- [ ] Deploy spark on Mesos Cluster Manager with Marathon for better resource utilization and high availability 
- [ ] Deploy Spark driver application with Marathon for spark driver fault tolerance and high availability  
- [ ] Use of Dataflow tools like NiFi, StreamSets for enabling accelerated data collection, curation, analysis and delivery in real-time 