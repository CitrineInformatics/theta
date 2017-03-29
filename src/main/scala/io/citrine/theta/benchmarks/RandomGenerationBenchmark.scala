package io.citrine.theta.benchmarks

import io.citrine.theta.Stopwatch

import scala.util.Random

/**
  * Created by maxhutch on 3/29/17.
  */
object RandomGenerationBenchmark extends Benchmark {
  /**
    * Run the benchmark
    *
    * @return the benchmark time (in seconds)
    */
  override def run(): Double = {
    Stopwatch.time(kernel, benchmark = "None", nWarm = 8, nTrial = 8)
  }

  def kernel(): Unit = {
    (0 until 4 * 1048576).foreach{i =>
      Random.nextDouble()
    }
  }
}
