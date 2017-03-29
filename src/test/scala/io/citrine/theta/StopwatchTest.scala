package io.citrine.theta

import io.citrine.theta.benchmarks.RandomGenerationBenchmark
import org.junit.Test

/**
  * Created by maxhutch on 3/29/17.
  */
@Test
class StopwatchTest {

  /**
    * Test that the non-dimensional time of the kernel is 1
    */
  @Test
  def testTime(): Unit = {
    val theta: Double = Stopwatch.time(RandomGenerationBenchmark.kernel(), benchmark = "RandomGeneration")
    assert(theta < 1.2, s"RandomGeneration benchmark inconsistent (too slow ${theta})")
    assert(theta > 0.8, s"RandomGeneration benchmark inconsistent (too fast ${theta})")
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
