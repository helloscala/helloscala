/**
 * DocumentUtilsTest
 *
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-12.
 */
package helloscala.edoc

import java.nio.file.{Files, Paths}

import helloscala.test.HelloscalaSpec

class DocumentUtilsTest extends HelloscalaSpec {

  "splitNameSuffix" in {
    DocumentUtils.splitNameSuffix("/home/hldev/Documents/重庆华龙网海数科技有限公司招聘岗位.doc") mustBe
      ("/home/hldev/Documents/重庆华龙网海数科技有限公司招聘岗位", "doc")
    DocumentUtils.splitNameSuffix("/home/hldev/Documents/重庆华龙网海数科技有限公司招聘岗位") mustBe
      ("/home/hldev/Documents/重庆华龙网海数科技有限公司招聘岗位", "")
    DocumentUtils.splitNameSuffix("") mustBe
      ("", "")
    DocumentUtils.splitNameSuffix("1.") mustBe
      ("1", "")
    DocumentUtils.splitNameSuffix(".") mustBe
      ("", "")
  }

  "DocUtilsTest" should {
    "getTextFromPath doc" in {
      val text = DocumentUtils.getTextFromPath(Paths.get("/home/hldev/Documents/重庆华龙网海数科技有限公司招聘岗位.doc"))
      text must not be empty
      println(text)
    }

    "getTextFromPath docx" in {
      val text = DocumentUtils.getTextFromPath(Paths.get("/home/hldev/Downloads/经济报告（第一页）.docx"))
      text must not be empty
      println(text)
    }

    "getMediaType" in {
      val path = Paths.get("/home/hldev/Downloads/经济报告（第一页）.docx")
      val in = Files.newInputStream(path)
      val mediaType = DocumentUtils.getMediaType(in)
      mediaType must not be null
      in.close()
    }

    //    "allMimeTypes" in {
    //      DocumentUtils.config.getMimeRepository.getMim
    //    }
  }

}
