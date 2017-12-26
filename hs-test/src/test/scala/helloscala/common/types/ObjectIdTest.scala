package helloscala.common.types

import helloscala.test.HelloscalaSpec

class ObjectIdTest extends HelloscalaSpec {

  "ObjectIdTest" should {

    "isValid" in {
      val id = "59f198e44bb0434ccf5108e2"
      ObjectId.isValid(id) mustBe true

      ObjectId.parse(id).isSuccess mustBe true

      ObjectId.parse(id).get.toString mustBe id
    }

  }

}
