package hello

/*
import zio.{ App, Ref, UIO, URIO, ZIO }
import zio.console.{ putStrLn, Console }

trait SomeDep {
  val someDep: SomeDep.Service
}

object SomeDep {
  trait Service {
    def getCount: UIO[Int]
  }

  trait Live extends SomeDep {
    val someDepCounter: Ref[Int]
    final override val someDep: Service = new Service {
      override def getCount: UIO[Int] = someDepCounter.get
    }
  }

  def getCount: URIO[SomeDep, Int] = ZIO.accessM(_.someDep.getCount)

}

object Main extends App {

  val makeSomeDep: UIO[SomeDep] = Ref
    .make(10)
    .map(
      x =>
        new SomeDep.Live {
          override val someDepCounter: Ref[Int] = x
        }
    )

  val diplayN: URIO[SomeDep with Console, Unit] = for {
    i <- SomeDep.getCount
    _ <- zio.console.putStrLn(i.toString)
  } yield {}

  def mixManualy(console_ : Console, someDep_ : SomeDep): Console with SomeDep =
    new SomeDep with Console {
      override val someDep: SomeDep.Service      = someDep_.someDep
      override val console: Console.Service[Any] = console_.console
    }

  val injectManualy: ZIO[Console, Nothing, Unit] = for {
    somedep_ <- makeSomeDep
    end      <- diplayN.provideSome[Console](mixManualy(_, somedep_))
  } yield end

  val injectWithMacro: ZIO[Console, Nothing, Unit] = {
    import zio.macros.delegate.syntax._

    makeSomeDep.flatMap(somedep => diplayN.providePart(somedep))
  }

  def run(args: List[String]): URIO[Console, Int] = injectWithMacro.as(0)
}
 */
