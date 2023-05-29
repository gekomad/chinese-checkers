package streams

import chineseCheckers.solver.{GameDef, Puzzle, Solver}

class ChineseCheckerSuite extends munit.FunSuite {

  test("shift") {

    new GameDef(HASH_SIZE = 1, TERRAIN = 0x3838fefefe3838L) with Solver {

      assert(bitsStream(0, 0, Right) == LazyList.empty)

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

    new GameDef(HASH_SIZE = 1, TERRAIN = 0x3838fefefe3838L) with Solver {

      val o = for {
        l2 <- bitsStream(100, 8, Right)
      } yield l2

      assert(List((8, (4, 2))) == o.toList)

    }
  }

  test("solution") {

    new GameDef(HASH_SIZE = 1, TERRAIN = 0x3838fefefe3838L) with Solver {
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
      val INIT_BOARD = 0xc000000L
      printBoard(INIT_BOARD)
      println("\n------------------------")
      override val startTime      = System.currentTimeMillis
      val l: LazyList[List[Long]] = gen((INIT_BOARD, List.empty), LazyList.empty, popCount(INIT_BOARD))

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

      l.foreach { t =>
        println("solution:")
        t.reverse.foreach(println(_))
      }
      assert(l.toList == List(List(33554432, 201326592), List(268435456, 201326592)))

    }
  }

  test("puzzle") {

    val T = 0x3838fefefe3838L
    new GameDef(HASH_SIZE = 1, TERRAIN = T) with Puzzle {
      /*
       # # # # # # # #
       # # - - - # # #
       # # - - - # # #
       - - - - - - - #
       - - - - - - - #
       - - - - - - - #
       # # - - - # # #
       # # - - - # # #
       */

      println("\n------------------------------")
      override val startTime = System.currentTimeMillis
      val nPieces            = 5

      def getSol(TERRAIN: Long, nPieces: Int, count: Int): Option[Long] = {
        if (count <= 0) None
        else {
          val l = puzzle(randomBit(TERRAIN), nPieces)
          l.fold(getSol(TERRAIN, nPieces, count - 1)) { _ =>
            l
          }
        }
      }

      val sol1 = getSol(TERRAIN, nPieces, 5)
      sol1.fold(println("no solutions")) { sol =>
        printBoard(sol)
        assert(popCount(sol) == nPieces)

        val a = new GameDef(HASH_SIZE = 1229498, TERRAIN = T) with Solver {
          printBoard(sol)
          maxSolution = 1
          override val startTime      = System.currentTimeMillis
          val l: LazyList[List[Long]] = gen((sol, List.empty), LazyList.empty, popCount(sol))
          println(s"n solutions: ${l.size}")
          assert(l.nonEmpty)
        }
        println(a)
      }
    }
  }

}
