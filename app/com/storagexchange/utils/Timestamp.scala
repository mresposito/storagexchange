package com.storagexchange.utils

import java.sql.Timestamp
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

object TimestampFormatter {

  implicit val rds: Reads[Timestamp] = ( __ \ "time").read[Long].map { time => new Timestamp(time) }
  implicit val wrs: Writes[Timestamp] = (__ \ "time").write[Long].contramap{ (a: Timestamp) => a.getTime }
  implicit val fmt: Format[Timestamp] = Format(rds, wrs)
}
