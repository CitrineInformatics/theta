package io.citrine.theta.benchmarks

import io.citrine.theta.Stopwatch

/**
  * Benchmarks that can be used to non-dimensionalize time
  * Created by maxhutch on 3/29/17.
  */
trait Benchmark {

  /**
    * Run the benchmark a couple times to get a solid reading
    *
    * @return the benchmark time (in seconds)
    */
  def run(): Double = {
    setup()
    val result = Stopwatch.time(kernel(), benchmark = "None")
    teardown()
    result
  }

  /**
    * Workload that will be timed for the benchmark
    */
  def kernel(): Unit

  /**
    * Get the number of event counts represented by this benchmark
    * @return number of events benchmarked
    */
  def getCount(): Long = 1L

  /**
    * Setup the benchmark, allocating test data for example
    */
  def setup(): Unit = {}

  /**
    * Tear down the benchmark, freeing test data for example
    */
  def teardown(): Unit = {}
}

trait BenchmarkBuilder {
  def build(): Benchmark
}
