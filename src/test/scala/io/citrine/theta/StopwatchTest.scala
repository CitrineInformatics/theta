package io.citrine.theta

import io.citrine.theta.benchmarks.{Benchmark, RandomGenerationBenchmark, StreamBenchmark}
import org.apache.commons.math3.distribution.NormalDistribution
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
    val equalTime = b.mean / 1000
    assert(
      !Stopwatch.isSlowerThan(b.kernel(), equalTime, minimumTimeDifference = b.std / 1000), "Unable to determine a benchmark is not slower than the known average execution time."
    )
  }

  /**
    * Test that the stopwatch can detect whether a benchmark with a known execution time is slower (or faster) than a specified time.
    */
  @Test
  def testIsSlowerThanMinimumTimeDifference(): Unit = {
    val b = new RandomSleepBenchmark()
    val fasterTime = (b.mean - 3 * b.std) / 1000
    assert(
      !Stopwatch.isSlowerThan(b.kernel(), fasterTime, minimumTimeDifference = 4 * b.std / 1000), "Unable to determine a benchmark is not slower than the known average execution time."
    )
    assert(
      Stopwatch.isSlowerThan(b.kernel(), fasterTime, minimumTimeDifference = 2 * b.std / 1000), "Unable to determine a benchmark is not slower than the known average execution time."
    )

    // check that minimum time difference does not affect the calculation when the sample mean is less than the hypothesized mean
    val slowerTime = (b.mean + 3 * b.std) / 1000
    assert(
      !Stopwatch.isSlowerThan(b.kernel(), slowerTime, minimumTimeDifference = 10 * b.std / 1000), "Unable to determine a benchmark is not slower than the known average execution time."
    )
  }

  /**
    * Test that the stopwatch can detect whether a benchmark with a known execution time is slower (or faster) than a specified time.
    */
  @Test
  def testIsSlowerThan(): Unit = {
    val b = new RandomSleepBenchmark()
    val effectSizes = Seq(0.5, 0.8) // test medium and large effect sizes
    val minimumTimeDifference = 0.2 * b.std / 1000 // must measure a small effect before rejecting null hypothesis
    effectSizes.foreach { effectSize =>
      val delta = b.std * effectSize
      val fasterTime = (b.mean - delta) / 1000
      val slowerTime = (b.mean + delta) / 1000
      assert(
        Stopwatch.isSlowerThan(b.kernel(), fasterTime, minimumTimeDifference = minimumTimeDifference), s"Unable to determine a benchmark is slower than a known faster time with effect size $effectSize."
      )
      assert(
        !Stopwatch.isSlowerThan(b.kernel(), slowerTime, minimumTimeDifference = minimumTimeDifference), s"Unable to determine a benchmark is faster than a known slower time with effect size $effectSize."
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

  /**
    * A [rough] approximation of confidence intervals in the estimate of \lambda in a Poisson distribution
    *
    * If the number of trials is high, then the sum of the trials becomes normal
    *
    * @param nTrue the number of true instances
    * @param nTotal the total number of instances
    * @param interval confidence intervale: default 0.95 for 95%
    * @return the (lower interval, estimate, upper interval) of the frequency parameter in the Poisson distribution
    */
  private def getFrequencyRange(nTrue: Int, nTotal: Int, interval: Double = 0.95): (Double, Double, Double) = {
    val expected = nTrue.toDouble / nTotal
    val alpha = 1.0 - interval
    val dist = new NormalDistribution()
    val x = dist.inverseCumulativeProbability(1.0 - alpha / 2.0)
    val lower = expected - x * Math.sqrt(expected / nTotal)
    val upper = expected + x * Math.sqrt(expected / nTotal)
    (lower, expected, upper)
  }

  @Test
  def testIsProbablySlowerThan(): Unit = {
    def testRates(error: Double, numRun: Int, numTrial: Int): (Int, Int, Int) = {
      val benchmark = RandomSleepBenchmark(100, error)
      val res = Seq.fill(numTrial){
        Stopwatch.isProbablyFasterThan(benchmark.kernel(), 0.1, minRun = numRun, maxRun = numRun)
      }
      (res.count(_.contains(true)), res.count(_.contains(false)), res.count(_.isEmpty))
    }

    val nTrial = 20
    Seq(4, 8, 16, 32, 64).foreach{n =>
      val (fp, fn, unknown) = testRates(10, n, nTrial)
      println(s"numRun = ${n}: fp = ${getFrequencyRange(fp, nTrial)}")
      println(s"numRun = ${n}: fn = ${getFrequencyRange(fn, nTrial)}")
      println(s"numRun = ${n}: uk = ${getFrequencyRange(unknown, nTrial)}")
      println("========")
    }
  }

  /**
    * For a test that is tight with respect to the natural variance (expected difference is smaller than standard
    * error), make sure that significance is easily reached and that the false negative rate is within expected bounds
    */
  @Test
  def testIsDefinitelyFasterThan(): Unit = {
    val fn = 0.2
    val nTrials = 16
    val benchmark = RandomSleepBenchmark(100, 20)
    val results = (0 until nTrials).map{_ =>
      Stopwatch.isProbablyFasterThan(benchmark.kernel(), 0.105, minRun = 2, falseNegative = fn, maxRun = 64)
    }
    assert(
      results.forall(_.isDefined),
      s"Was unable to reach significance in ${results.count(_.isEmpty)} of $nTrials trials"
    )
    val (lower, _, _) = getFrequencyRange(results.count(_.contains(false)), nTrials)
    assert(lower < fn / 1.0, s"False negative rate of $lower was probably higher than target $fn")
  }

    /**
    * For a test that is tight with respect to the natural variance (expected difference is smaller than standard
    * error), make sure that significance is easily reached and that the false negative rate is within expected bounds
    */
  @Test
  def testIsDefinitelyFasterThen(): Unit = {
    val fn = 0.2
    val nTrials = 16
    val benchmark = RandomSleepBenchmark(100, 20)
    val results = (0 until nTrials).map{_ =>
      Stopwatch.isSlowerThan(benchmark.kernel(), 0.105, minRun = 2, maxRun = 64, falseNegative = fn)
    }
    val (lower, _, _) = getFrequencyRange(results.count(_ == true), nTrials)
    assert(lower < fn / 1.0, s"False negative rate of $lower was probably higher than target $fn")
  }
}

/**
  * Test benchmark that sleeps for a random amount of time sampled from a normal distribution.
  *
  * @param mean Average sleep time (in ms)
  * @param std Standard deviation about the mean sleep time (in ms)
  */
case class RandomSleepBenchmark(mean: Double = 100.0, std: Double = 10.0) extends Benchmark {
  /** Kernel that sleep for a random amount of time */
  override def kernel(): Unit = {
    val r = Math.max(mean + Random.nextGaussian() * std, 0.0)
    Thread.sleep(r.toLong)
  }
}

object StopwatchTest {
  def main(argv: Array[String]): Unit = {
    val begin = System.currentTimeMillis()
    (0 until 10).foreach{idx =>
      new StopwatchTest().testIsDefinitelyFasterThen()
    }
    println(s"Took ${System.currentTimeMillis() - begin} to do ryan's approach")
    val start = System.currentTimeMillis()
    (0 until 10).foreach{idx =>
      new StopwatchTest().testIsDefinitelyFasterThan()
    }
    println(s"Took ${System.currentTimeMillis() - start} to do hutch's approach")

  }
}
