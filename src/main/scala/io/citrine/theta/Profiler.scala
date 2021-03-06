package io.citrine.theta

/**
  * Created by maxhutch on 4/21/17.
  */
class Profiler(name: String, benchmark: String = "Default",
               minRun: Int = 8, maxRun: Int = 64,
               targetError: Double = 0.05, confidence: Double = 0.95) {

  def profile[R](block: Counter => R): ProfilerReport = {
    val time = Stopwatch.wallclock({block(new Counter())}, minRun, maxRun, targetError, confidence)
    val counter = new Counter()
    block(counter)

    val theta = time / BenchmarkRegistry.getTime(benchmark)
    val eff = (theta * BenchmarkRegistry.getCount(benchmark)) / counter.getCount()
    new ProfilerReport(time, theta, eff)
  }

}
