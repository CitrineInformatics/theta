package io.citrine.theta

import io.citrine.theta.benchmarks.{RandomGenerationBenchmark, StreamBenchmark}
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.util.Random

/**
  * Created by maxhutch on 3/29/17.
  */
@Test
class BenchmarkRegistryTest {

  /**
    * Make sure using the default doesn't throw an error
    */
  @Test
  def testDefault(): Unit = {
    BenchmarkRegistry.getTime("Default")
  }


  @Test
  @Category(Array(classOf[SlowTest]))
  def testConsistencyRandomGeneration(): Unit = {
    (0 until 32).foreach{ i =>
      val theta: Double = Stopwatch.time(RandomGenerationBenchmark.kernel(), benchmark = "RandomGeneration")
      assert(theta < 1.1, s"RandomGeneration benchmark inconsistent (too slow ${theta})")
      assert(theta > 0.9, s"RandomGeneration benchmark inconsistent (too fast ${theta})")
    }
  }

  @Test
  @Category(Array(classOf[SlowTest]))
  def testConsistencyStream(): Unit = {
    val N = StreamBenchmark.N
    val a = new Array[Double](N)
    val b = new Array[Double](N)
    val c = new Array[Double](N)
    (0 until N).foreach{i =>
      a(i) = 2.0
      b(i) = 0.5
      c(i) = 0.0
    }
    (0 until 8).foreach{ i =>
      val theta: Double = Stopwatch.time(StreamBenchmark.customKernel(a, b, c, Random.nextDouble()), benchmark = "STREAM")
      assert(theta < 1.1, s"RandomGeneration benchmark inconsistent (too slow ${theta})")
      assert(theta > 0.9, s"RandomGeneration benchmark inconsistent (too fast ${theta})")
    }
  }
}

object BenchmarkRegistryTest {
  def main(argv: Array[String]): Unit = {
    new BenchmarkRegistryTest().testConsistencyStream()
  }
}
