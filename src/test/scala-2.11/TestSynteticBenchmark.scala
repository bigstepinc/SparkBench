import java.io.File

import com.bigstep.sparkutils.SyntheticBenchmark
import org.apache.spark.sql.SparkSession
import org.scalatest._
import org.apache.commons.io.FileUtils

class TestSynteticBenchmark extends FlatSpec with BeforeAndAfter with Matchers {

  SparkSession.builder.master("local[2]").appName("SparkBenchmark").getOrCreate()

  val bench =  SyntheticBenchmark.create().generateRecords(1000)

  "Benchmark" should "be able to generate proper values" in {

    bench.getDF.count() should be(1000)
    bench.getDF.columns should contain theSameElementsAs Array("rowId", "value")
    bench.getDF.takeAsList(1).get(0).getLong(0) should be(0)
    bench.getDF.takeAsList(1).get(0).getString(1).length should be(100) //100 chars long

  }

  "Benchmark" should "be able to sort values" in {

    bench.sortByValue().getDF.collect() should contain theSameElementsAs bench.getDF.orderBy("value").collect()

  }



  "Benchmark" should "be able to save and load values" in {

    val path = System.getProperty("java.io.tmpdir")+"/spark-input"
    bench.save(path)

    SyntheticBenchmark.load(path).getDF.collect() should contain theSameElementsAs bench.getDF.collect()


    FileUtils.deleteDirectory(new File(path))

  }

  "Benchmark" should "receive options" in {

    val path = System.getProperty("java.io.tmpdir")+"/spark-input-2"
    SyntheticBenchmark.create().options(Map("spark.io.compression.codec"->"snappy")).generateRecords(10).save(path)
    SyntheticBenchmark.load(path, Map("spark.io.compression.codec"->"snappy"))
    FileUtils.deleteDirectory(new File(path))
  }

}
