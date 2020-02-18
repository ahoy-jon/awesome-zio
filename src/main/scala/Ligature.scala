package univalence.demo

object FlatMapDemo {

  def makeInt(str: String): Option[Int] = ???
  def getString: Option[String]         = ???

  /*

  getString.flatMap(makeInt)
  getString flatMap makeInt
  getString >>= makeInt
   */

  implicit class OptionOps[A](val opt: Option[A]) extends AnyVal {
    @inline def >>=[B](f: A => Option[B]): Option[B] = opt flatMap f
  }

  def main(args: Array[String]): Unit = {

    val result = for {
      n <- List(1, 2, 3, 4)
      c <- List('a', 'b', 'c', 'd')
    } yield n.toString + c.toString

    //List(1a, 1b, 1c, 1d, 2a, 2b, 2c, 2d, 3a, 3b, 3c, 3d, 4a, 4b, 4c, 4d)
    println(result)
  }

}
