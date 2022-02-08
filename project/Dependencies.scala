import sbt._

object Dependencies {
  lazy val breezeVersion = "2.0"
  lazy val junitVersion = "4.13.1"

  val thetaDeps = Seq(
    "org.scalanlp" %% "breeze" % breezeVersion,
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  )
}
