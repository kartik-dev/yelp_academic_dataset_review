name := "Yelp_academic_dataset_review"

version := "1.0"

val sparkVersion = "2.0.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion  % "provided"

libraryDependencies  += "org.apache.spark" % "spark-sql_2.11" % sparkVersion % "provided"

libraryDependencies  += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies ++= Seq ("joda-time" % "joda-time" % "2.8.2","org.joda" % "joda-convert" % "1.7")