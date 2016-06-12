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

import scala.collection.Map
import scala.concurrent.Future
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import play.api.{GlobalSettings, Routes}
import play.api.libs.iteratee.Iteratee
import play.api.mvc._

import com.github.levkhomich.akka.tracing.http.TracingHeaders


trait TracingSettings extends GlobalSettings with PlayControllerTracing {

  protected def sample(request: RequestHeader): Unit = {
    trace.sample(request, play.libs.Akka.system.name)
  }

  protected def addHttpAnnotations(request: RequestHeader): Unit = {
    // TODO: use batching
    trace.recordKeyValue(request, "request.path", request.path)
    trace.recordKeyValue(request, "request.method", request.method)
    trace.recordKeyValue(request, "request.secure", request.secure)
    trace.recordKeyValue(request, "request.proto", request.version)
    trace.recordKeyValue(request, "client.address", request.remoteAddress)
    // TODO: separate cookie records
    request.queryString.foreach { case (key, values) =>
      values.foreach(trace.recordKeyValue(request, "request.query." + key, _))
    }
    request.headers.toMap.foreach { case (key, values) =>
      values.foreach(trace.recordKeyValue(request, "request.headers." + key, _))
    }
  }

  protected def requestTraced(request: RequestHeader): Boolean =
    !request.path.startsWith("/assets")

  protected def extractTracingTags(request: RequestHeader): Map[String, String] = {
    val spanId = TracingHeaders.SpanId -> Random.nextLong.toString
    if (request.headers.get(TracingHeaders.TraceId).isEmpty)
      Map(TracingHeaders.TraceId -> Random.nextLong.toString) + spanId
    else
      TracingHeaders.All.flatMap(header =>
        request.headers.get(header).map(header -> _)
      ).toMap + spanId
  }

  protected class TracedAction(delegateAction: EssentialAction) extends EssentialAction with RequestTaggingHandler {
    override def apply(request: RequestHeader): Iteratee[Array[Byte], Result] = {
      import scala.concurrent.ExecutionContext.Implicits.global
      if (requestTraced(request)) {
        sample(request)
        addHttpAnnotations(request)
      }
      delegateAction(request) map { r => Result(r.header,r.body.onDoneEnumerating({
        trace.recordKeyValue(request, "statusCode", r.header.status)
        trace.finish(request)
      }),r.connection)
      }
    }

    override def tagRequest(request: RequestHeader): RequestHeader = {
      if (requestTraced(request))
        request.copy(tags = request.tags ++ extractTracingTags(request))
      else
        request
    }
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] =
    super.onRouteRequest(request).map {
      case alreadyTraced: TracedAction =>
        alreadyTraced
      case alreadyTagged: EssentialAction with RequestTaggingHandler =>
        new TracedAction(alreadyTagged) {
          override def tagRequest(request: RequestHeader): RequestHeader =
            super.tagRequest(alreadyTagged.tagRequest(request))
        }
      case action: EssentialAction =>
        new TracedAction(action)
      case ws @ WebSocket(f) =>
        WebSocket[ws.FramesIn, ws.FramesOut](request =>
          if (requestTraced(request)) {
            val taggedRequest = request.copy(tags = request.tags ++ extractTracingTags(request))
            sample(taggedRequest)
            addHttpAnnotations(taggedRequest)
            trace.finish(taggedRequest)
            ws.f(taggedRequest)
          } else
            ws.f(request)
        )(ws.inFormatter, ws.outFormatter)
      case handler =>
        handler
    }

  override def onRequestCompletion(request: RequestHeader): Unit = {
    trace.finish(request)
    super.onRequestCompletion(request)
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    trace.record(request, ex)
    super.onError(request, ex)
  }

}
