package com.bigstep.sparkutils

import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession

import scala.concurrent.duration._

/**
  * Terasort like benchmark that uses dataframes and parquet file formats for a more
  * realistic test scenario.
  * "Generates a dataframe with the following schema:
  * key:binary(10), value:string(78) record_count
  * records of 100 Bytes each (before compression). They are key-value pairs"
  */
object Benchmark {

  val logger = LogManager.getLogger("com.bigstep.Benchmark")

   def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      System.err.print(
        "Missing application startup params:\n" +
        "\tgenerate <record_count> <output_dir>\n" +
        "\tsort <input_dir> <output_dir>\n")
      System.exit(-1)
    }
    val spark = SparkSession.builder.appName("SparkBenchmark").getOrCreate()
    val bench =  SyntheticBenchmark.create()

    val t0 = System.nanoTime()

    args(0) match {
      case "generate" => bench.generateRecords(args(1).toLong)
        .save(args(2))
      case "sort" => bench.load(args(1))
        .sortByValue()
        .save(args(2))
    }

    val t1 = System.nanoTime()
    val duration  = Duration(t1 - t0, NANOSECONDS).toSeconds

    logger.info(s"Total execution time: ${duration}s")

  }
}
