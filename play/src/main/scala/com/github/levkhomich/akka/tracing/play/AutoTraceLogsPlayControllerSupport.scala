package com.github.levkhomich.akka.tracing.play

import play.api.mvc.RequestHeader
import play.libs.Akka

trait AutoTraceLogsPlayControllerSupport extends PlayControllerTracing {

  lazy protected val log : TracedLog = new TracedLog

  protected class TracedLog {
    val loggingSystem = akka.event.Logging(Akka.system(), this.getClass)

    def info(message: String)(implicit request : RequestHeader): Unit = {
      trace.record(request,s"INFO.traced: $message")
      loggingSystem.info(message)
    }

    def error(message: String)(implicit request: RequestHeader): Unit = {
      trace.record(request, s"ERROR.traced: $message")
      loggingSystem.error(message)
    }

    def error(cause: Throwable, message: String)(implicit request: RequestHeader): Unit = {
      trace.record(request, s"ERROR.traced: Exception follows: ${message}")
      trace.record(request, cause)
      loggingSystem.error(cause, message)
    }

    def warning(message: String)(implicit request: RequestHeader): Unit = {
      trace.record(request, s"WARN.traced: $message")
      loggingSystem.warning(message)
    }

    def warn(message: String)(implicit request: RequestHeader): Unit = {
      trace.record(request, s"WARN.traced: $message")
      loggingSystem.warning(message)
    }

    def debug(message: String)(implicit request: RequestHeader): Unit = {
      trace.record(request, s"DEBUG.traced: $message")
      loggingSystem.debug(message)
    }
  }

}
