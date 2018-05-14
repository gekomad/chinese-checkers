package chineseCheckers.solver

trait Solver extends GameDef {

  def gen(board: (Long, List[Long]), acc: Stream[List[Long]]): Stream[List[Long]] = {

    val boardu64 = board._1
    val stack = board._2
    nmoves = nmoves + 1

    if (nmoves % 50000000L == 0)
      println(s"Nmoves: $nmoves Cut: $cut ${cut * 100 / nmoves}% ")

    if (hashArray.get((boardu64 % HASH_SIZE).toInt) == boardu64) {
      cut = cut + 1

      Stream.empty
    } else {

      if (stack.length == TOT - 1) {
        Nsolution = Nsolution + 1
        printStack(boardu64 :: stack)
        Stream(boardu64 :: stack)
      } else {

        val res = {
          for {
            movement <- movements(boardu64)
            (from, (captured, to)) <- movement

          } yield gen((((boardu64 & ~from) | to) & ~captured, boardu64 :: stack), acc)

        }.flatten.toStream


        hashArray.put((boardu64 % HASH_SIZE).toInt, boardu64)
        res #::: acc
      }
    }
  }
}
