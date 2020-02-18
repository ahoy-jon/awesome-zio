val ZioVersion    = "1.0.0-RC17+406-13ace38d-SNAPSHOT"
val Specs2Version = "4.7.0"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

organization := "io.univalence"
name := "zio-awesome-project"
version := "0.0.1"
scalaVersion := "2.11.12"
maxErrors := 3
libraryDependencies ++= Seq(
  "dev.zio"    %% "zio"             % ZioVersion,
  "dev.zio"    %% "zio-test"        % ZioVersion % Test,
  "dev.zio"    %% "zio-test-sbt"    % ZioVersion % Test,
  "dev.zio"    %% "zio-macros-core" % "0.6.2",
  "org.specs2" %% "specs2-core"     % Specs2Version % "test"
)

// Refine scalac params from tpolecat
scalacOptions --= Seq(
  "-Xfatal-warnings"
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
