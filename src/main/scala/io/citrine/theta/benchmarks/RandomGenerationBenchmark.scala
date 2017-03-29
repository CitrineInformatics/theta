package io.citrine.theta.benchmarks

import scala.util.Random

/**
  * Benchmark that generates random numbers
  * Created by maxhutch on 3/29/17.
  */
object RandomGenerationBenchmark extends Benchmark {

  /**
    * Generate 2^20 random numbers using Scala.Random.nextDouble()
    */
  override def kernel(): Unit = {
    (0 until 1048576).foreach{i =>
      Random.nextDouble()
    }
  }
}
