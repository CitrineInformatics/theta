package io.citrine.theta

/**
  * Created by maxhutch on 4/21/17.
  */
class Profiler(name: String, benchmark: String = "Default", nWarm: Int = 2, nTrial: Int = 4) {

  def profile[R](block: Counter => R): ProfilerReport = {
    (0 until nWarm).foreach{i =>
      block(new Counter())
    }
    val start = System.nanoTime()
    var counter = new Counter()
    (0 until nTrial).foreach{i =>
      counter = new Counter()
      block(counter)
    }
    val end = System.nanoTime()

    val time = (end - start) * 1.0e-9 / nTrial
    val theta = time / BenchmarkRegistry.getTime(benchmark)
    val eff = (theta * BenchmarkRegistry.getCount(benchmark)) / counter.getCount()
    new ProfilerReport(time, theta, eff)
  }

}
