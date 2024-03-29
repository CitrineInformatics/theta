import Dependencies._

name                   := "theta"
crossScalaVersions     := List("2.13.4", "2.12.4")
organization           := "io.citrine"
organizationName       := "Citrine Informatics"
homepage               := Some(url("https://github.com/CitrineInformatics/theta"))
developers             := List(Developer(id="maxhutch", name="Max Hutchinson", email="maxhutch@citrine.io", url=url("https://github.com/maxhutch")))
description            := "A portable timing library"
licenses               += "Apache-2.0" ->  url("http://www.apache.org/licenses/LICENSE-2.0.txt")
scmInfo                := Some(ScmInfo(url("https://github.com/CitrineInformatics/theta"), "scm:git@github.com:CitrineInformatics/theta.git"))
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"

pomIncludeRepository := { _ => false }
libraryDependencies ++= thetaDeps
javaOptions ++= sys.env.getOrElse("JAVA_OPTS", "").split(" ").toSeq
Test / parallelExecution := false
