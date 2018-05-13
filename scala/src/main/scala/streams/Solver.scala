package streams

import java.nio.LongBuffer

trait SolutionChecker extends GameDef with Solver


trait Type1 extends SolutionChecker {

  /*

     Bitboard Calculator: http://cinnamonchess.altervista.org/bitboard_calculator/Calc.html

     8| 63 62 61 60 59 58 57 56
     7| 55 54 53 52 51 50 49 48
     6| 47 46 45 44 43 42 41 40
     5| 39 38 37 36 35 34 33 32
     4| 31 30 29 28 27 26 25 24
     3| 23 22 21 20 19 18 17 16
     2| 15 14 13 12 11 10 09 08
     1| 07 06 05 04 03 02 01 00
     --------------------------
        7  6  5  4  3  2  1  0

   */

  override val TERRAIN = 0x3838FEFEFE3838L
  /*
     # # # # # # # #
     # # O O O # # #
     # # O O O # # #
     O O O O O O O #
     O O O O O O O #
     O O O O O O O #
     # # O O O # # #
     # # O O O # # #
   */

  override val INIT_BOARD = 0x3838FEEEFE3838L
  /*
     # # # # # # # #
     # # O O O # # #
     # # O O O # # #
     O O O O O O O #
     O O O - O O O #
     O O O O O O O #
     # # O O O # # #
     # # O O O # # #
    */

}

object Main extends App {

  new Type1 {

    val board = Board(INIT_BOARD)
    override val TOT = board.popCount()
    override val hashArray = LongBuffer.allocate(HASH_SIZE)

    board.printBoard
    println("\n------------------------")
    override val start_time = System.currentTimeMillis
    val l: Stream[List[Board]] = gen((board, List.empty), Stream.empty)

    l.foreach {
      t =>
        println("solution:")
        t.reverse.foreach(println(_))
    }

  }
}

trait Solver extends GameDef {

  def gen(board: (Board, List[Board]), acc: Stream[List[Board]]): Stream[List[Board]] = {

    val boardu64 = board._1.board
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
        printStack(board._1 :: stack)
        Stream(board._1 :: stack)
      } else {

        val res = {
          for {
            movement <- board._1.movements
            (from, (captured, to)) <- movement

          } yield gen((Board(((boardu64 & ~from) | to) & ~captured), board._1 :: stack), acc)

        }.flatten.toStream


        hashArray.put((boardu64 % HASH_SIZE).toInt, boardu64)
        res #::: acc
      }
    }
  }
}
