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

package com.github.levkhomich.akka.tracing

// see https://github.com/twitter/finagle/blob/master/finagle-http/src/main/scala/com/twitter/finagle/http/Codec.scala
object TracingHeaders {
  val TraceId = "X-B3-TraceId"
  val SpanId = "X-B3-SpanId"
  val ParentSpanId = "X-B3-ParentSpanId"
  val Sampled = "X-B3-Sampled"
  val Flags = "X-B3-Flags"
}
