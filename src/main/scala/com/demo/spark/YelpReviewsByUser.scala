package com.demo.spark

import org.apache.spark.sql.SparkSession

object YelpReviewsByUser {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Spark SQL basic example")
      .getOrCreate()

    val df = spark.read.json("/yelp-dataset/yelp_academic_dataset_review.json")
    df.show

    df.printSchema()
  }
}