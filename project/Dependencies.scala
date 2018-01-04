import sbt._
import sbt.Keys._

object Dependencies {
  val versionScala211 = "2.11.12"
  val versionScala212 = "2.12.4"

  val _scalatest = "org.scalatest" %% "scalatest" % "3.0.4"

  private val versionAkka = "2.5.8"
  lazy val _akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % versionAkka,
    "com.typesafe.akka" %% "akka-slf4j" % versionAkka,
    "com.typesafe.akka" %% "akka-stream" % versionAkka,
    "com.typesafe.akka" %% "akka-stream-testkit" % versionAkka % Test
  ).map(_.exclude("org.scala-lang.modules", s"scala-java8-compat").cross(CrossVersion.binary)) :+
    ("org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0").exclude("org.scala-lang", "scala-library")
  val _akkaTest = Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % versionAkka
  )

  val _akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "0.17"

  private val versionAlpakka = "0.15"
  val _alpakkaCassandra = ("com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % versionAlpakka)
    .excludeAll(ExclusionRule("com.typesafe.akka"), ExclusionRule("com.datastax.cassandra"), ExclusionRule("io.netty"), ExclusionRule("com.google.guava"))

  val _alpakkaFile = ("com.lightbend.akka" %% "akka-stream-alpakka-file" % versionAlpakka)
    .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaFtp = ("com.lightbend.akka" %% "akka-stream-alpakka-ftp" % versionAlpakka)
    .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaMongodb = ("com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % versionAlpakka)
    .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaElasticsearch = ("com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % versionAlpakka)
    .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaSlick = ("com.lightbend.akka" %% "akka-stream-alpakka-slick" % versionAlpakka)
    .excludeAll(ExclusionRule("com.typesafe.akka"))

  private val versionAkkaHttp = "10.0.11"
  lazy val _akkaHttpCore = ("com.typesafe.akka" %% "akka-http-core" % versionAkkaHttp)
    .exclude("com.typesafe.akka", s"akka-actor").cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", s"akka-stream").cross(CrossVersion.binary)
  lazy val _akkaHttpTest = ("com.typesafe.akka" %% "akka-http-testkit" % versionAkkaHttp)
    .exclude("com.typesafe.akka", s"akka-stream").cross(CrossVersion.binary)
  lazy val _akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-http" % versionAkkaHttp,
    _akkaHttpTest % Test,
    //    "com.typesafe" %% "ssl-config-akka" % "0.2.2",
    "ch.megard" %% "akka-http-cors" % "0.2.2"
  ).map(_
    .exclude("com.typesafe.akka", "akka-actor").cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-stream").cross(CrossVersion.binary))

  val swaggerVersion = "1.5.17"
  val _swagger = Seq(
    "io.swagger" % "swagger-core" % swaggerVersion,
    //    "io.swagger" % "swagger-annotations" % swaggerVersion,
    //    "io.swagger" % "swagger-models" % swaggerVersion,
    ("io.swagger" % "swagger-jaxrs" % swaggerVersion)
      .exclude("org.reflections", "reflections"),
    ("io.swagger" %% "swagger-scala-module" % "1.0.4")
      .excludeAll(ExclusionRule("com.fasterxml.jackson.module"), ExclusionRule("com.fasterxml.jackson")),
    "org.reflections" % "reflections" % "0.9.10"
  ).map(_.exclude("com.google.guava", "guava"))

  val _swaggerAnnotation = "io.swagger" % "swagger-annotations" % swaggerVersion

  val _typesafeConfig = "com.typesafe" % "config" % "1.3.2"

  val _scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  private val versionJackson = "2.9.2"
  val _jackson = Seq(
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % versionJackson,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % versionJackson,
    "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % versionJackson,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % versionJackson,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % versionJackson)

  lazy val _redis = ("net.debasishg" %% "redisclient" % "3.4")
    .exclude("com.typesafe.akka", "akka-actor").cross(CrossVersion.binary)

  private val versionSlick = "3.2.1"
  val _slick = Seq(
    "com.typesafe.slick" %% "slick" % versionSlick,
    "com.typesafe.slick" %% "slick-hikaricp" % versionSlick
  )

  private val versionSlickPg = "0.15.4"
  val _slickPg = Seq(
    "com.github.tminglei" %% "slick-pg" % versionSlickPg
  )

  private val versionElastic4s = "5.4.13"
  val _elastic4s = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-tcp" % versionElastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-xpack-security" % versionElastic4s
  ).map(_.exclude("log4j", "log4j")
    .excludeAll(ExclusionRule("io.netty"), ExclusionRule("org.apache.lucene"), ExclusionRule("org.elasticsearch"), ExclusionRule("org.elasticsearch.client")))

  private val versionElasticsearch = "5.4.3"
  val _elasticsearch = Seq(
    ("org.elasticsearch" % "elasticsearch" % versionElasticsearch)
      .excludeAll(
        ExclusionRule("com.fasterxml.jackson.datatype"),
        ExclusionRule("com.fasterxml.jackson.module"),
        ExclusionRule("com.fasterxml.jackson.core"),
        ExclusionRule("com.fasterxml.jackson.dataformat")
      ),
    "org.elasticsearch.client" % "transport" % versionElasticsearch,
    "org.elasticsearch.client" % "x-pack-transport" % versionElasticsearch
  ).map(_
    .excludeAll(ExclusionRule("com.google.guava"))
    .exclude("io.netty", "netty-buffer")
    .exclude("io.netty", "netty-codec")
    .exclude("io.netty", "netty-codec-http")
    .exclude("io.netty", "netty-common")
    .exclude("io.netty", "netty-handler")
    .exclude("io.netty", "netty-resolver")
    .exclude("io.netty", "netty-transport")
    .exclude("io.netty", "netty-all"))

  private val versionGuice = "4.1.0"
  val _guice = ("com.google.inject" % "guice" % versionGuice).exclude("com.google.guava", "guava")
  val _guiceAssistedinject = "com.google.inject.extensions" % "guice-assistedinject" % versionGuice

  private val versionNetty = "4.1.19.Final"
  val _nettyNativeEpoll = "io.netty" % "netty-transport-native-epoll" % versionNetty classifier "linux-x86_64"
  //  val _nettyAll = "io.netty" % "netty-all" % versionNetty
  val _nettyHandler = "io.netty" % "netty-handler" % versionNetty
  val _nettyCodecHttp = "io.netty" % "netty-codec-http" % versionNetty

  private val versionCassandra = "3.3.2"
  val _cassandraDriver = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % versionCassandra,
    "com.datastax.cassandra" % "cassandra-driver-extras" % "3.3.1"
  ).map(_.excludeAll(ExclusionRule("io.netty"), ExclusionRule("com.google.guava")))

  val _logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val _curator = ("org.apache.curator" % "curator-framework" % "2.12.0").exclude("log4j", "log4j")

  val _jsoup = "org.jsoup" % "jsoup" % "1.11.2"

  val _commonsCodec = "commons-codec" % "commons-codec" % "1.11"

  val _commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.7"

  val _bouncycastleProvider = "org.bouncycastle" % "bcprov-jdk15on" % "1.58"

  private val versionPoi = "3.17"
  val _poi = Seq(
    "org.apache.poi" % "poi" % versionPoi,
    "org.apache.poi" % "poi-scratchpad" % versionPoi,
    "org.apache.poi" % "poi-ooxml" % versionPoi
  )

  val _hikariCP = "com.zaxxer" % "HikariCP" % "2.7.4"

  val _postgresql = "org.postgresql" % "postgresql" % "42.1.4"
  val _mysql = "mysql" % "mysql-connector-java" % "6.0.6"

  val _jama = "gov.nist.math" % "jama" % "1.0.3"

  private val versionTika = "1.17"
  val _tika = Seq(
    ("org.apache.tika" % "tika-parsers" % versionTika)
      .excludeAll(ExclusionRule("org.apache.poi")),
    "org.apache.tika" % "tika-langdetect" % versionTika
  )

  val _seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % "3.8.1"

  val _okhttp = "com.squareup.okhttp3" % "okhttp" % "3.9.1"

  private val versionQuartz = "2.2.3"
  val _quartz = "org.quartz-scheduler" % "quartz" % versionQuartz

  val _guava = "com.google.guava" % "guava" % "19.0"

  val _junit4 = "junit" % "junit" % "4.12"

  val _log4jToSlf4j = "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.10.0"
}

