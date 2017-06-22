package io.citrine.theta

import scala.collection.mutable

object Stopwatch {
  /**
    * Time a block and normalize the result by the time of a benchmark
    *
    * @param block block to run
    * @return the non-dimensional time of the block
    */
  def time[R](block: => R, benchmark: String = "Default",
              minRun: Int = 4, maxRun: Int = 128, targetError: Double = 0.05): Double = {
    wallclock(block, minRun, maxRun, targetError) / BenchmarkRegistry.getTime(benchmark)
  }

  private[theta] def wallclock[R](block: => R, minRun: Int = 4, maxRun: Int = 128, targetError: Double = 0.05): Double = {
    val minRunActual = Math.max(minRun, 4)
    val times = mutable.ListBuffer.empty[Double]
    var iteration = 0
    var errorEstimate: Double = Double.MaxValue
    var mean: Double = 0.0
    while ((errorEstimate > targetError || iteration < minRunActual) && iteration < maxRun) {
      val start = System.nanoTime()
      block
      val thisTime: Double = System.nanoTime() - start

      times.append(thisTime) // add a new time
      if (iteration % 4 == 3) times.remove(times.indexOf(times.max)) // wipe out an slow time every 4 iterations
      iteration = iteration + 1

      /* Compute mean and variance */
      val sumTime = times.sum
      val sumSq = times.map(x => x * x).sum
      mean = sumTime / times.size
      val variance = sumSq / times.size - mean * mean

      /* Estimate the uncertainty in the mean */
      errorEstimate = Math.sqrt(variance / times.size) / mean
    }
    mean * 1.0e-9 // convert to seconds
  }
}