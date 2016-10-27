## Yelp Dataset

## Introduction

### Build Instructions

##### 1. Setup single node SMACK sandbox virtual machine

Clone SMACK vagrant repository

```
git clone https://github.com/kartik-dev/SMACK-Sandbox.git
```

Bring up SMACK single node VM

This might take time,since it downloads and installs hadoop, mesos, spark, cassandra, nifi, confluent-io platform and Zeppelin

```
vagrant up
```

ssh to smack vm (use putty)

```
ip: 192.168.0.50

username: root``

password: vagrant
```

##### 2. Clone git repository

```
git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git
```

Upload sample dataset

```
cd yelp_academic_dataset_review ``

sh scripts/yelp-data-upload.sh /vagrant/resources/yelp_dataset_challenge_academic_dataset.tar

```

##### 3. Dockerizing Spark - Build Spark Base docker Image

Downloads base java 8 image and installs spark 2.0 binaries.

```
docker build -t newyorker/spark -f SparkBaseDockerImage .
```

##### 4. Build docker image of Spark Driver Image

Note that this will take a while when you start it for the first time since it downloads and installs maven and downloads all the project’s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

The pom.xml contains a very basic Maven configuration. It configures the Spark 2.0 dependencies using a Java 1.8 compiler and creates a fat jar with all the dependencies.

sbt build tool could be used in place of maven. This could be easily replaced in Dockerfile

```
docker build -t newyorker/spark-driver -f SparkDriverDockerImage .
```

##### 5. Setup docker network for spark

```
docker network create spark_network;
```

##### 6. Submit Spark application
Once the image is built, submit spark application. Spark application will be deployed on standalone spark. This could be changed by changing the spark master URL

-- Aggregate reviews by user
 
```
docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpReviewsByUser" newyorker/spark-driver 
``` 