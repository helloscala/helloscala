package helloscala.slick

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.tminglei.slickpg._
import helloscala.common.data.NameValue
import helloscala.common.jackson.Jackson
import helloscala.common.types.ObjectId
import helloscala.common.util.TimeUtils
import slick.basic.Capability
import slick.jdbc.{JdbcCapabilities, PositionedParameters, SetParameter}

trait PgProfile
  extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgJsonSupport
  with PgHStoreSupport {

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate // JdbcCapabilities.insertOrUpdate

  override def pgjson = "jsonb"

  override val api: MyAPI.type = MyAPI

  object MyAPI
    extends API
    with ArrayImplicits
    with JsonImplicits
    with DateTimeImplicits
    with HStoreImplicits {

    implicit object SetZonedDateTime extends SetParameter[ZonedDateTime] {
      override def apply(v1: ZonedDateTime, v2: PositionedParameters): Unit = v2.setTimestamp(Timestamp.from(v1.toInstant))
    }

    implicit object SetOptionZonedDateTime extends SetParameter[Option[ZonedDateTime]] {
      override def apply(v1: Option[ZonedDateTime], v2: PositionedParameters): Unit = v2.setTimestampOption(v1.map(zdt => Timestamp.from(zdt.toInstant)))
    }

    implicit object SetLocalDateTime extends SetParameter[LocalDateTime] {
      override def apply(v1: LocalDateTime, v2: PositionedParameters): Unit = v2.setTimestamp(Timestamp.from(v1.toInstant(TimeUtils.ZONE_CHINA_OFFSET)))
    }

    implicit object SetOptionLocalDateTime extends SetParameter[Option[LocalDateTime]] {
      override def apply(v1: Option[LocalDateTime], v2: PositionedParameters): Unit = v2.setTimestampOption(v1.map(ldt => Timestamp.from(ldt.toInstant(TimeUtils.ZONE_CHINA_OFFSET))))
    }

    implicit object SetLocalDate extends SetParameter[LocalDate] {
      override def apply(v1: LocalDate, v2: PositionedParameters): Unit = v2.setDate(java.sql.Date.valueOf(v1))
    }

    implicit object SetOptionLocalDate extends SetParameter[Option[LocalDate]] {
      override def apply(v1: Option[LocalDate], v2: PositionedParameters): Unit = v2.setDateOption(v1.map(java.sql.Date.valueOf))
    }

    implicit object SetLocalTime extends SetParameter[LocalTime] {
      override def apply(v1: LocalTime, v2: PositionedParameters): Unit = v2.setTime(java.sql.Time.valueOf(v1))
    }

    implicit object SetOptionLocalTime extends SetParameter[Option[LocalTime]] {
      override def apply(v1: Option[LocalTime], v2: PositionedParameters): Unit = v2.setTimeOption(v1.map(java.sql.Time.valueOf))
    }

    implicit val jsonNodeColumnType: BaseColumnType[JsonNode] = MappedColumnType.base[JsonNode, JsonString](
      { node => JsonString(node.toString) }, { str => Jackson.defaultObjectMapper.readTree(str.value) })

    implicit val passportColumnType: BaseColumnType[NameValue] = MappedColumnType.base[NameValue, String](
      { node => Jackson.defaultObjectMapper.writeValueAsString(node) }, { str => Jackson.defaultObjectMapper.readValue(str, classOf[NameValue]) })

    implicit val objectNodeColumnType: BaseColumnType[ObjectNode] = MappedColumnType.base[ObjectNode, JsonString](
      { node => JsonString(node.toString) }, { str => Jackson.defaultObjectMapper.readValue(str.value, classOf[ObjectNode]) })

    implicit val objectIdTypeMapper: BaseColumnType[ObjectId] = MappedColumnType.base[ObjectId, String](
      { oid =>
        if (oid eq null) {
          throw new NullPointerException("objectIdTypeMapper: ObjectId is null")
        }
        oid.toString
      }, { str => ObjectId.apply(str) })

    implicit val strListTypeMapper: DriverJdbcType[List[String]] = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val int4ListTypeMapper: DriverJdbcType[List[Int]] = new SimpleArrayJdbcType[Int]("int4").to(_.toList)
    implicit val int8ListTypeMapper: DriverJdbcType[List[Long]] = new SimpleArrayJdbcType[Long]("int8").to(_.toList)

    implicit val objectJsonArrayTypeMapper: DriverJdbcType[List[JsonNode]] =
      new AdvancedArrayJdbcType[JsonNode](
        pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JsonNode](str => Jackson.defaultObjectMapper.readTree(str))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JsonNode](node => Jackson.defaultObjectMapper.writeValueAsString(node))(v)).to(_.toList)

    implicit val objectIdListTypeMapper: DriverJdbcType[List[ObjectId]] = new AdvancedArrayJdbcType[ObjectId](
      "text",
      (s) => utils.SimpleArrayUtils.fromString[ObjectId](str => ObjectId.apply(str))(s).orNull,
      (v) => utils.SimpleArrayUtils.mkString[ObjectId](id => id.toString())(v)).to(_.toList)

    type FilterCriteriaType = Option[Rep[Option[Boolean]]]

    def dynamicFilter(list: Seq[FilterCriteriaType]): Rep[Option[Boolean]] =
      list.collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(Some(true): Rep[Option[Boolean]])

    def dynamicFilter(item: Option[Rep[Boolean]], list: Option[Rep[Boolean]]*): Rep[Boolean] =
      (item +: list).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])

    def dynamicFilterOr(list: Seq[FilterCriteriaType]): Rep[Option[Boolean]] =
      list.collect({ case Some(criteria) => criteria }).reduceLeftOption(_ || _).getOrElse(Some(true): Rep[Option[Boolean]])

    trait HSTable[T] {
      this: Table[T] =>

      def sqlTypeObjectId = O.SqlType("char(24)")

      def sqlTypeSha256 = O.SqlType("char(64)")
    }

  }

}

object PgProfile extends PgProfile
