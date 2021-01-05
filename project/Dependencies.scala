import sbt._

object Dependencies {
  lazy val netlibVersion = "1.1.2"
  lazy val junitVersion = "4.13.1"
  lazy val breezeVersion = "1.1"

  val thetaDeps = Seq(
    "com.github.fommil.netlib" % "all" % netlibVersion,
    "org.scalanlp" %% "breeze" % breezeVersion,
    "junit" % "junit" % junitVersion % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  )
}