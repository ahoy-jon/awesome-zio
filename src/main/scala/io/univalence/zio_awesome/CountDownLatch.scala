package io.univalence.zio_awesome

import java.util.concurrent.TimeUnit

import zio.blocking.Blocking
import zio.{ Promise, RIO, Ref, Task, UIO, URIO, ZIO }

trait CountDownLatch {
  def countDown: UIO[Unit]
  def await: UIO[Unit]
}

object CountDownLatchOld {
  def make(count: Int): UIO[CountDownLatch] =
    for {
      ready <- Promise.make[Nothing, Unit]
      ref   <- Ref.make(count)
    } yield new CountDownLatch {
      final override def countDown: UIO[Unit] =
        ref.updateAndGet(_ - 1) >>= {
          case 0 => ready.succeed(()).unit
          case _ => ZIO.unit
        }

      final override def await: UIO[Unit] = ready.await
    }
}

object CountDownLatch {
  def make(count: Int): UIO[CountDownLatch] =
    if (count <= 0)
      UIO(new CountDownLatch {
        override def countDown: UIO[Unit] = ZIO.unit
        override def await: UIO[Unit]     = ZIO.unit
      })
    else
      for {
        ready <- Promise.make[Nothing, Unit]
        ref   <- Ref.make(count)
      } yield new CountDownLatch {
        final override def countDown: UIO[Unit] =
          (ref.updateAndGet(_ - 1) >>= {
            case 0 => ready.succeed(()).ignore
            case _ => ZIO.unit
          }).uninterruptible

        final override def await: UIO[Unit] = ready.await
      }
}

object CountDownLatchJavaLike {
  def make(count: Int): UIO[CountDownLatch] =
    for {
      ref <- Ref.make(count)
    } yield new CountDownLatch {
      override def countDown: UIO[Unit] = ref.update(_ - 1)
      override def await: UIO[Unit]     = ref.get.doUntil(_ <= 0).unit
    }
}

object CountDownLatchWrapJava {
  def make(count: Int): UIO[CountDownLatch] =
    for {
      cdl <- WrapImpure.wrapTotal(new java.util.concurrent.CountDownLatch(count))
    } yield {
      new CountDownLatch {
        override def countDown: UIO[Unit] = cdl.execTotal(_.countDown())
        override def await: UIO[Unit]     = cdl.execTotal(_.getCount()).doUntil(_ <= 0L).unit
      }
    }
}

//Ou une version plus technique avec l'aide de Adam Fraser
object CountDownLatchWrapJavaBlock {
  def make(count: Int): URIO[Blocking, CountDownLatch] =
    for {
      blocking <- ZIO.environment[Blocking]
      cdl      <- WrapImpure.wrapTotal(new java.util.concurrent.CountDownLatch(count))
    } yield {
      new CountDownLatch {
        override def countDown: UIO[Unit] = cdl.execTotal(_.countDown())
        override def await: UIO[Unit]     = cdl.execBlockInterrupt(_.await()).orDie.provide(blocking)
      }
    }
}

//regarde maman, sans Kleisli
final class WrapImpure[B](private val value: B) {
  def exec[C](f: B => C): Task[C]                        = ZIO.effect(f(value))
  def execTotal[C](f: B => C): UIO[C]                    = ZIO.effectTotal(f(value))
  def execBlockInterrupt[C](f: B => C): RIO[Blocking, C] = zio.blocking.effectBlockingInterrupt(f(value))
}

object WrapImpure {
  def wrap[B](b: => B): Task[WrapImpure[B]]     = ZIO.effect(new WrapImpure(b))
  def wrapTotal[B](b: => B): UIO[WrapImpure[B]] = ZIO.effectTotal(new WrapImpure(b))
}
