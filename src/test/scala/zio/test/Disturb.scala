package zio.test

import zio.ZIO.{ EffectTotal, FlatMap, InterruptStatus, Succeed }
import zio.{ IO, UIO, ZIO }

object Disturb {

  private def loop[R, E, A](effect: ZIO[R, E, A], remainingStep: Int): ZIO[R, Option[E], (Int, A)] = {

    def next[R1, E1, A1](effect: ZIO[R1, E1, A1]): ZIO[R1, Option[E1], (Int, A1)] = loop(effect, remainingStep - 1)

    def empty: IO[None.type, Nothing] = ZIO.failNow(None)

    def lift[R1, E1, A1](effect: ZIO[R1, E1, A1], steps: Int = 1): ZIO[R1, Option[E1], (Int, A1)] =
      effect.mapError(x => Option(x)).map((remainingStep - steps) -> _)

    effect match {
      case e: InterruptStatus[R, E, A] if e.flag.isInterruptible => next(e.zio)
      case e: InterruptStatus[R, E, A]                           => lift(e) //Will not disturb something not interruptible.
      case _ if remainingStep <= 0                               => empty
      case f: FlatMap[R, E, _, A]                                => next(f.zio) >>= { case (n, x) => loop(f.k(x), n) }
      case e: EffectTotal[A]                                     => lift(e)
      case e: Succeed[A]                                         => lift(e, steps = 0)
      //case _ => ??? // implement for other subclasses.
    }
  }

  /**
   * cut the execution of an effect after n sub operations
   */
  def maxSteps[R, E, A](effect: ZIO[R, E, A], n: Int): ZIO[R, Option[E], A] =
    loop(effect, n).map(_._2)

}
