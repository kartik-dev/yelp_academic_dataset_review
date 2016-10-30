## Yelp Dataset Challenge

### Background

Yelp Dataset consists of business, reviews, users, checkins, tips from yelp. Yelp engineers have developed an excellent search engine to sift through 102 million reviews and help people find the most relevant businesses for their everyday needs.

This application uploads Yelp dataset into HDFS for analytics. It uses spark sql to query and analysis the dataset stored in HDFS and it can be scheduled to run periodically to aggregate and store the output in Cassandra. Spark SQL can automatically infer the schema of a JSON dataset and load it as a Dataset, hence it doesn't require pre parsing of JSON files.

Spark driver program is dockerized and it can be launched on standalone spark cluster or on mesos cluster manager.

This application also includes web-based notebooks that enables faster interactive data-analytics with apache Zeppelin and the application is dockerized.

### Solution Architecture

![alt tag](https://github.com/kartik-dev/yelp_academic_dataset_review/blob/master/scripts/Yelp_dataset_challenge_architecture.png)

## Installation

### Setup single node SMACK sandbox virtual machine

#### Step 1: Clone SMACK vagrant repository
```
git clone https://github.com/kartik-dev/SMACK-Sandbox.git
```

#### Step 2: Bring up SMACK sandbox

This might take some time (Approx 10 to 15mins),since it downloads and installs hadoop, mesos, spark, cassandra, nifi, confluent-io platform and Zeppelin (we might not use all the above service for this application, but it is part of the SMACK sandbox)
```
cd SMACK-Sandbox

vagrant up
```

#### Step 3: Once ready, ssh to smack vm (use putty)
```
ip: 192.168.0.50

username: root

password: vagrant
```

Note:
Please refer to https://github.com/kartik-dev/SMACK-Sandbox for more information on SMACK-Sandbox and https://github.com/kartik-dev/SMACK-Sandbox/blob/master/ADVANCED.md for advanced information

As of now, SMACK services needs to be started manually in case if the vagrant machine is halted. Please refer to https://github.com/kartik-dev/SMACK-Sandbox/blob/master/ManualStartScripts.md for starting services manually

## Steps to run Yelp Dataset challenge application 

#### Step 1: Clone yelp_academic_dataset_review repository
```
cd /root

git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git
```

#### Step 2: Download yelp dataset from https://www.yelp.com/dataset_challenge/dataset

#### Step 3: Uploading dataset into HDFS

This script will take .tar file as parameter and store extracted json files into HDFS
```
cd /root/yelp_academic_dataset_review

sh scripts/yelp-data-upload-to-HDFS.sh <tar file path>
```

#### Step 4: Bring up Cassandra
Cassandra will be used by spark application to store aggregated/output data for visualization or deeper analysis 
```
export PATH=$PATH:/usr/local/cassandra/bin

cp /vagrant/resources/cassandra/cassandra.yaml /usr/local/cassandra/conf/
 
cassandra -R &
```

create cassandra tables used by spark application
```
cqlsh 192.168.0.50 -f scripts/cassandra-query.cql
```

#### Step 5: Running a Spark application with Docker

Pull kramalingam/spark-driver image from docker-io registry
```
docker pull kramalingam/spark-driver
```

Now that the image is built, we just need to run it (this will launch standalone spark cluster)

use case 1: group reviews by stars
```
docker run -e "SPARK_CLASS=com.demo.spark.YelpGroupReviewsByStars" -e "SPARKMASTER=local" kramalingam/spark-driver 
```

use case 2: top 10 business by category Restaurants
```
docker run -e "SPARK_CLASS=com.demo.spark.YelpTop10BusinessByCategories" -e "SPARKMASTER=local" kramalingam/spark-driver
```

To launch spark driver on mesos spark master (not tested with SMACK Sandbox)
```
docker run -e "SPARK_CLASS=com.demo.spark.YelpGroupReviewsByStars" -e "SPARKMASTER=mesos://zk://192.168.99.100:2181/mesos" kramalingam/spark-driver 
```

#### Step 6: Interactive data analysis with Apache Zeppelin
```
docker pull kramalingam/spark-zeppelin

docker run --rm -p 8080:8080 kramalingam/spark-zeppelin &
```
Zeppelin will be running at http://192.168.0.50:8080 and Please import sample zeppelin notebook from https://github.com/kartik-dev/yelp_academic_dataset_review/blob/master/scripts/Yelp-Dataset-Challenge.json

##### Yelp data challenge -use cases:
1. Top 10: coolest restaurants
2. Top 10: business with most of the reviews
3. Top 10 review count on specific time of year
4. Average Review count by Business and city
5. Top 10 users by review count
6. Top 10 Business by category restaurants

## Docker Images

kramalingam/spark - Dockerized spark binaries as base image for driver application and zeppelin to use

kramalingam/spark-zeppelin - Dockerized zeppelin for beautiful data-driven, interactive and collaborative documents with SQL, Scala

kramalingam/spark-driver - Dockerized spark driver application with YelpGroupReviewsByStars and YelpTop10BusinessByCategories jobs.

## Rebuild and deploy Docker images

#### Rebuild docker image of Spark base
```
cd /root/yelp_academic_dataset_review

docker build -t kramalingam/spark -f SparkBaseDockerImage .
```
Dockerfile
```
FROM java:8 

RUN apt-get update
RUN apt-get install -y maven

RUN wget http://d3kbcqa49mib13.cloudfront.net/spark-2.0.0-bin-hadoop2.7.tgz

RUN tar -xzf spark-2.0.0-bin-hadoop2.7.tgz

RUN mv spark-2.0.0-bin-hadoop2.7 /opt/spark

EXPOSE 8080
```
#### Rebuild Docker image of Spark Driver Application

Note that this will take a while when you start it for the first time since it downloads and installs maven and downloads all the project’s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

The pom.xml contains a very basic Maven configuration. It configures the Spark 2.0 dependencies using a Java 1.8 compiler and creates a fat jar with all the dependencies.

sbt build tool could be used in place of maven. This could be easily be replaced in Dockerfile
```
cd /root/yelp_academic_dataset_review

mvn clean compile package

docker build -t kramalingam/spark-driver -f SparkDriverDockerImage .
```
Dockerfile
```
#using the spark-docker image we just created as our base image
FROM kramalingam/spark

#app.jar is our Fat Jar to be run; here we assume it’s in the same build context as the Dockerfile;
COPY target/yelp-academic-dataset-review-0.7-jar-with-dependencies.jar /opt/yelp-academic-dataset-review-0.7-jar-with-dependencies.jar

#calling the spark-submit command; with the --class argument being an input environment variable
CMD /opt/spark/bin/spark-submit --class $SPARK_CLASS --master $SPARKMASTER /opt/yelp-academic-dataset-review-0.7-jar-with-dependencies.jar
```

Once the image is built, submit spark application. Spark application will be deployed on standalone spark.
```
docker run -e "SPARK_CLASS=com.demo.spark.YelpGroupReviewsByStars" kramalingam/spark-driver 
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

ADD scripts/Yelp-Dataset-Challenge.json $ZEPPELIN_NOTEBOOK_DIR/2BTRWA9EV/Yelp-Dataset-Challenge.json

WORKDIR $ZEPPELIN_HOME
CMD ["bin/zeppelin.sh"]
```

## Advanced analytics Use cases (Spark MLlib could be leveraged for this)
- Review Rating Prediction 
- Sentiment Analysis
- Natural Language Processing (NLP)
- Social Graph Mining

## Things to improve
- [ ] Deploy spark on Mesos Marathon for better resource utilization and high availability 
- [ ] Deploy Spark driver application with Marathon for spark driver fault tolerance and high availability  
- [ ] Use of Dataflow tools like NiFi, StreamSets for enabling accelerated data collection, curation, analysis and delivery in real-time 