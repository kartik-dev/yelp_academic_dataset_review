package com.demo.spark

import org.apache.spark.sql.SparkSession

object YelpReviewsByUser {
  def main(args: Array[String]): Unit = {
    val warehouseLocation = "hdfs://192.168.0.50:8020/spark-warehouse"

    val spark = SparkSession
      .builder()
      .appName("Yelp academic dataset example")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .getOrCreate()

    val df = spark.read.json("hdfs://192.168.0.50:8020/yelp-dataset/yelp_academic_dataset_review.json")
    df.show

    df.printSchema()
  }
}