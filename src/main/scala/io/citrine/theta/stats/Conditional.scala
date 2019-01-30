package io.citrine.theta.stats


trait Conditional {
  def isSatisfied(maxSamples: Int, minSamples: Int = 4, probability: Double = 0.95): Boolean
}