package io.univalence.zio_awesome

object DisabledThread extends zio.App {
  import zio.blocking._
  import zio.ZIO
  override def run(args: List[String]): ZIO[Blocking, Nothing, Int] =
    for {
      cdl <- ZIO.effectTotal(new java.util.concurrent.CountDownLatch(1))
      _   <- effectBlockingInterrupt(cdl.await()).orDie.race(ZIO.unit)
    } yield 0
}
