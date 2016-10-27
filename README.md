### Build Instructions

##### 1. Clone git repository

``git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git``

Upload sample dataset

``sh scripts/yelp-data-upload.sh /vagrant/resources/yelp_dataset_challenge_academic_dataset.tar``

##### 2. Dockerizing Spark - Build Spark Base docker Image

``docker build -t newyorker/spark -f SparkBaseDockerImage .``

##### 3. Build docker image of Spark Driver Image

Note that this will take a while when you start it for the first time since it downloads and installs sbt and downloads all the project’s dependencies. Every subsequent start of this build will only take a few seconds, as again everything will be already cached

``docker build -t newyorker/spark-driver -f SparkDriverDockerImage .``

##### 4. Setup docker network for spark

``docker network create spark_network;``

##### 5. Submit Spark application
<span style="color:red">
 
`` docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpReviewsByUser" newyorker/spark-driver ``

</span>  
 