package chineseCheckers

import chineseCheckers.solver.{GameDef, Solver}

object Main extends App {

  new GameDef(HASH_SIZE = 1229498, INIT_BOARD = 0x3838FEEEFE3838L, TERRAIN = 0x3838FEFEFE3838L) with Solver {

    /* TERRAIN
    # # # # # # # #
    # # O O O # # #
    # # O O O # # #
    O O O O O O O #
    O O O O O O O #
    O O O O O O O #
    # # O O O # # #
    # # O O O # # #
     */

    /* INIT_BOARD
    # # # # # # # #
    # # O O O # # #
    # # O O O # # #
    O O O O O O O #
    O O O - O O O #
    O O O O O O O #
    # # O O O # # #
    # # O O O # # #
     */


    printBoard(board)
    println("\n------------------------")
    override val startTime = System.currentTimeMillis
    val l: Stream[List[Long]] = gen((board, List.empty), Stream.empty)

    l.foreach {
        println("solution:")
        _.reverse.foreach(println)
    }
  }
}
