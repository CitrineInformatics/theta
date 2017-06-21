package io.citrine.theta

/**
  * Created by maxhutch on 4/21/17.
  */
class ProfilerReport(time: Double, theta: Double, efficiency: Double) {

  def getTime(): Double = {
    time
  }

  def getTheta(): Double = {
    theta
  }

  def getEfficiency(): Double = {
    efficiency
  }
}
