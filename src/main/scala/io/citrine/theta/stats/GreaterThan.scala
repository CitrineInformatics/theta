package io.citrine.theta.stats

import org.apache.commons.math3.random.EmpiricalDistribution

class GreaterThan(x: Double, func: () => Double) extends Conditional {
  override def isSatisfied(maxSamples: Int, minSamples: Int = 4, probability: Double = 0.95): Boolean = {
    var samples: Vector[Double] = Vector.fill(minSamples - 1)(func())
    (minSamples - 1 until maxSamples).foreach{i =>
      samples = samples :+ func()
      val dist = new EmpiricalDistribution()
      dist.load(samples.toArray)

      val probabilitySatisfied = 1 - dist.cumulativeProbability(x)
      if (probabilitySatisfied > probability) {
        return true
      } else if (probabilitySatisfied < (1-probability)) {
        false
      }
    }
    true

  }
}