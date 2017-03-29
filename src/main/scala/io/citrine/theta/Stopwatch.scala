package io.citrine.theta

object Stopwatch {
  /**
    * Quick timer utility.
    *
    * @param cmd command to run
    * @tparam R the return type of the command
    * @return the timed command's return value
    */
  def time[R](cmd: => R, benchmark: String = "Default", nWarm: Int = 2, nTrial: Int = 1): Double = {
    (0 until nWarm).foreach(i => cmd)
    val start = System.nanoTime()
    (0 until nTrial).foreach(i => cmd)
    val end = System.nanoTime()
    (end - start) * 1.0e-9 / (nTrial * BenchmarkRegistry.getTime(benchmark))
  }
}