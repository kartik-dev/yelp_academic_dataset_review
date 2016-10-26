##### Clone git repository

git clone https://github.com/kartik-dev/yelp_academic_dataset_review.git

##### Build Spark Base docker Image

docker build -t newyorker/spark -f SparkBaseDockerImage .

##### Spark Driver docker Image

docker build -t newyorker/spark-driver -f SparkDriveDockerImage .

##### Submit Spark application

docker run --net spark_network -e "SPARK_CLASS=com.demo.spark.YelpReviewsByUser" newyorker/spark-driver 