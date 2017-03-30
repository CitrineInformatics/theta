package io.citrine.theta

import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by maxhutch on 3/29/17.
  */
@Test
class BenchmarkRegistryTest {

  @Test
  @Category(Array(classOf[SlowTest]))
  def testRegisteredBenchmarks(): Unit = {
    BenchmarkRegistry.benchmarks.keySet.foreach{name =>
      Stopwatch.time(BenchmarkRegistry.benchmarks(name).kernel())
    }

  }

}
