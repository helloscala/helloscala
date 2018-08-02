import sbt.Keys._
import sbt._

object Version {

  import Environment._

  private def _version: String = sys.props.get("build.version")
    .orElse(sys.env.get("BUILD_VERSION"))
    .getOrElse("1.1.7")

  lazy val versionning = Seq(
    version := (if (buildEnv.value == BuildEnv.Developement && !_version.endsWith("-SNAPSHOT")) s"${_version}-SNAPSHOT" else
      _version)
  )
}
