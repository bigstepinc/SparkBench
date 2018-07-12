# SparkBench
Terasort-like benchmark for spark 2.x that uses dataframes, saves files in parquet etc for a more realistic testing.
Row size is 100 characters (unicode).

To generate test data execute: 1m rows (100MB dataset):
```
~/spark-2.3.0-bin-hadoop2.7/bin/spark-submit sparkbench_2.11-1.0.jar generate 1000000 /input
```
To sort the test data:
```
~/spark-2.3.0-bin-hadoop2.7/bin/spark-submit sparkbench_2.11-1.0.jar sort /input /output
```
