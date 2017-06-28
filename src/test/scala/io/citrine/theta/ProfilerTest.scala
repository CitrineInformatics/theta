package io.citrine.theta

import io.citrine.theta.benchmarks.{RandomGenerationBenchmark, StreamBenchmark}
import org.junit.Test

import scala.util.Random

/**
  * Created by maxhutch on 4/21/17.
  */
@Test
class ProfilerTest {

  /**
    * Test that the non-dimensional time of the kernel is 1
    */
  @Test
  def testTimeDefault(): Unit = {
    val report = new Profiler("test", benchmark = "RandomGeneration").profile{c: Counter =>
      RandomGenerationBenchmark.kernel()
      c.count(RandomGenerationBenchmark.getCount())
    }

    assert(report.getTheta() < 1.2, s"RandomGeneration benchmark inconsistent (too slow ${report.getTheta()})")
    assert(report.getTheta() > 0.8, s"RandomGeneration benchmark inconsistent (too fast ${report.getTheta()})")

    assert(report.getEfficiency() < 1.2, s"RandomGeneration benchmark inconsistent (too slow ${report.getEfficiency()})")
    assert(report.getEfficiency() > 0.8, s"RandomGeneration benchmark inconsistent (too fast ${report.getEfficiency()})")
  }

  /**
    * Test that the non-dimensional time of the STREAM benchmark is 1
    */
  @Test
  def testTimeStream(): Unit = {
    val benchmark = new StreamBenchmark()
    benchmark.setup()
    val report = new Profiler("test", benchmark = "STREAM").profile{counter: Counter =>
      benchmark.kernel()
      counter.count(benchmark.getCount())
    }
    benchmark.teardown()

    assert(report.getTheta() < 1.2, s"STREAM benchmark inconsistent (too slow ${report.getTheta()})")
    assert(report.getTheta() > 0.8, s"STREAM benchmark inconsistent (too fast ${report.getTheta()})")

    assert(report.getEfficiency() < 1.2, s"STREAM benchmark inconsistent (too slow ${report.getEfficiency()})")
    assert(report.getEfficiency() > 0.8, s"STREAM benchmark inconsistent (too fast ${report.getEfficiency()})")
  }

}
