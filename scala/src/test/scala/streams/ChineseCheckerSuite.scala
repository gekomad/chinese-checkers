package streams

import chineseCheckers.solver.{GameDef, Solver}
import org.scalatest.FunSuite

class ChineseCheckerSuite extends FunSuite {

  test("shift") {

    new GameDef(HASH_SIZE = 1, INIT_BOARD = 0, TERRAIN = 0x3838FEFEFE3838L) with Solver {

      assert(bitsStream(0, 0, Right) == Stream.empty)

      val a = 100L
      val b = a >>> 1
      val c = b << 1
      assert(a == c)

      val bits = 0xc000000000000000L
      val prec = bits >>> 1
      assert(0x6000000000000000L == prec)

    }
  }

  test("bitsStream") {

    new GameDef(HASH_SIZE = 1, INIT_BOARD = 100, TERRAIN = 0x3838FEFEFE3838L) with Solver {

      val o = for {
        l2 <- bitsStream(100, 8, Right)
      } yield l2

      assert(List((8, (4, 2))) == o.toList)

    }
  }

  test("solution") {


    new GameDef(HASH_SIZE = 1, INIT_BOARD = 0xc000000L, TERRAIN = 0x3838FEFEFE3838L) with Solver {
      /*
         # # # # # # # #
         # # - - - # # #
         # # - - - # # #
         - - - - - - - #
         - - - - O O - #
         - - - - - - - #
         # # - - - # # #
         # # - - - # # #
        */

      printBoard(board)
      println("\n------------------------")
      override val startTime = System.currentTimeMillis
      val l: Stream[List[Long]] = gen((board, List.empty), Stream.empty)

      /*

      solution: 1

      # # # # # # # #
      # # - - - # # #
      # # - - - # # #
      - - - - - - - #
      - - - - - - O #
      - - - - - - - #
      # # - - - # # #
      # # - - - # # #

     solution: 2

      # # # # # # # #
      # # - - - # # #
      # # - - - # # #
      - - - - - - - #
      - - - O - - - #
      - - - - - - - #
      # # - - - # # #
      # # - - - # # #

     */

      l.foreach {
        t =>
          println("solution:")
          t.reverse.foreach(println(_))
      }
      assert(l.toList == List(List(33554432, 201326592), List(268435456, 201326592)))

    }
  }


}
