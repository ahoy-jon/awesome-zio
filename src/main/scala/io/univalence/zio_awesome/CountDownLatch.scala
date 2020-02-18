package io.univalence.zio_awesome

import zio.{ Promise, Ref, UIO, ZIO }

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
