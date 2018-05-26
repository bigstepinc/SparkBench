
name := "SparkBench"

version := "1.0"

scalaVersion := "2.11.12"


libraryDependencies ++= Seq(
  "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
  "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.8.2" % Runtime
)



libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.3.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

mainClass in Compile := Some("com.bigstep.sparkutils.Benchmark")