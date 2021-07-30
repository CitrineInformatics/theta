import sbt._

object Dependencies {
  lazy val junitVersion = "4.13.1"
  lazy val breezeVersion = "1.2"

  val thetaDeps = Seq(
    "org.scalanlp" %% "breeze" % breezeVersion,
    "org.scalanlp" %% "breeze-natives" % breezeVersion,
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  )
}
