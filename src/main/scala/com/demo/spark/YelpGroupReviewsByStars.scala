package com.demo.spark

import org.apache.spark.sql.SparkSession

object YelpGroupReviewsByStars {
  def main(args: Array[String]): Unit = {
    val warehouseLocation = "hdfs://192.168.0.50:8020/spark-warehouse"

    val spark = SparkSession
      .builder()
      .appName("Yelp academic dataset example")
      .config("spark.cassandra.connection.host", "127.0.0.1")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val inputDF = spark.read.json("hdfs://192.168.0.50:8020/yelp-dataset/yelp_academic_dataset_review.json")

    val outputDF1 = inputDF
      .groupBy(inputDF("stars")) // Group by stars
      .count // Aggregate distinct hotels
      .withColumnRenamed("count", "stars_count")

    outputDF1.show
    
    outputDF1.write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "reviews_fact", "keyspace" -> "yelp_dataset"))
      .save()
  }
}