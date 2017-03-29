package io.citrine.theta.benchmarks

/**
  * Created by maxhutch on 3/29/17.
  */
trait Benchmark {

  /**
    * Run the benchmark
    * @return the benchmark time (in seconds)
    */
  def run(): Double
}
