package com.github.levkhomich.akka.tracing

import java.util.concurrent.TimeoutException

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration

class EnabledStrategyTracingSpecification extends AkkaTracingSpecification with MockCollector {

  val system = testActorSystem(1,Some("com.github.levkhomich.akka.tracing.NoopTracingEnabledStrategy"))
  implicit val trace = TracingExtension(system)

  sequential

  "TracingExtension" should {
    "not trace when enabled strategy is NoopTracingEnabledStrategy" in {
      def generateTracesWithSampleRate(count: Int, sampleRate: Int): Unit = {
        val system = testActorSystem(1,Some("com.github.levkhomich.akka.tracing.NoopTracingEnabledStrategy"))
        generateTraces(count, TracingExtension(system))
        Thread.sleep(3000)
        system.shutdown()
        system.awaitTermination(FiniteDuration(5, duration.SECONDS)) must not(throwA[TimeoutException])
      }

      generateTracesWithSampleRate(2, 1)
      generateTracesWithSampleRate(60, 2)
      generateTracesWithSampleRate(500, 5)

      // Collector should get no results
      results.size() must beEqualTo(0)
    }

    "crash if provided with non existent enabled-strategy " in {
      testActorSystem(1,Some("non-existent-class")) must throwA[RuntimeException]
    }

    "shutdown correctly" in {
      system.shutdown()
      collector.stop()
      system.awaitTermination(FiniteDuration(5, duration.SECONDS)) must not(throwA[TimeoutException])
    }

  }
}
