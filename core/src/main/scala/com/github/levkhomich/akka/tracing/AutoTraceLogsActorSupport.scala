package com.github.levkhomich.akka.tracing

trait AutoTraceLogsActorSupport extends ActorTracing {

  lazy protected val log = new TracedActorLog

  protected class TracedActorLog {
    val loggingSystem = akka.event.Logging(context.system, this.getClass)

    def info(message: String)(implicit source : TraceSource): Unit = {
      trace.record(source.msg,s"INFO.actortraced: $message")
      loggingSystem.info(message)
    }

    def error(message: String)(implicit source : TraceSource): Unit = {
      trace.record(source.msg, s"ERROR.actortraced: $message")
      loggingSystem.error(message)
    }

    def error(cause: Throwable, message: String)(implicit source : TraceSource): Unit = {
      trace.record(source.msg, s"ERROR.actortraced: Exception follows: ${message}")
      trace.record(source.msg, cause)
      loggingSystem.error(cause, message)
    }

    def warning(message: String)(implicit source : TraceSource): Unit = {
      trace.record(source.msg, s"WARN.actortraced: $message")
      loggingSystem.warning(message)
    }

    def warn(message: String)(implicit source : TraceSource): Unit = {
      trace.record(source.msg, s"WARN.actortraced: $message")
      loggingSystem.warning(message)
    }

    def debug(message: String)(implicit source : TraceSource): Unit = {
      trace.record(source.msg, s"DEBUG.actortraced: $message")
      loggingSystem.debug(message)
    }
  }


}
