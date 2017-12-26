object Version {
  private def _version: String = sys.props.get("build.version")
    .orElse(sys.env.get("BUILD_VERSION"))
    .getOrElse("0.2-SNAPSHOT")

  val version = _version
}
