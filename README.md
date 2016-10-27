## Yelp Dataset Challenge

## Introduction

Upload yelp_academic_dataset_review into HDFS for analytics. yelp-data-upload-to-HDFS.sh file will take tar file as parameter and upload extracted json files to HDFS

Another very interesting use-case, is to include web-based notebooks that enables faster interactive data-analytics than the Spark-shell like Zeppelin

### Build Instructions

##### 1. Setup single node SMACK sandbox virtual machine

Clone SMACK vagrant repository

```
git clone https://github.com/kartik-dev/SMACK-Sandbox.git
```

Bring up SMACK single node VM

This might take time (Approx 10 to 15mins),since it downloads and installs hadoop, mesos, spark, cassandra, nifi, confluent-io platform and Zeppelin

```
vagrant up
```

ssh to smack vm (use putty)

```
ip: 192.168.0.50

username: root``

password: vagrant
```

##### 2. Clone yelp_academic_dataset_review repository

```
git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git
```

Upload sample dataset

```
cd yelp_academic_dataset_review ``

sh scripts/yelp-data-upload-to-HDFS.sh <tar file path(i.e /vagrant/resources/yelp_dataset_challenge_academic_dataset.tar)>

```
-- You could still use the file /vagrant/resources/yelp_dataset_challenge_academic_dataset.tar for testing.

##### 3. Dockerizing Spark - Build Spark Base docker Image

Downloads base java 8 image and installs spark 2.0 binaries.

```
docker build -t newyorker/spark -f SparkBaseDockerImage .
```

##### 4. Build docker image of Spark Driver Image

Note that this will take a while when you start it for the first time since it downloads and installs maven and downloads all the project’s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

The pom.xml contains a very basic Maven configuration. It configures the Spark 2.0 dependencies using a Java 1.8 compiler and creates a fat jar with all the dependencies.

sbt build tool could be used in place of maven. This could be easily be replaced in Dockerfile

```
docker build -t newyorker/spark-driver -f SparkDriverDockerImage .
```

##### 5. Setup docker network for spark

Here we’re running the default bridge mode and mapping ports into the container.  

```
docker network create spark_network;
```

##### 6. Submit Spark application
Once the image is built, submit spark application. Spark application will be deployed on standalone spark. This could be changed by changing the spark master URL

-- Aggregate reviews by user
 
```
docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpReviewsByUser" newyorker/spark-driver 
``` 