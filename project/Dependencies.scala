import sbt._

object Dependencies {
  lazy val netlibVersion = "1.1.2"
  lazy val junitVersion = "4.13.1"
  lazy val breezeVersion = "2.0"

  val thetaDeps = Seq(
    "com.github.fommil.netlib" % "all" % netlibVersion pomOnly(),
    "org.scalanlp" %% "breeze" % breezeVersion,
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  )
}
