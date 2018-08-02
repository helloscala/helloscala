package com.github.swagger.akka.model

case class Contact(name: String, url: String, email: String)

case class License(name: String, url: String)

case class Info(
    description: String = "",
    version: String = "",
    title: String = "",
    termsOfService: String = "",
    contact: Option[Contact] = None,
    license: Option[License] = None,
    vendorExtensions: Map[String, Object] = Map.empty)
