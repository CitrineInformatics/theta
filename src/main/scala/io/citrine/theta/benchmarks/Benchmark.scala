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
    Stopwatch.time(kernel(), benchmark = "None", nWarm = 16, nTrial = 8)
  }

  /**
    * Workload that will be timed for the benchmark
    */
  def kernel(): Unit
}
