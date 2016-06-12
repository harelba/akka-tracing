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

import akka.actor.Actor
import play.api.routing.Router.Tags.RoutePattern
import play.api.mvc.RequestHeader

import com.github.levkhomich.akka.tracing.{TracingExtensionImpl, BaseTracingSupport, ActorTracing}
import com.github.levkhomich.akka.tracing.http.TracingHeaders

trait PlayActorTracing extends ActorTracing { self: Actor =>

  implicit def requestHeader2TracingSupport(headers: RequestHeader): PlayRequestTracingSupport =
    new PlayRequestTracingSupport(headers)

}


class PlayRequestTracingSupport(val headers: RequestHeader) extends AnyVal with BaseTracingSupport {

  override private[tracing] def $spanId: Long =
    headers.tags.get(TracingHeaders.SpanId).map(java.lang.Long.parseLong).getOrElse(
      throw new IllegalStateException(
        "Traced request was not properly tagged. Probably, TracingSettings was not mixed into global Play config."
      )
    )

  override private[tracing] def sample(): Unit = {}

  override private[tracing] def $traceId: Option[Long] =
    headers.tags.get(TracingHeaders.TraceId).map(java.lang.Long.parseLong)

  override protected[tracing] def spanName: String = {
    val route = headers.tags.getOrElse(RoutePattern, headers.path)
    headers.method + " " + route
  }

  override def asChildOf(ts: BaseTracingSupport)(implicit tracer: TracingExtensionImpl): BaseTracingSupport =
    throw new IllegalStateException()

  override private[tracing] def $parentId: Option[Long] =
    headers.tags.get(TracingHeaders.ParentSpanId).map(java.lang.Long.parseLong)

  override private[tracing] def isSampled: Boolean =
    headers.tags.get(TracingHeaders.TraceId).isDefined
}


