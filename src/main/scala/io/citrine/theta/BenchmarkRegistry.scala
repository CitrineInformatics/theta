package io.citrine.theta

import io.citrine.theta.benchmarks.{Benchmark, RandomGenerationBenchmark, StreamBenchmark}

import scala.collection.mutable

/**
  * Registry of benchmarks and their run-times
  *
  * Created by maxhutch on 3/29/17.
  */
object BenchmarkRegistry {

  /**
    * Get a time from the benchmark registry
    * @param name of the benchmark to get the time of
    * @return the time in seconds
    */
  def getTime(name: String): Double = {
    if (name == "None") return 1.0
    if (name == "Default") return getTime(default)

    times.get(name) match {
      case Some(x) => x
      case None =>
        benchmarks.get(name) match {
          case None => throw new IllegalArgumentException("Unknown benchmark name")
          case Some(bm) =>
            bm.run()
            val time = bm.run()
            times(name) = time
            time
        }
    }
  }

  /**
    * Get the event count associated with the benchmark
    * @param name of the benchmark
    * @return numer of events that were benchmarked
    */
  def getCount(name: String): Long = {
    if (name == "None") return 1L
    if (name == "Default") return getCount(default)

    benchmarks.get(name) match {
      case None => throw new IllegalArgumentException()
      case Some(bm) => bm.getCount()
    }
  }

  /**
    * Register a benchmark in the registry
    * @param name of the benchmark
    * @param bm object to register
    */
  def register(name: String, bm: Benchmark): Unit = {
    benchmarks(name) = bm
  }

  /**
    * Pre-compute all the benchmark timings
    */
  def preheat(): Unit = {
    benchmarks.keySet.foreach(getTime)
  }

  /** Map from name to benchmark object */
  val benchmarks: mutable.Map[String, Benchmark] = mutable.HashMap()
  register("RandomGeneration", RandomGenerationBenchmark)
  register("STREAM", new StreamBenchmark())

  /** Map from name to benchmark time */
  val times: mutable.Map[String, Double] = mutable.HashMap()

  val default: String = "RandomGeneration"
}
