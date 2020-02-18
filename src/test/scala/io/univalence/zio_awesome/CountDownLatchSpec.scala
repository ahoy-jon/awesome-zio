package io.univalence.zio_awesome

import zio._
import zio.test._

object CountDownLatchSpec extends DefaultRunnableSpec {

  val make: Int => UIO[CountDownLatch] = CountDownLatchJavaLike.make

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
      testM("make 0 should be available")({
        for {
          countDownLatch <- make(0)
          r              <- completedOrError(countDownLatch.await, "should be completed")
        } yield r
      }),
      testM("basic scenario with disturb")(
        checkAllM(Gen.fromIterable(0 to 10))(
          steps =>
            for {
              cdl <- make(2)
              _   <- cdl.countDown
              _   <- Disturb.maxSteps(cdl.countDown, steps).orElse(cdl.countDown)
              r   <- completedOrError(cdl.await, "should be completed")
            } yield {
              r
            }
        )
      )
    )

  def completedOrError[R, E, A](effect: ZIO[R, E, A], error: => String): ZIO[R, E, TestResult] =
    effect
      .as(assertCompletes)
      .race(ZIO.effectTotal(Thread.sleep(50)) *> ZIO.succeed(assert(true)(Assertion.isFalse.label(error))))

}
