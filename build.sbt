import Commons._
import Dependencies._
import Environment.{buildEnv, BuildEnv}
import sbt._

scalaVersion in ThisBuild := Dependencies.versionScala211

scalafmtOnCompile in ThisBuild := true

buildEnv in ThisBuild := {
  sys.props
    .get("build.env")
    .orElse(sys.props.get("env"))
    .orElse(sys.env.get("BUILD_ENV"))
    .flatMap {
      case "prod"  => Some(BuildEnv.Production)
      case "stage" => Some(BuildEnv.Stage)
      case "test"  => Some(BuildEnv.Test)
      case "dev"   => Some(BuildEnv.Developement)
      case _       => None
    }
    .getOrElse(BuildEnv.Developement)
}

version in ThisBuild := (if (buildEnv.value == BuildEnv.Developement && !_version.endsWith("-SNAPSHOT"))
                           s"${_version}-SNAPSHOT"
                         else _version)

lazy val root = Project(id = "helloscala-root", base = file("."))
  .aggregate(
    hsStarterNosql,
    hsInjectTest,
    hsInject,
    hsDoc,
    hsAkkaHttp,
    hsAkkaHttpCache,
    hsAkkaHttpSession,
    hsSwaggerAkkaHttp,
    hsHttpClient,
    hsAkkaHttpCore,
    hsNosql,
    hsSlick,
    hsJdbc,
    hsDiscovery,
    hsTest,
    hsCore
  )
  .settings(Publishing.noPublish: _*)

lazy val hsDoc = coreProject("hs-doc")
  .dependsOn(hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      ) ++ _poi ++ _tika
  )

lazy val hsStarterNosql = coreProject("hs-starter-nosql")
  .dependsOn(hsNosql, hsInject)

lazy val hsInjectTest = coreProject("hs-inject-test")
  .dependsOn(
    hsInject,
    hsTest % "compile->test;test->test",
    hsCore
  )
  .settings(
    libraryDependencies ++= Seq(
      _akkaHttpTest
    )
  )

lazy val hsInject = coreProject("hs-inject")
  .dependsOn(hsJdbc, hsDiscovery, hsAkkaHttp, hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      _hikariCP % Provided,
      _guice
    ) ++ _elastic4s.map(_ % Provided) ++ _elasticsearch.map(_ % Provided) ++ _cassandraDriver.map(_ % Provided)
  )

lazy val hsAkkaHttpCache = coreProject("hs-akka-http-cache")
  .dependsOn(hsAkkaHttp)
  .settings(
    libraryDependencies += _redis
  )

lazy val hsAkkaHttpSession = coreProject("hs-akka-http-session")
  .dependsOn(hsAkkaHttp, hsSwaggerAkkaHttp, hsTest % "compile->test;test->test", hsCore)

lazy val hsAkkaHttp = coreProject("hs-akka-http")
  .dependsOn(hsAkkaHttpCore, hsSwaggerAkkaHttp, hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      _guice % Provided
    )
  )

lazy val hsSwaggerAkkaHttp = coreProject("hs-swagger-akka-http")
  .dependsOn(hsAkkaHttpCore, hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= _swagger
  )

lazy val hsHttpClient = coreProject("hs-http-client")
  .dependsOn(hsAkkaHttpCore, hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      _okhttp
    )
  )

lazy val hsAkkaHttpCore = coreProject("hs-akka-http-core")
  .dependsOn(hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= _akkaHttp
  )

lazy val hsNosql = coreProject("hs-nosql")
  .dependsOn(hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      _alpakkaCassandra % Provided,
      _guice % Provided,
      _nettyNativeEpoll,
      _nettyHandler,
      _nettyCodecHttp
    ) ++ _elastic4s ++ _elasticsearch ++ _cassandraDriver
  )

lazy val hsSlick = coreProject("hs-slick")
  .dependsOn(hsJdbc, hsInject, hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      _postgresql
    ) ++ _slick ++ _slickPg
  )

lazy val hsJdbc = coreProject("hs-jdbc")
  .dependsOn(hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      _hikariCP,
      _postgresql % Provided,
      _mysql % Provided
    )
  )

lazy val hsDiscovery = coreProject("hs-discovery")
  .dependsOn(hsTest % "compile->test;test->test", hsCore)
  .settings(
    libraryDependencies ++= Seq(
      //      _curator,
      _guice % Provided
    )
  )

lazy val hsTest = coreProject("hs-test")
  .dependsOn(hsCore)
  .settings(
    libraryDependencies ++= Seq(_scalatest) ++ _akkaTest
  )

lazy val hsCore = coreProject("hs-core")
  .settings(
    libraryDependencies ++= Seq(
      _swaggerAnnotation % Provided,
      _hikariCP % Provided,
//      _commonsLang3,
      _bouncycastleProvider,
      _log4jToSlf4j,
      _scalaLogging,
      _scalatest % Test,
      _junit4 % Test,
      _guava,
      _logbackClassic,
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      _typesafeConfig
    ) ++ _akka ++ _jackson,
    PB.protocVersion := "-v361",
    PB.targets in Compile := Seq(
      scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
    )
  )

def coreProject(name: String) =
  Project(id = name, base = file(name))
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
    .settings(
      organization := "helloscala"
    )

def _version: String =
  sys.props
    .get("build.version")
    .orElse(sys.env.get("BUILD_VERSION"))
    .getOrElse("1.1.10")
