package io.citrine.theta.benchmarks

import scala.util.Random

/**
  * Benchmark that generates random numbers
  * Created by maxhutch on 3/29/17.
  */
class RandomGenerationBenchmark() extends Benchmark {

  val N: Long = 8 * 1048576L

  /**
    * Generate 2^23 random numbers using Scala.Random.nextDouble()
    */
  override def kernel(): Unit = {
    (0 until N.toInt).foreach{i =>
      Random.nextDouble()
    }
  }

  override def getCount(): Long = N
}

object RandomGenerationBenchmark extends BenchmarkBuilder {
  override def build(): Benchmark = new RandomGenerationBenchmark()
}
