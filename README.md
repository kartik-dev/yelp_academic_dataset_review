##### Submit Streaming spark job (for Scenario 4)

`spark-submit --class com.demo.spark.ClickLogStreamingJob --master local[2]  ClickLogAnalytics-assembly-1.0.jar`

`spark-submit --class com.demo.spark.ClickLogStreamingToHDFS --master local[2]  ClickLogAnalytics-assembly-1.0.jar`

##### Submit Batch spark job (for Scenario 5)
`spark-submit --class com.demo.spark.ClickLogBatchJob --master local[2]  ClickLogAnalytics-assembly-1.0.jar`

##### Create Kafka Topic
`/home/vagrant/confluent-3.0.1/bin/kafka-topics --zookeeper 192.168.0.50:2181 --create --topic clicklog --partitions 1 --replication-factor 1`