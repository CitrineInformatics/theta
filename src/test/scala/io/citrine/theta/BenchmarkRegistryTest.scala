package io.citrine.theta

import io.citrine.theta.benchmarks.RandomGenerationBenchmark
import org.junit.Test
import org.junit.experimental.categories.Category

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
  def testConsistency(): Unit = {
    (0 until 32).foreach{ i =>
      val theta: Double = Stopwatch.time(RandomGenerationBenchmark.kernel(), benchmark = "RandomGeneration")
      assert(theta < 1.2, s"RandomGeneration benchmark inconsistent (too slow ${theta})")
      assert(theta > 0.8, s"RandomGeneration benchmark inconsistent (too fast ${theta})")
    }

  }



}
