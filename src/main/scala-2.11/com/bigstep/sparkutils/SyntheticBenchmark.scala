package com.bigstep.sparkutils

import org.apache.log4j.LogManager
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

/**
  * This class provides terasort-like capabilities but using dataframes and the parquet file format.
  * Compression is enabled by default but can be disabled.
  * Created by Alex Bordei on 5/26/18.
  *
  */
class SyntheticBenchmark(dataFrame:DataFrame=null, options:Map[String,String] = Map()) {

  val logger = LogManager.getLogger("com.bigstep.Benchmark")
  private val spark = SparkSession.getDefaultSession.get

  def getDF:DataFrame = dataFrame

  def options(opt:Map[String,String]): SyntheticBenchmark =
    SyntheticBenchmark.create(dataFrame, options)


  def generateRecords(rowCount:Long):SyntheticBenchmark =  {

    val chars = 'A' to 'Z'
    val randValue = udf( (rowId:Long) => {
      val rnd = new scala.util.Random(rowId)
      //rnd.nextString(100)  //not exactly like the original terasort but very similar.
      (1 to 100).map( i => chars(rnd.nextInt(chars.length))).mkString
    })

    val df=spark.range(rowCount)
                .toDF("rowId")
                .repartition(spark.sparkContext.getExecutorMemoryStatus.size-1) //this repartitions to the number of workers to increase performance

    val df2=df.withColumn("value", randValue(df("rowId")))


    new SyntheticBenchmark(df2)
  }

  def sortByValue():SyntheticBenchmark =
    new SyntheticBenchmark(dataFrame.orderBy(dataFrame("value")))

  def save(outputDirectory:String): Unit =
    dataFrame
      .write.options(options)
      .parquet(outputDirectory)

   def load(inputDirectory:String): SyntheticBenchmark =
    new SyntheticBenchmark(spark.read.options(options).parquet(inputDirectory))

}

object SyntheticBenchmark
{
  def create():SyntheticBenchmark =
    new SyntheticBenchmark(null)

  def create(dataFrame: DataFrame):SyntheticBenchmark =
    new SyntheticBenchmark(dataFrame)

  def create(dataFrame: DataFrame, options: Map[String, String]):SyntheticBenchmark =
    new SyntheticBenchmark(dataFrame, options)

  def load(inputDirectory:String):SyntheticBenchmark =
    create().load(inputDirectory)

  def load(inputDirectory:String, options: Map[String, String]):SyntheticBenchmark =
    create(null, options).load(inputDirectory)
}