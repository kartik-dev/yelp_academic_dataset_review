#specifying our base docker-image
FROM ubuntu:14.04
 
####installing [software-properties-common] so that we can use [apt-add-repository] to add the repository [ppa:webupd8team/java] form which we install Java8
RUN apt-get install software-properties-common -y
RUN apt-add-repository ppa:webupd8team/java -y
RUN apt-get update -y
 
####automatically agreeing on oracle license agreement that normally pops up while installing java8
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
 
####installing java
RUN apt-get install -y oracle-java8-installer
#####################################################################################
####downloading and unpacking Spark 1.6.1 [prebuilt for Hadoop 2.6+ and scala 2.10]
RUN wget http://apache.mirror.triple-it.nl/spark/spark-1.6.1/spark-1.6.1-bin-hadoop2.6.tgz
RUN tar -xzf spark-1.6.1-bin-hadoop2.6.tgz
 
####moving the spark root folder to /opt/spark
RUN mv spark-1.6.1-bin-hadoop2.6 /opt/spark
 
####exposing port 8080 so we can later access the Spark master UI; to verify spark is running …etc.
EXPOSE 8080
