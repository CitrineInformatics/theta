package io.citrine.theta.benchmarks

import io.citrine.theta.Stopwatch

import scala.util.Random

/**
  * This runs the STREAM benchmark (https://www.cs.virginia.edu/stream/)
  * Created by maxhutch on 3/29/17.
  */
object StreamBenchmark extends Benchmark {
  val N: Int = 33554432

  override def run(): Double = {
    val a = new Array[Double](N)
    val b = new Array[Double](N)
    val c = new Array[Double](N)
    (0 until N).foreach{i =>
      a(i) = 2.0
      b(i) = 0.5
      c(i) = 0.0
    }
    Stopwatch.time({customKernel(a, b, c, Random.nextDouble())}, benchmark = "None")
  }

  /**
    * Long vector manipulations that are memory bandwidth bound
    */
  def customKernel(a: Array[Double], b: Array[Double], c: Array[Double], scalar: Double): Unit = {
    a.indices.foreach(i => c(i) = a(i) )
    a.indices.foreach(i => b(i) = scalar * c(i) )
    a.indices.foreach(i => c(i) = a(i) + c(i) )
    a.indices.foreach(i => a(i) = b(i) + scalar * c(i) )
  }

  /**
    * Unused
    */
  override def kernel(): Unit = {}

  override def getCount(): Long = 10L * N
}
