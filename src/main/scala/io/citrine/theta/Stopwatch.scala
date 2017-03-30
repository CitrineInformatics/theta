package io.citrine.theta

object Stopwatch {
  /**
    * Time a block and normalize the result by the time of a benchmark
    *
    * @param block block to run
    * @return the non-dimensional time of the block
    */
  def time[R](block: => R, benchmark: String = "Default", nWarm: Int = 4, nTrial: Int = 8): Double = {
    (0 until nWarm).foreach(i => block)
    val start = System.nanoTime()
    (0 until nTrial).foreach(i => block)
    val end = System.nanoTime()
    (end - start) * 1.0e-9 / (nTrial * BenchmarkRegistry.getTime(benchmark))
  }
}