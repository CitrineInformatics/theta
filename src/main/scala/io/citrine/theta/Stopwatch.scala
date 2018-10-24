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
      val variance = times.map(t => t - mean).map(t => t * t).sum / times.size

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
}