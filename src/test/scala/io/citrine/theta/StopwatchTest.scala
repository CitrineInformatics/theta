package io.citrine.theta

import io.citrine.theta.benchmarks.{Benchmark, RandomGenerationBenchmark, StreamBenchmark}
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

  /**
    * Test that the stopwatch can detect whether a benchmark with a known execution time is slower (or faster) than a specified time.
    */
  @Test
  def testIsNotSlowerThanAnEqualTime(): Unit = {
    val b = new RandomSleepBenchmark()
    val equalTime = b.mean
    assert(
      !Stopwatch.isSlowerThan(b.kernel(), equalTime, minimumTimeDifference = Some(b.std / 1000)), "Unable to determine a benchmark is not slower than the known average execution time."
    )
  }

  /**
    * Test that the stopwatch can detect whether a benchmark with a known execution time is slower (or faster) than a specified time.
    */
  @Test
  def testIsSlowerThan(): Unit = {
    val b = new RandomSleepBenchmark()
    val effectSizes = Seq(0.5, 0.8) // test medium and large effect sizes
    effectSizes.foreach { effectSize =>
      val delta = b.std * effectSize
      val fasterTime = (b.mean - delta) / 1000
      val slowerTime = (b.mean + delta) / 1000
      assert(
        Stopwatch.isSlowerThan(b.kernel(), fasterTime), s"Unable to determine a benchmark is slower than a known faster time with effect size $effectSize."
      )
      assert(
        !Stopwatch.isSlowerThan(b.kernel(), slowerTime), s"Unable to determine a benchmark is faster than a known slower time with effect size $effectSize."
      )
    }
  }

  /**
    * Test that the stopwatch whether the stopwatch will exit early if an acceptable effect size is measured
    */
  @Test
  def testIsSlowerThanEffectSizeEarlyExit(): Unit = {
    val b = new RandomSleepBenchmark()

    // the measured effect size should by +/- 2 (much larger than +/- 0.5 required to terminate the test)
    val muchFasterTime = (b.mean - b.std * 2) / 1000
    val muchSlowerTime = (b.mean + b.std * 2) / 1000

    // set FP/FN rates so low that the required number of samples won't be satisfied before an acceptable effect size is measured
    val stringentProbability = 1.0e-6

    val minRun = 4
    val maxExpectedDuration = b.mean * (minRun + 1)

    val start1 = System.nanoTime()
    val isSlowerThanFasterTime = Stopwatch.isSlowerThan(b.kernel(), muchFasterTime, minRun = minRun, acceptableEffectSize = Some(0.5), falseNegative = stringentProbability, falsePositive = stringentProbability)
    val duration1 = (System.nanoTime() - start1) * 1.0e-9
    assert(duration1 < maxExpectedDuration, s"Benchmark did not exit early when effect size is much larger than acceptable limit.")
    assert(isSlowerThanFasterTime, "Unable to determine a benchmark is slower than a known faster time after measuring acceptable effect size.")

    val start2 = System.nanoTime()
    val isSlowerThanSlowerTime = Stopwatch.isSlowerThan(b.kernel(), muchSlowerTime, minRun = minRun, acceptableEffectSize = Some(-0.5), falseNegative = stringentProbability, falsePositive = stringentProbability)
    val duration2 = (System.nanoTime() - start2) * 1.0e-9
    assert(duration2 < maxExpectedDuration, s"Benchmark did not exit early when effect size is much larger than acceptable limit.")
    assert(!isSlowerThanSlowerTime, "Unable to determine a benchmark is faster than a known slower time after measuring acceptable effect size.")
  }
}

/**
  * Test benchmark that sleeps for a random amount of time sampled from a normal distribution.
  */
class RandomSleepBenchmark() extends Benchmark {
  /** Average sleep time (in ms) */
  val mean = 100.0 // ms

  /** Standard deviation about the mean sleep time (in ms) */
  val std = 10.0 // ms

  /** Kernel that sleep for a random amount of time */
  override def kernel(): Unit = {
    val r = mean + Random.nextGaussian() * std
    Thread.sleep(r.toLong)
  }
}

object StopwatchTest {
  def main(argv: Array[String]): Unit = {
    new StopwatchTest().testTimeDefault()
    new StopwatchTest().testTimeStream()
  }
}
