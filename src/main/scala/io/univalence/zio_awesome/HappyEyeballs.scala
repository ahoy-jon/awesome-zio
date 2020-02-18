package io.univalence.zio_awesome

import zio.clock.Clock
import zio.duration.Duration
import zio.{ IO, Promise, UIO, URIO, ZIO }

object HappyEyeballs {
  //from https://blog.softwaremill.com/happy-eyeballs-algorithm-using-zio-120997ba5152
  //modification to make it more readeable
  def happyEyeballs[R, E, T](tasks: List[ZIO[R, E, T]], delay: Duration): ZIO[R with Clock, Option[E], T] =
    tasks match {
      case Nil         => IO.fail(None)
      case task :: Nil => task.asSomeError
      case task :: otherTasks =>
        Promise.make[Nothing, Unit].flatMap { isFailed =>
          val signalFailed: UIO[Unit]                = isFailed.succeed(Unit).ignore
          val waitOrAlreadyFailed: URIO[Clock, Unit] = isFailed.await.timeout(delay).ignore

          task
            .tapError(_ => signalFailed)
            .asSomeError
            .race(waitOrAlreadyFailed *> happyEyeballs(otherTasks, delay))
        }
    }
}
