package io.univalence.zio_awesome

private object ZioDataTypes {

  /**
   * @tparam R ce dont j'ai besoin
   * @tparam E quand cela va mal
   * @tparam A quand tout se passe bien
   */
  trait ZIO[-R, +E, +A] {
    def unsafeRun: R => Either[E, A]

    def flatMap[R1 <: R, E1 >: E, B](f0: A => ZIO[R1, E1, B]): ZIO[R1, E1, B]
    def fork: URIO[R, Fiber[E, A]]
  }

  //besoin de rien
  type IO[+E, +A] = ZIO[Any, E, A]

  //tout va bien se passer
  type UIO[+A] = ZIO[Any, Nothing, A]

  //avec R, on peut faire un A
  type URIO[-R, A] = ZIO[R, Nothing, A]

  //pourquoi ?
  type Exit[+E, +A] = Either[Cause[E], A]

  sealed trait Cause[+E]
  case class Fail[+E](value: E)               extends Cause[E]
  case class Die(value: Throwable)            extends Cause[Nothing]
  case class Interrupt(fiberId: (Long, Long)) extends Cause[Nothing]

  trait Fiber[+E, +A] {
    def interrupt: UIO[Exit[E, A]]
    def join: IO[E, A]
  }

  trait Promise[E, A] {
    def await: IO[E, A]
    def complete(io: IO[E, A]): UIO[Boolean]
  }

  trait Queue[-R, +E, A] {
    def isShutdown: UIO[Boolean]
    def offer(a: A): ZIO[R, E, Boolean]
    def take: ZIO[R, E, A]
  }

  trait Ref[A] {
    def get: UIO[A]
    def modify[B](f: A => (B, A)): UIO[B]
  }

  case class Reservation[-R, +E, +A](acquire: ZIO[R, E, A], release: Exit[Any, Any] => ZIO[R, Nothing, Any])

  trait ZManaged[-R, +E, +A] {
    def reservation: ZIO[R, E, Reservation[R, E, A]]

    def flatMap[R1 <: R, E1 >: E, B](f0: A => ZManaged[R1, E1, B]): ZManaged[R1, E1, B]
  }

  trait Stream[-R, +E, +A] {
    //1. on "acquire" la stream
    //2. on "pull" les éléments un par un(ZIO[R, Option[E], A])
    //3. si on a un None dans le Option[E], on arrête
    //4. on "release"
    def process: ZManaged[R, Nothing, ZIO[R, Option[E], A]]

    def flatMap[R1 <: R, E1 >: E, B](f0: A => Stream[R1, E1, B]): Stream[R1, E1, B]
  }

  trait Semaphore {
    def available: UIO[Long]
    def withPermits[R, E, A](n: Long)(task: ZIO[R, E, A]): ZIO[R, E, A]
  }

}
