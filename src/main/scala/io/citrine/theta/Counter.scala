package io.citrine.theta

/**
  * Created by maxhutch on 4/21/17.
  */
class Counter {
  var count: Long = 0

  def count(n: Int): Unit = {
    count += n
  }

  def count(n: Long): Unit = {
    count += n
  }

  def getCount(): Long = {
    count
  }
}
