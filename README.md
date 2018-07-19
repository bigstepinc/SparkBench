# SparkBench [![Build Status](https://travis-ci.org/bigstepinc/SparkBench.svg?branch=master)](https://travis-ci.org/bigstepinc/SparkBench)
Terasort-like synthetic benchmark for Apache Spark 2.x that uses dataframes, parquet etc for a more realistic load testing. It is both IO, network and CPU intensive. It also puts pressure on the temporary directories and other configuration settings.

You can download the binaries from the [Releases](https://github.com/bigstepinc/SparkBench/releases).

Usage: 
```
spark-submit sparkbench.jar generate <number_of_rows> <output_directory> [noPartitions=10]
            Generates a dataframe with the specified number of rows where each row has a value field with 100 characters.
spark-submit sparkbench.jar sort <output_directory> <output_directory_sorted> [noPartitions=10]
            Sorts the dataframe generated by 'generate' from <output_directory> and saves the output in the <output_directory_sorted> directory.
```

- One million rows (1000000) is approximately 100MB of data.
- Ten million rows (10000000) is approximately 1GB of data. 
- Ten billion rows (10000000000) is approximately 1TB of data.

For maximum performance you might want to set the number of partitions (noPartitions) to the number of executors in the cluster. For each partition a different parquet file will be generated. This allows you to control the size of the files being uploaded. Too many small files might generate listing delays but too few will only use part of the cluster for uploading and consume a lot of temporary space.
Please note that the temporary/shuffle space (controlled with spark.local.dir, spark.externalBlockStore.baseDir, SPARK_WORKER_DIR, SPARK_LOCAL_DIRS) needs to be large enough and fast enough to support the process or will otherwise become a bottleneck before your CPU, network or main storage subsystem. It is recommended that you use local NVMe drives, perhaps as a stripped LVM volume, or a RAM disk.        

To generate test data execute: 1m rows (100MB dataset):
```
spark-submit sparkbench_2.11-1.0.14.jar generate 1000000 /input
```
To sort the test data:
```
spark-submit sparkbench_2.11-1.0.14.jar sort /input /output
```

## Using with S3:


Setup the *conf/core-site.xml* for S3:
```
<configuration>
<property>
  <name>fs.s3a.impl</name>
  <value>org.apache.hadoop.fs.s3a.S3AFileSystem</value>
</property>
<property>
  <name>fs.s3a.access.key</name>
  <value>ACCESSKEY</value>
</property>
<property>
  <name>fs.s3a.secret.key</name>
  <value>SECRETKEY</value>
</property>
<property>
  <name>fs.s3a.endpoint</name>
  <value>https://object.ecstestdrive.com</value>
</property>
<property>
  <name>fs.s3a.connection.ssl.enabled</name>
  <value>enabled</value>
</property>
<property>
  <name>fs.s3a.signing-algorithm</name>
  <value>S3SignerType</value>
</property>
</configuration>
```
The S3 implementation uses the java temporary directory so you might want to set this to your fast scratch space dir in *conf/spark-env.conf*.
```
 SPARK_DAEMON_JAVA_OPTS=-Djava.io.tmpdir=/mnt/nvme1/java-tmp-dir"
```
... and generate and sort the data:
```
spark-submit --packages org.apache.hadoop:hadoop-aws:2.7.3,org.apache.hadoop:hadoop-common:2.7.3 sparkbench_2.11-1.0.14.jar generate 1000000 s3a://sparkgen-1m
spark-submit --packages org.apache.hadoop:hadoop-aws:2.7.3,org.apache.hadoop:hadoop-common:2.7.3 sparkbench_2.11-1.0.14.jar sort s3a://sparkgen-1m s3a://sparkgen-1m-sorted
```

## Using with Google GCS:
Setup the conf/core-site.xml in spark

```
<property>
  <name>google.cloud.auth.service.account.enable</name>
  <value>true</value>
</property>
<property>
  <name>google.cloud.auth.service.account.json.keyfile</name>
  <value>JSON_KEY_FILE</value>
</property>
<property>
  <name>fs.gs.impl</name>
  <value>com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem</value>
</property>
<property>
  <name>fs.AbstractFileSystem.gs.impl</name>
  <value>com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem</value>
</property>
<property>
  <name>fs.gs.project.id</name>
  <value>GCP_PROJECT_ID</value>
</property>
```
Generate and sort data:
```
spark-submit --packages com.google.cloud.bigdataoss:gcs-connector:1.8.1-hadoop2 sparkbench_2.11-1.0.14.jar generate 1000000 gs://sparkgen-1m
spark-submit --packages com.google.cloud.bigdataoss:gcs-connector:1.8.1-hadoop2 sparkbench_2.11-1.0.14.jar sort gs://sparkgen-1m gs://sparkgen-1m-sorted
```
Note: If you get something like this:
```
Exception in thread "main" java.lang.NoSuchMethodError: com.google.common.base.Splitter.splitToList(Ljava/lang/CharSequence;)Ljava/util/List;
```
Make sure to use the following configuration variables:
```
spark.driver.userClassPathFirst=true
spark.executor.userClassPathFirst=true
```
## Using programmatically
To use in your project as a library use something like:

```
libraryDependencies += "com.bigstep" %% "sparkbench_2.11" % "1.0.14" 
```
or
```
spark-submit --packages com.bigstep:sparkbench_2.11:1.0.14
```
... and use in your program (Scala/Java):
```
import com.bigstep.sparkutils._
//generate values and save them
spark.time(SyntheticBenchmark.create().generateRecords(1000).save("/input"))
//sort values and save them.
spark.time(SyntheticBenchmark.load("/input").sortByValue().save("/output"))
```

You can also setup options such as the compression codec:
```
SyntheticBenchmark.create()
                  .options(Map("spark.io.compression.codec"->"snappy"))
                  .generateRecords(1000)
                  .save("/input")
```

Note:  When writing to S3, GCS or any other object storage systems where the rename operation is expensive or your temporary directory is not fast enough you should use the following setting to:
```
spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version 2
```

