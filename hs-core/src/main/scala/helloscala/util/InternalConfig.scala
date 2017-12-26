package helloscala.util

import com.typesafe.config.{ Config, ConfigFactory }

object InternalConfig {
  var config: Config = ConfigFactory.load()
}
