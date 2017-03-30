package io.citrine.theta

import org.junit.Test

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

}
