name := """remove-duplicates"""

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "com.jsuereth" %% "scala-arm" % "1.3")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
