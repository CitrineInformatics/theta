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
    val theta: Double = Stopwatch.time(new RandomGenerationBenchmark().kernel(), benchmark = "RandomGeneration")
    assert(theta < 1.2, s"RandomGeneration benchmark inconsistent (too slow ${theta})")
    assert(theta > 0.8, s"RandomGeneration benchmark inconsistent (too fast ${theta})")
  }

  /**
    * Test that the non-dimensional time of the STREAM benchmark is 1
    */
  @Test
  def testTimeStream(): Unit = {
    val benchmark = new StreamBenchmark()
    benchmark.setup()
    val theta: Double = Stopwatch.time({benchmark.kernel()}, benchmark = "STREAM")
    benchmark.teardown()
    assert(theta < 1.2, s"STREAM benchmark inconsistent (too slow ${theta})")
    assert(theta > 0.8, s"STREAM benchmark inconsistent (too fast ${theta})")
  }

}

object StopwatchTest {
  def main(argv: Array[String]): Unit = {
    new StopwatchTest().testTimeDefault()
    new StopwatchTest().testTimeStream()
  }
}
