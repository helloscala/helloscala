object Version {
  private def _version: String = sys.props.get("build.version")
    .orElse(sys.env.get("BUILD_VERSION"))
    .getOrElse("1.0.0")

  val version = _version
}
