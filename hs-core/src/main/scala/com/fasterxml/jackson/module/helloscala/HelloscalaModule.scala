/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fasterxml.jackson.module.helloscala

import akka.Done
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.Serializers
import helloscala.common.jackson.{AkkaDoneSerialize, ObjectIdDeserializer, ObjectIdSerializer}
import helloscala.common.types.ObjectId

class HelloscalaSerializers extends Serializers.Base {
  override def findSerializer(
      config: SerializationConfig,
      `type`: JavaType,
      beanDesc: BeanDescription): JsonSerializer[_] = {
    val rawClass = `type`.getRawClass
    if (classOf[ObjectId].isAssignableFrom(rawClass)) new ObjectIdSerializer
    else if (classOf[Done].isAssignableFrom(rawClass)) new AkkaDoneSerialize
    else super.findSerializer(config, `type`, beanDesc)
  }
}

class HelloscalaDeserializers extends Deserializers.Base {

  @throws[JsonMappingException]
  override def findBeanDeserializer(
      `type`: JavaType,
      config: DeserializationConfig,
      beanDesc: BeanDescription): JsonDeserializer[_] = {
    val rawClass = `type`.getRawClass
    if (classOf[ObjectId].isAssignableFrom(rawClass)) new ObjectIdDeserializer
    else super.findBeanDeserializer(`type`, config, beanDesc)
  }
}

class HelloscalaModule extends Module {
  override def getModuleName = "helloscala"

  override def version: Version = Version.unknownVersion

  override def setupModule(context: Module.SetupContext): Unit = {
    context.addSerializers(new HelloscalaSerializers)
    context.addDeserializers(new HelloscalaDeserializers)
  }
}
