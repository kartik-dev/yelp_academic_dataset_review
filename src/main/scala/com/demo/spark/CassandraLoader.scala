package com.demo.spark

import org.apache.spark.sql.SparkSession
import com.datastax.spark.connector._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object CassandraLoader {
  def main(args: Array[String]): Unit = {
    val warehouseLocation = "hdfs://192.168.0.50:8020/spark-warehouse"

    val spark = SparkSession
      .builder()
      .appName("Yelp academic dataset example")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .config("spark.cassandra.connection.host", "127.0.0.1")
      .getOrCreate()

    val df = spark.read.json("hdfs://192.168.0.50:8020/yelp-dataset/yelp_academic_dataset_review.json")

    df.show()
    
    df.printSchema()
    
    df.write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "reviews", "keyspace" -> "yelp_dataset"))
      .save()

  }
}