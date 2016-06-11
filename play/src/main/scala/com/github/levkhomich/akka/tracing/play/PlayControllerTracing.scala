/**
 * Copyright 2014 the Akka Tracing contributors. See AUTHORS for more details.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.levkhomich.akka.tracing.play

import akka.event.LoggingAdapter
import play.api.mvc._
import com.github.levkhomich.akka.tracing.{TracingExtension, TracingExtensionImpl}
import play.libs.Akka


trait PlayControllerTracing {

  protected implicit def requestHeader2TracingSupport(headers: RequestHeader): PlayRequestTracingSupport =
    new PlayRequestTracingSupport(headers)

  protected implicit def trace: TracingExtensionImpl = TracingExtension(play.libs.Akka.system)

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