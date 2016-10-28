package com.demo.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType

case class Review(review_id: String, user_id: String, stars: Float, date: String, text: String, textType: String, business_id: String, votes: Map[String, Integer])

object CassandraLoader {
  def main(args: Array[String]): Unit = {
    val warehouseLocation = "hdfs://192.168.0.50:8020/spark-warehouse"

    val spark = SparkSession
      .builder()
      .appName("Yelp academic dataset example")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .config("spark.cassandra.connection.host", "127.0.0.1")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    val df = spark.read.json("hdfs://192.168.0.50:8020/yelp-dataset/yelp_academic_dataset_review.json")

    df.show()

    df.printSchema()

    df.write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "reviews", "keyspace" -> "yelp_dataset"))
      .save()
  }
}