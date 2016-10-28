## Yelp Dataset Challenge

### Introduction

This application uploads yelp_academic_dataset_review into HDFS for analytics. yelp-data-upload-to-HDFS.sh file will take tar file as parameter and upload extracted json files to HDFS

Another very interesting use-case, is to include web-based notebooks that enables faster interactive data-analytics than the Spark-shell like Zeppelin

### Architecture

HDFS => Spark => Cassandra => Visualization Tool

Yelp => Kafka => Spark Streaming => Cassandra/HDFS => Visualization Tools 

### SMACK Sandbox

Please refer to https://github.com/kartik-dev/SMACK-Sandbox for more information on SMACK-Sandbox

## Docker Images

newyorker/spark

newyorker/spark-zeppelin

newyorker/spark-driver
 
## Installation

##### 1. Setup single node SMACK sandbox virtual machine

Clone SMACK vagrant repository

```
git clone https://github.com/kartik-dev/SMACK-Sandbox.git
```

Bring up SMACK single node VM

This might take some time (Approx 10 to 15mins),since it downloads and installs hadoop, mesos, spark, cassandra, nifi, confluent-io platform and Zeppelin

```
vagrant up
```

Once ready, ssh to smack vm (use putty)

```
ip: 192.168.0.50

username: root

password: vagrant
```

##### 2. Clone yelp_academic_dataset_review repository

```

cd /root

git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git

```

Download yelp dataset from https://www.yelp.com/dataset_challenge/dataset

Upload dataset into HDFS with scripts/yelp-data-upload-to-HDFS.sh

```
cd yelp_academic_dataset_review ``

sh scripts/yelp-data-upload-to-HDFS.sh <tar file path>

```

##### 3. Dockerizing Spark - Build Spark Base docker Image

Downloads base java 8 image and installs spark 2.0 binaries.

```
docker build -t newyorker/spark -f SparkBaseDockerImage .
```

##### 4. Dockerizing Spark Driver - Build docker image of Spark Driver Application

Note that this will take a while when you start it for the first time since it downloads and installs maven and downloads all the project�s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

The pom.xml contains a very basic Maven configuration. It configures the Spark 2.0 dependencies using a Java 1.8 compiler and creates a fat jar with all the dependencies.

sbt build tool could be used in place of maven. This could be easily be replaced in Dockerfile

```
docker build -t newyorker/spark-driver -f SparkDriverDockerImage .
```

##### 5. Setup docker network for spark

Here we�re are going to run in default bridge mode and mapping ports into the container.  

```
docker network create spark_network;
```

##### 6. Bring up Cassandra

```
export PATH=$PATH:/usr/local/cassandra/bin

cassandra -R &

```

##### 7. Submit Spark application
Once the image is built, submit spark application. Spark application will be deployed on standalone spark. This could be changed by changing the spark master URL

-- Aggregate reviews by user
 
```
docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpReviewsByUser" newyorker/spark-driver 
``` 

##### 8. Build and run spark-zeppelin for adhoc analysis

```
docker build -t newyorker/spark-zeppelin -f SparkZeppelinDockerImage .

docker run --rm -p 8080:8080 newyorker/spark-zeppelin &
```

Zeppelin will be running at http://192.168.0.50:8080

Please see sample zeppelin notebook 
