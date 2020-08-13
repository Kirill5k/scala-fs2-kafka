package io.kirill

import org.slf4j.LoggerFactory

trait Logging {
  val loggerName             = getClass.getName
  @transient lazy val logger = LoggerFactory.getLogger(loggerName)
}
