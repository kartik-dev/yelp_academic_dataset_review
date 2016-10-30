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

#### Clone yelp_academic_dataset_review repository
```
cd /root

git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git
```

#### Download yelp dataset from https://www.yelp.com/dataset_challenge/dataset

#### Uploading dataset into HDFS
```
cd /root/yelp_academic_dataset_review

sh scripts/yelp-data-upload-to-HDFS.sh <tar file path>
```

#### Interactive data analysis with Apache Zeppelin
```
docker pull kramalingam/spark-zeppelin

docker run --rm -p 8080:8080 kramalingam/spark-zeppelin &
```
Zeppelin will be running at http://192.168.0.50:8080 and sample zeppelin notebook scripts/YelpReviewDataset.json

#### Bring up Cassandra
```
export PATH=$PATH:/usr/local/cassandra/bin

cp /vagrant/resources/cassandra/cassandra.yaml /usr/local/cassandra/conf/
 
cassandra -R &
```

setup cassandra tables
```
cqlsh 192.168.0.50 -f scripts/cassandra-query.cql
```

#### To run the spark SQL application

```
docker pull kramalingam/spark-driver

docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpGroupReviewsByStars" kramalingam/spark-driver 
```

## Rebuild and deploy docker images

#### Rebuild docker image of Spark base
```
cd /root/yelp_academic_dataset_review

docker build -t kramalingam/spark -f SparkBaseDockerImage .
```
SparkBaseDockerImage - Dockerfile
```
FROM java:8 

RUN apt-get update
RUN apt-get install -y maven

RUN wget http://d3kbcqa49mib13.cloudfront.net/spark-2.0.0-bin-hadoop2.7.tgz

RUN tar -xzf spark-2.0.0-bin-hadoop2.7.tgz

RUN mv spark-2.0.0-bin-hadoop2.7 /opt/spark

EXPOSE 8080
```

#### Rebuild docker image of Spark Driver Application

Note that this will take a while when you start it for the first time since it downloads and installs maven and downloads all the project’s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

The pom.xml contains a very basic Maven configuration. It configures the Spark 2.0 dependencies using a Java 1.8 compiler and creates a fat jar with all the dependencies.

sbt build tool could be used in place of maven. This could be easily be replaced in Dockerfile

To rebuild the spark driver image
```
cd /root/yelp_academic_dataset_review

mvn clean compile package

docker build -t kramalingam/spark-driver -f SparkDriverDockerImage .
```
SparkDriverDockerImage - Dockerfile
```
#using the spark-docker image we just created as our base image
FROM kramalingam/spark

#app.jar is our Fat Jar to be run; here we assume it’s in the same build context as the Dockerfile;
COPY target/yelp-academic-dataset-review-0.7-jar-with-dependencies.jar /opt/yelp-academic-dataset-review-0.7-jar-with-dependencies.jar

#calling the spark-submit command; with the --class argument being an input environment variable
CMD /opt/spark/bin/spark-submit --class $SPARK_CLASS --master local[1] /opt/yelp-academic-dataset-review-0.7-jar-with-dependencies.jar
```

Once the image is built, submit spark application. Spark application will be deployed on standalone spark.
```
docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpGroupReviewsByStars" kramalingam/spark-driver 
```

#### To rebuild spark-zeppelin image:

```
docker build -t kramalingam/spark-zeppelin -f SparkZeppelinDockerImage .
```

Dockerfile
```
#using the spark-docker image we just created as our base image
FROM kramalingam/spark

# Zeppelin
ENV ZEPPELIN_PORT 8080
ENV ZEPPELIN_HOME /usr/zeppelin
ENV ZEPPELIN_CONF_DIR $ZEPPELIN_HOME/conf
ENV ZEPPELIN_NOTEBOOK_DIR $ZEPPELIN_HOME/notebook
ENV ZEPPELIN_COMMIT 22bd851047c4ada20108754f3d15fbd8fe7b065a

RUN set -ex \
 && buildDeps=' \
    bzip2 \
 ' \
 && apt-get update && apt-get install -y --no-install-recommends $buildDeps \
 && curl -sL http://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
   | gunzip \
   | tar x -C /tmp/ \
 && git clone https://github.com/apache/zeppelin.git /usr/src/zeppelin \
 && cd /usr/src/zeppelin \
 && git checkout -q $ZEPPELIN_COMMIT \
 && dev/change_scala_version.sh "2.11" \
 && sed -i 's/--no-color/buildSkipTests --no-color/' zeppelin-web/pom.xml \
 && MAVEN_OPTS="-Xms512m -Xmx1024m" /tmp/apache-maven-3.3.9/bin/mvn --batch-mode package -DskipTests -Pscala-2.11 -Pbuild-distr \
  -pl 'zeppelin-interpreter,zeppelin-zengine,zeppelin-display,spark-dependencies,spark,markdown,angular,shell,hbase,postgresql,jdbc,python,elasticsearch,zeppelin-web,zeppelin-server,zeppelin-distribution' \
 && tar xvf /usr/src/zeppelin/zeppelin-distribution/target/zeppelin*.tar.gz -C /usr/ \
 && mv /usr/zeppelin* $ZEPPELIN_HOME \
 && mkdir -p $ZEPPELIN_HOME/logs \
 && mkdir -p $ZEPPELIN_HOME/run \
 && rm -rf $ZEPPELIN_NOTEBOOK_DIR/2BWJFTXKJ \
 && apt-get purge -y --auto-remove $buildDeps \
 && rm -rf /var/lib/apt/lists/* \
 && rm -rf /usr/src/zeppelin \
 && rm -rf /root/.m2 \
 && rm -rf /root/.npm \
 && rm -rf /tmp/*

WORKDIR $ZEPPELIN_HOME
CMD ["bin/zeppelin.sh"]
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