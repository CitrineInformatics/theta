package io.citrine.theta

import io.citrine.theta.benchmarks.{RandomGenerationBenchmark, StreamBenchmark}
import org.junit.Test

import scala.util.Random

/**
  * Created by maxhutch on 3/29/17.
  */
@Test
class StopwatchTest {

  /**
    * Test that the non-dimensional time of the kernel is 1
    */
  @Test
  def testTimeDefault(): Unit = {
    val theta: Double = Stopwatch.time(RandomGenerationBenchmark.kernel(), benchmark = "RandomGeneration")
    assert(theta < 1.2, s"RandomGeneration benchmark inconsistent (too slow ${theta})")
    assert(theta > 0.8, s"RandomGeneration benchmark inconsistent (too fast ${theta})")
  }

  @Test
  def testTimeStream(): Unit = {
    val a: Array[Double] = new Array[Double](StreamBenchmark.N)
    val b: Array[Double] = new Array[Double](StreamBenchmark.N)
    val c: Array[Double] = new Array[Double](StreamBenchmark.N)

    val theta: Double = Stopwatch.time({StreamBenchmark.customKernel(a, b, c, Random.nextDouble())}, benchmark = "STREAM")
    assert(theta < 1.2, s"STREAM benchmark inconsistent (too slow ${theta})")
    assert(theta > 0.8, s"STREAM benchmark inconsistent (too fast ${theta})")
  }

}

object StopwatchTest {
  def main(argv: Array[String]): Unit = {
    val timee = Stopwatch.time(RandomGenerationBenchmark.kernel(), benchmark = "RandomGeneration")
    println(s"Timed at ${timee}")

    {
      (0 until 64).foreach { i =>
        val start = System.nanoTime()
        RandomGenerationBenchmark.kernel()
        println(s"Took ${System.nanoTime() - start}")
      }
    }

    {
      (0 until 64).foreach { i =>
        val time = Stopwatch.time(RandomGenerationBenchmark.kernel(), benchmark = "RandomGeneration")
        println(s"Timed at ${time}")
      }
    }
  }
}
