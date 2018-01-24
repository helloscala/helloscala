package helloscala.http

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.{Contact, Info, License}
import helloscala.common.Configuration
import helloscala.common.util.ReflectUtils

/**
 * Swagger Route，配置使用Swagger自动生成文档。只包含swagger.json或swagger.yaml配置文件（/api-docs/swagger.json）
 * Swagger API访问地址：/api-docs/swagger-ui/index.html
 *
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-10.
 */
class SwaggerHttpDoc private (
    configuration: Configuration,
    _apiTypes: Traversable[Class[_]],
    basePath: String) extends SwaggerHttpService {

  require(configuration.underlying.hasPath(s"$basePath.scan-packages"), s"config配置路径：[$basePath.scan-packages] 不存在")

  override val apiDocsPath: String = configuration.get[Option[String]](s"$basePath.api-docs-path").getOrElse(super.apiDocsPath)

  // 指定Api类型，如：XXXRoutes
  override val apiClasses: Set[Class[_]] = {
    val cls = configuration.get[Seq[String]](s"$basePath.scan-packages")
      .flatMap(pkg => ReflectUtils.listClassNameFromPackage(pkg))
      .map(cn => Class.forName(cn))
      .toSet
    cls ++ _apiTypes
  }

  // Api服务访问地址（非Api文档访问地址）
  override val host: String =
    configuration.get[Option[String]](s"$basePath.host")
      .getOrElse("%s:%d".format(configuration.get[String]("server.host"), configuration.get[Int]("server.port")))

  // Api信息
  override val info: Info = {
    val contact =
      if (!configuration.has(s"$basePath.info.contact")) None
      else Some(Contact(
        configuration.getString(s"$basePath.info.contact.name"),
        configuration.getString(s"$basePath.info.contact.url"),
        configuration.getString(s"$basePath.info.contact.email")))
    val license =
      if (!configuration.has(s"$basePath.info.license")) None
      else Some(License(
        configuration.getString(s"$basePath.info.license.name"),
        configuration.getString(s"$basePath.info.license.url")))

    Info(
      configuration.get[Option[String]](s"$basePath.info.version").getOrElse(""),
      configuration.get[Option[String]](s"$basePath.info.description").getOrElse(""),
      configuration.get[Option[String]](s"$basePath.info.title").getOrElse(""),
      configuration.get[Option[String]](s"$basePath.info.termsOfService").getOrElse(""),
      contact,
      license)
  }

  val swaggerUiRoute: Route = {
    val s = scala.io.Source.fromInputStream(Thread.currentThread().getContextClassLoader.getResourceAsStream("hs-swagger-ui/index.html"))
    val indexHtml = s.getLines().mkString("\n").replace("/api-docs/swagger.json", s"/$apiDocsPath/swagger.json")

    try {
      pathPrefix(apiDocsPath) {
        path("swagger") {
          redirect(s"/api-docs/swagger-ui/index.html?url=/$apiDocsPath/swagger.json", StatusCodes.Found)
        } ~
          pathPrefix("swagger-ui") {
            path("index.html") {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, indexHtml))
            } ~
              getFromResourceDirectory("hs-swagger-ui") ~
              get {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, indexHtml))
              }
          }
      }
    } finally {
      if (s ne null) s.close()
    }
  }

}

object SwaggerHttpDoc {

  def apply(
      configuration: Configuration,
      apiTypes: Traversable[Class[_]] = Traversable(),
      basePath: String = "akka.http.swagger") =
    new SwaggerHttpDoc(configuration, apiTypes, basePath)

}
