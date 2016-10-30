package com.demo.spark

import org.apache.spark.sql.SparkSession

object YelpTop10BusinessByCategories {
  def main(args: Array[String]): Unit = {
    val warehouseLocation = "hdfs://192.168.0.50:8020/spark-warehouse"

    val spark = SparkSession
      .builder()
      .appName("Yelp academic dataset example")
      .config("spark.cassandra.connection.host", "192.168.0.50")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val business = spark.read.json("hdfs://192.168.0.50:8020/yelp-dataset/yelp_academic_dataset_business.json")

    business.createOrReplaceTempView("business")

    val outputDF1 = spark.sql("SELECT name,state,city,review_count, cat FROM business LATERAL VIEW explode(categories) tab AS cat WHERE cat = 'Restaurants' ORDER BY review_count DESC LIMIT 10")

    outputDF1.show

    outputDF1.write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "top_10restaurants", "keyspace" -> "yelp_dataset"))
      .save()
  }
}