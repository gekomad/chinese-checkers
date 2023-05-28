# Chinese Checkers with Bitboard Hash

    # # # # # # # #
    # # O O O # # #
    # # O O O # # #
    O O O O O O O #
    O O O - O O O #
    O O O O O O O #
    # # O O O # # #
    # # O O O # # #


Each checkerboard is a 64-bit word where 'O' is 1-bit, '-' is 0-bit and '#' means space not available. With bitwise operators (AND , OR, etc) you can move the pieces.

We need of two checkerboards, the first one to set the pieces, the second to specify the terrain.

The checkerboard up has these bits 00000000_00111000_00111000_11111110_11101110_11111110_00111000_00111000 and his exadecimal notation is 0x3838FEEEFE3838, the terrain is
00111000_00111000_11111110_11111110_11111110_00111000_00111000 (0x3838FEFEFE3838).

You can use the Bitmap Calculator (https://gekomad.github.io/Cinnamon/BitboardCalculator) to manipulate the bit mask.

To speed up the backtracking process, an Hash Table of 64-bit words is used.

### C:

`cd c`

`gcc -O3 main.c -o checkers`

`./checkers`

### C++:

`cd cpp`

`g++ -O3 main.cpp -pthread -o checkers`

`./checkers [n_thread]`


### Rust:

`cd rust`

`cargo run --release`

### Vala:

`cd vala`

`valac ChineseCheckers.vala --Xcc=-O3 -o checkers `

`./checkers`

### Pure functional Scala:

`cd scala`

`sbt run`

