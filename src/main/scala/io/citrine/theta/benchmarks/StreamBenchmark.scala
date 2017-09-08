package io.citrine.theta.benchmarks

import scala.util.Random

/**
  * This runs the STREAM benchmark (https://www.cs.virginia.edu/stream/)
  * Created by maxhutch on 3/29/17.
  */
class StreamBenchmark() extends Benchmark {
  val N: Int = 33554432

  var a: Array[Double] = Array()
  var b: Array[Double] = Array()
  var c: Array[Double] = Array()

  override def setup(): Unit = {
    a = new Array[Double](N)
    b = new Array[Double](N)
    c = new Array[Double](N)
    (0 until N).foreach{i =>
      a(i) = 2.0
      b(i) = 0.5
      c(i) = 0.0
    }
  }

  override def teardown(): Unit = {
    a = Array()
    b = Array()
    c = Array()
  }


  /**
    * Unused
    */
  override def kernel(): Unit = {
    val scalar = Random.nextDouble()
    a.indices.foreach(i => c(i) = a(i) )
    a.indices.foreach(i => b(i) = scalar * c(i) )
    a.indices.foreach(i => c(i) = a(i) + c(i) )
    a.indices.foreach(i => a(i) = b(i) + scalar * c(i) )
  }

  override def getCount(): Long = 10L * N
}

object StreamBenchmark extends BenchmarkBuilder {
  override def build(): Benchmark = new StreamBenchmark()
}
