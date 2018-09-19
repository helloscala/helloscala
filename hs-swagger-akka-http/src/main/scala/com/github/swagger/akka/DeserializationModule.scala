package com.github.swagger.akka

import java.io.IOException

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonNode}
import io.swagger.models._
import io.swagger.models.auth.SecuritySchemeDefinition
import io.swagger.models.parameters.Parameter
import io.swagger.models.properties.Property

class PathDeserializer extends JsonDeserializer[Path] {

  @throws[IOException]
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): Path = {
    val node: JsonNode = jp.getCodec.readTree(jp)
    node.get("$ref") match {
      case null => Json.mapper.convertValue(node, classOf[RefPath])
      case _    => Json.pathMapper.convertValue(node, classOf[Path])
    }
  }
}

class ResponseDeserializer extends JsonDeserializer[Response] {

  @throws[IOException]
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): Response = {
    val node: JsonNode = jp.getCodec.readTree(jp)
    node.get("$ref") match {
      case null => Json.mapper.convertValue(node, classOf[RefResponse])
      case _    => Json.responseMapper.convertValue(node, classOf[Response])
    }
  }
}

class DeserializationModule(val includePathDeserializer: Boolean, val includeResponseDeserializer: Boolean)
    extends SimpleModule {

  if (includePathDeserializer)
    this.addDeserializer(classOf[Path], new PathDeserializer)
  if (includeResponseDeserializer)
    this.addDeserializer(classOf[Response], new ResponseDeserializer)
  this.addDeserializer(classOf[Property], new PropertyDeserializer)
  this.addDeserializer(classOf[Model], new ModelDeserializer)
  this.addDeserializer(classOf[Parameter], new ParameterDeserializer)
  this.addDeserializer(classOf[SecuritySchemeDefinition], new SecurityDefinitionDeserializer)

  def this() {
    this(true, true)
  }

}
