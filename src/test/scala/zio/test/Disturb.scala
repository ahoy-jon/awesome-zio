package zio.test

import zio.ZIO._
import zio.ZIO

object Disturb {

  /**
   * cut the execution of an effect after n sub operations
   */
  def maxSteps[R, E, A](effect: ZIO[R, E, A], n: Int): ZIO[R, Option[E], A] =
    loop(effect, n).map(_.value)

  private final case class Value[+A](remainingSteps: Int, value: A)

  private def loop[R, E, A](effect: ZIO[R, E, A], remainingSteps: Int): ZIO[R, Option[E], Value[A]] = {
    def next[R1, E1, A1](effect: ZIO[R1, E1, A1]): ZIO[R1, Option[E1], Value[A1]] = loop(effect, remainingSteps - 1)

    def lift[R1, E1, A1](effect: ZIO[R1, E1, A1]): ZIO[R1, Option[E1], Value[A1]] =
      effect.asSomeError.map(value => Value(remainingSteps = remainingSteps - 1, value = value))

    effect match {
      case e: InterruptStatus[R, E, A] if e.flag.isInterruptible => next(e.zio)
      case e: InterruptStatus[R, E, A]                           => lift(e) //Will not disturb something not interruptible.
      case _ if remainingSteps <= 0                              => ZIO.failNow(None)
      case e: EffectTotal[A]                                     => lift[Any, Nothing, A](e)
      case e: Succeed[A]                                         => lift[Any, Nothing, A](e)
      //case _ => ??? // implement for other subclasses.
      case f: FlatMap[R, E, _, A] =>
        next(f.zio) >>= { x =>
          loop(f.k(x.value), x.remainingSteps)
        }
    }
  }
}
