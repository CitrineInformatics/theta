package io.citrine.theta

import org.apache.commons.math3.distribution.TDistribution

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Stopwatch {
  /**
    * Time a block and normalize the result by the time of a benchmark
    *
    * @param block block to run
    * @return the non-dimensional time of the block
    */
  def time[R](block: => R, benchmark: String = "Default",
              minRun: Int = 4, maxRun: Int = 64,
              targetError: Double = 0.05, confidence: Double = 0.95): Double = {
    wallclock(block, minRun, maxRun, targetError, confidence) / BenchmarkRegistry.getTime(benchmark)
  }

  private[theta] def wallclock[R](
                                   block: => R,
                                   minRun: Int = 4,
                                   maxRun: Int = 64,
                                   targetError: Double = 0.05,
                                   confidence: Double = 0.95
                                 ): Double = {
    if (minRun < 4) {
      throw new IllegalArgumentException(s"A minimum of 4 runs are required to establish bounds with meaningful confidence. Only $minRun were requested.")
    }

    val times = mutable.ListBuffer.empty[Double]
    var iteration: Int = 0
    var errorEstimate: Double = Double.MaxValue
    var mean: Double = 0.0
    while ((errorEstimate > targetError || iteration < minRun) && iteration < maxRun) {
      val start = System.nanoTime()
      block
      val thisTime: Double = System.nanoTime() - start

      times.append(thisTime) // add a new time
      if (iteration % 4 == 3) times.remove(times.indexOf(times.max)) // wipe out an slow time every 4 iterations
      iteration += 1

      /* Compute mean and variance */
      val sumTime = times.sum
      mean = sumTime / times.size
      val variance: Double = calcVariance(times, Some(mean))

      /* Estimate the uncertainty in the mean */
      errorEstimate = if (times.size > 1) {
        // We have a small number of samples, so the distribution will be a student's T distribution
        val dist = new TDistribution(times.size - 1)
        // Where on the t-distribution do we achieve the desired level of confidence?
        val x = dist.inverseCumulativeProbability(1.0 - (1.0 - confidence) / 2.0)
        // Convert from the position in the t-distribution to the uncertainty in the mean,
        // relative to the estimate of the mean
        // Note that we're dividing the uncertainty in the mean by the estimated, not true, mean
        // That adds a second order correction that we're ignoring here
        x * Math.sqrt(variance / times.size) / mean
      } else {
        Double.MaxValue
      }
    }
    mean * 1.0e-9 // convert to seconds
  }

  /**
    * Test whether a function is slower than the specified time.
    *
    * @param block                 to time
    * @param time                  Critical time (in seconds) to test whether the block is slower than
    * @param minRun                Minimum number of runs to perform testing the runtime
    * @param maxRun                Maximum number of runs to perform. If this number is reached, a runtime exception is
    * @param falsePositive         False positive rate. (Also known as the probability of observing a Type I error.)
    * @param falseNegative         False negative rate. (Also known as the probability of observing a Type II error.)
    *                              thrown because the null hypothesis could not be tested at the specified
    *                              false positive and false negative rates. In which case, either the maximum number
    *                              of runs should be increased or the FP/FN rates should be relaxed.
    * @param minimumTimeDifference Minimum time difference (in seconds) required to conclude the runtime is slower
    *                              than the specified time.
    * @param acceptableEffectSize  If defined, the test will exit if the effect size exceeds this value.
    *                              Large, medium and small effects are given by 0.8, 0.5 and 0.2, respectively.
    *                              Units are defined in terms of standard deviations of the mean. The effect size
    *                              is directional. If positive, the test will exit early if the mean execution time
    *                              is clearly slower than the given `time`. If negative, the test will exit early if
    *                              the execution time is clearly faster than the specified time. Note, however,
    *                              that ''clearly'' in this context is defined by the effect size you specify.
    * @tparam R Return type of the execution block
    * @return Whether the function block is faster than the specified time.
    */
  def isSlowerThan[R](
                       block: => R, time: Double,
                       minRun: Int = 4, maxRun: Int = 64,
                       falsePositive: Double = 0.05, falseNegative: Double = 0.20,
                       minimumTimeDifference: Double = 0.0,
                       acceptableEffectSize: Option[Double] = None
                     ): Boolean = {
    validateInputs(minRun, maxRun, falsePositive, falseNegative)

    val times = mutable.ListBuffer.empty[Double]
    var iteration: Int = 0
    while (iteration < minRun || iteration < maxRun) {
      val start = System.nanoTime()
      block
      val thisTime: Double = (System.nanoTime() - start) * 1.0e-9 // convert to seconds
      times.append(thisTime)
      iteration += 1
      val sampleSize = times.size
      if (sampleSize > 1 && iteration >= minRun) {
        val sampleMean = calcHodgesLehmannMedian(times)
        val sampleMAD = calcMAD(times)
        val sampleStd = convertMADtoStd(sampleMAD)

        val tDist = new TDistribution(sampleSize - 1)
        val tAlpha = tDist.inverseCumulativeProbability(falsePositive)
        val tBeta = tDist.inverseCumulativeProbability(falseNegative)
        val delta = sampleMean - time - minimumTimeDifference
        val tRequiredSampleSize = Math.pow(sampleStd * (tAlpha + tBeta) / delta, 2)

        // Do we have enough samples to test the hypothesis at the requested FP/FN rates?
        if (sampleSize >= tRequiredSampleSize) {
          val t = delta / (sampleStd / Math.sqrt(sampleSize))
          return t > -tAlpha
        }

        if (acceptableEffectSize.isDefined) {
          val effectSize = calcEffectSize(sampleMean, sampleStd, time)
          if (measuredAcceptableEffectSize(effectSize, acceptableEffectSize.get)) {
            // At this point the result is not statistically significant (at least not at the FP/FN rates specified).
            // However, we've measured a large enough effect to report the findings via direct comparison.
            return sampleMean > time
          }
        }
      }
    }
    false // Results are not statistically significant. Cannot reject null hypothesis that execution time == `time`.
  }

  /** Checks that the user input sensible parameter values */
  private def validateInputs(minRun: Int, maxRun: Int, falsePositive: Double, falseNegative: Double): Unit = {
    if (minRun < 2) {
      throw new IllegalArgumentException(s"Minimum number of runs = $minRun was requested. At least 2 runs are required.")
    }
    if (maxRun < 2) {
      throw new IllegalArgumentException(s"Maximum number of runs = $maxRun was requested. At least 2 runs are required.")
    }
    if (maxRun < minRun) {
      throw new IllegalArgumentException(s"Maximum number of runs must be greater than or equal to the minimum number of runs. Maximum requested = $maxRun. Minimum requested = $minRun.")
    }
    if (falsePositive < 0 || falsePositive > 0.5) {
      throw new IllegalArgumentException(s"False positive probability (= $falsePositive) must be between 0 and 0.5.")
    }
    if (falseNegative < 0 || falseNegative > 0.5) {
      throw new IllegalArgumentException(s"False negative probability (= $falseNegative) must be between 0 and 0.5.")
    }
  }

  /**
    * Checks wither the sign of the effect size matches the acceptable effect size and whether the magnitude of the
    * effect is greater than the acceptable limit.
    *
    * @param effectSize           Measure effect size
    * @param acceptableEffectSize Minimum acceptable effect size for the test to terminate
    * @return Whether an acceptable effect size has been measured.
    */
  private def measuredAcceptableEffectSize(effectSize: Double, acceptableEffectSize: Double): Boolean = {
    Math.signum(effectSize) == Math.signum(acceptableEffectSize) && Math.abs(effectSize) > Math.abs(acceptableEffectSize)
  }

  /**
    * Calculates the variance of the list, given its mean.
    *
    * @param list of samples
    * @param mean of the list
    * @return sample variance
    */
  private def calcVariance(list: ListBuffer[Double], mean: Option[Double] = None): Double = {
    list.map(Math.pow(_, 2)).sum / list.size - Math.pow(mean.getOrElse(list.sum / list.size), 2)
  }

  /**
    * Calculates the size of the measured effect in units of standard deviations of the mean.
    *
    * @param sampleMean Mean of the samples taken from a normal distribution
    * @param sampleStd  Standard deviation of normally distributed samples
    * @param hypMean    Hypothesized mean
    * @return Effect size.
    */
  private def calcEffectSize(sampleMean: Double, sampleStd: Double, hypMean: Double): Double = {
    (sampleMean - hypMean) / sampleStd
  }

  /**
    * Returns the median of a list.
    *
    * @param list to obtain the median from
    * @return Median value
    */
  private def calcMedian(list: Seq[Double]): Double = {
    val size = list.size
    val sortedTimes = list.sorted
    val median = if (size % 2 == 0) {
      val rightIndex = size / 2
      val leftIndex = rightIndex - 1
      (sortedTimes(leftIndex) + sortedTimes(rightIndex)) / 2
    } else {
      val medianIndex = (size - 1) / 2
      sortedTimes(medianIndex)
    }
    median
  }

  /**
    * Calculates the median absolute deviation (MAD) of the list.
    *
    * @param list to evaluate
    * @return Median absolute deviation
    */
  private def calcMAD(list: Seq[Double]): Double = {
    val median = calcMedian(list)
    val medianDeviations = list.map(x => Math.abs(x - median))
    calcMedian(medianDeviations)
  }

  /**
    * Factor to convert median absolute deviation to standard deviation.
    * This value can be calculated from `1.0 / new NormalDistribution().inverseCumulativeProbability(0.75)`.
    */
  private val kMADtoStd: Double = 1.4826022185056023

  /**
    * Convert median absolute deviation (MAD) to standard deviation.
    *
    * @param mad Median absolution deviation
    * @return Standard deviation
    */
  private def convertMADtoStd(mad: Double): Double = {
    kMADtoStd * mad
  }

  /**
    * Calculates the Hodges-Lehmann estimate of the median. For symmetric distributions, this returns a consistent,
    * median-unbiased estimate. Commonly, is used as a method to estimate the mean that is robust to outliers.
    *
    * @param list used to approximate the median
    * @return Approximate median
    */
  private def calcHodgesLehmannMedian(list: Seq[Double]): Double = {
    val size = list.size
    val means = (0 until size).flatMap { i =>
      (i until size).map { j =>
        0.5 * (list(i) + list(j))
      }
    }
    calcMedian(means)
  }
}