# SparkBench [![Build Status](https://travis-ci.org/bigstepinc/SparkBench.svg?branch=master)](https://travis-ci.org/bigstepinc/SparkBench)
Terasort-like benchmark for spark 2.x that uses dataframes, saves files in parquet etc for a more realistic testing.
Row size is 100 characters (unicode).

You can download the binaries from the [Releases](https://github.com/bigstepinc/SparkBench/releases).

To generate test data execute: 1m rows (100MB dataset):
```
~/spark-2.3.0-bin-hadoop2.7/bin/spark-submit sparkbench_2.11-1.0.jar generate 1000000 /input
```
To sort the test data:
```
~/spark-2.3.0-bin-hadoop2.7/bin/spark-submit sparkbench_2.11-1.0.jar sort /input /output
```

Note: the current implementation forces a repartition with the numebr of partitions equal to the number of workers available in the cluster.


## Using programatically
To use in your project as a library use something like:
```
//generate values and save them
SyntheticBenchmark.create().generateRecords(1000).save("/input")
//sort values and save them.
SyntheticBenchmark.load("/input").sortByValue().save("/output")
```

```
libraryDependencies += "com.bigstep" %% "sparkbench_2.11" % "1.0" 
```
or
```
spark-submit --packages com.bigstep:sparkbench_2.11:1.0
```