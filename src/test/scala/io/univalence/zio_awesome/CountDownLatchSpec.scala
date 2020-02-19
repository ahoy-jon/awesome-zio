package io.univalence.zio_awesome

import java.util.concurrent.TimeUnit

import zio._
import zio.blocking.Blocking
import zio.test._

import scala.concurrent.duration.Duration

object CountDownLatchSpec extends DefaultRunnableSpec {

  val make: Int => URIO[Blocking, CountDownLatch] = CountDownLatch.make

  override def spec =
    suite("finish")(
      testM("basic scenario")(for {
        l <- make(2)
        _ <- l.countDown
        _ <- l.countDown
        _ <- l.await
      } yield {
        assertCompletes
      }),
      testM("keep the gate closed")(for {
        l  <- make(2)
        _  <- l.countDown
        r1 <- runningOrError(l.await)
        _  <- l.countDown
        r2 <- completedOrError(l.await)
      } yield {
        r1 && r2
      }),
      testM("make 0 should be available")({
        for {
          countDownLatch <- make(0)
          r              <- completedOrError(countDownLatch.await)
        } yield r
      }),
      testM("basic scenario with disturb")(
        checkAllM(Gen.fromIterable(0 to 10))(
          steps =>
            for {
              cdl <- make(2)
              _   <- cdl.countDown
              _   <- Disturb.maxSteps(cdl.countDown, steps).orElse(cdl.countDown)
              r   <- completedOrError(cdl.await)
            } yield {
              r
            }
        )
      )
    )
  private val wait50ms: UIO[Unit] = ZIO.effectTotal(Thread.sleep(50))

  import zio.blocking.blocking

  def runningOrError[R, E, A](effect: ZIO[R, E, A], error: String = ""): ZIO[R with Blocking, E, TestResult] =
    blocking(effect)
      .as(assert(value = true)(Assertion.isFalse.label(error)))
      .race(wait50ms.as(assertCompletes))

  def completedOrError[R, E, A](effect: ZIO[R, E, A], error: String = ""): ZIO[R with Blocking, E, TestResult] =
    blocking(effect)
      .as(assertCompletes)
      .race(wait50ms.as(assert(value = true)(Assertion.isFalse.label(error))))

}
