/*
    CHINESE CHECKERS - find all solutions - vala source

	run: valac ChineseCheckers.vala --Xcc=-O3;./ChineseCheckers

 	 	O	O	O
 	 	O	O	O
O	O	O	O	O	O	O
O	O	O	-	O	O	O
O	O	O	O	O	O	O
 	 	O	O	O
 	 	O	O	O
 */


public class ChineseCheckers
{

  private uint64 board;
  private uint64 nmoves;
  private uint64 cut;

  const int HASH_SIZE = 1229498;

// Bitboard Calculator: http://cinnamonchess.altervista.org/bitboard_calculator/Calc.html

  private const uint64 INIT_BOARD = 0x3838FEEEFE3838ULL;
  private const uint64 BOARD = 0x3838FEFEFE3838ULL;
  private const uint64 BOARD_to_RIGHT = 0x2020F8F8F82020ULL;
  private const uint64 BOARD_to_LEFT = 0x8083E3E3E0808ULL;
  private const uint64 BOARD_to_UP = 0x3838FE3838ULL;
  private const uint64 BOARD_to_DOWN = 0x3838FE38380000ULL;

  private int Nsolution;
  private int nstack;
  private int TOT;

  private int64 start_time;
  uint64 *hash_array;
  uint64 stack[64];
  private static inline int BITScanForward (uint64 bb)
  {
    //  @author Matt Taylor (2003)
    const int[64]
      lsb_64_table =
      { 63, 30, 3, 32, 59, 14, 11, 33, 60, 24, 50, 9, 55, 19, 21, 34, 61, 29,
      2, 53, 51, 23, 41, 18, 56, 28, 1, 43, 46, 27, 0, 35, 62, 31, 58, 4, 5,
      49, 54, 6,
      15, 52, 12, 40, 7, 42, 45, 16, 25, 57, 48, 13, 10, 39, 8, 44, 20, 47,
      38, 22, 17,
      37, 36, 26
    };

    bb ^= bb - 1;
    uint folded = (int) bb ^ (bb >> 32);
    return lsb_64_table[folded * 0x78291ACF >> 26];
  }

  private
    const uint64 POW2[64] =
    { 0x1ULL, 0x2ULL, 0x4ULL, 0x8ULL, 0x10ULL, 0x20ULL,
    0x40ULL, 0x80ULL, 0x100ULL, 0x200ULL, 0x400ULL, 0x800ULL, 0x1000ULL,
    0x2000ULL, 0x4000ULL, 0x8000ULL, 0x10000ULL, 0x20000ULL, 0x40000ULL,
    0x80000ULL, 0x100000ULL, 0x200000ULL, 0x400000ULL, 0x800000ULL,
    0x1000000ULL, 0x2000000ULL, 0x4000000ULL, 0x8000000ULL, 0x10000000ULL,
    0x20000000ULL, 0x40000000ULL, 0x80000000ULL, 0x100000000ULL,
    0x200000000ULL, 0x400000000ULL, 0x800000000ULL, 0x1000000000ULL,
    0x2000000000ULL, 0x4000000000ULL, 0x8000000000ULL, 0x10000000000ULL,
    0x20000000000ULL, 0x40000000000ULL, 0x80000000000ULL,
    0x100000000000ULL, 0x200000000000ULL, 0x400000000000ULL,
    0x800000000000ULL, 0x1000000000000ULL, 0x2000000000000ULL,
    0x4000000000000ULL, 0x8000000000000ULL, 0x10000000000000ULL,
    0x20000000000000ULL, 0x40000000000000ULL, 0x80000000000000ULL,
    0x100000000000000ULL, 0x200000000000000ULL, 0x400000000000000ULL,
    0x800000000000000ULL, 0x1000000000000000ULL, 0x2000000000000000ULL,
    0x4000000000000000ULL, 0x8000000000000000ULL
  };

  void undomove ()
  {
    nstack--;
    assert (nstack >= 0);
    board = stack[nstack];
  }

  void makemove (uint64 from, uint64 to, uint64 capture)
  {
    stack[nstack] = board;
    board &= ~from;
    board |= to;
    board &= ~capture;

    assert (nstack < TOT);
    nstack++;
  }

  int popCount (uint64 x)
  {
    int count = 0;
    while (x != 0)
      {
	count++;
	x &= x - 1;
      }
    return count;
  }


  int64 get_ms ()
  {
    int64 msec = GLib.get_real_time () / 1000;

    return msec;
  }

  void print (uint64 board)
  {
    for (int k = 64; k >= 1; k--)
      {
	if ((board & POW2[k - 1]) == 0)
	  {
	    if ((BOARD & POW2[k - 1]) == 0)
	      stdout.printf (" \t");
	    else
	      stdout.printf ("-\t");
	  }
	else
	  stdout.printf ("O\t");
	if ((k - 1) % 8 == 0)
	  stdout.printf ("\n");
      }
  }

  void print_stack ()
  {
    int64 time = get_ms () - start_time;
    Nsolution++;
    stdout.printf
      ("\nSolution# %d ms: %lld ----------------- start stack moves ------------------------  \n",
       Nsolution, time);
    for (int i = 0; i < nstack; i++)
      {
	print (stack[i]);
      }
    print (board);
    stdout.printf ("Nmoves: %llu Hash cut: %llu (%d%%) ", nmoves, cut,
		   cut * 100 / nmoves);
    stdout.printf
      ("\nSolution# %d ms: %lld ----------------- end stack moves ------------------------  \n",
       Nsolution, time);


  }

  void gen ()
  {

    uint64 from, bits, capture;

    if (((++nmoves) % 5000000000) == 0)
      {
	stdout.printf ("Nmoves: %llu Cut:%llu (%d%%) ", nmoves, cut,
		       cut * 100 / nmoves);

      }
    if (hash_array[board % HASH_SIZE] == board)
      {
	cut++;
	return;
      }

    if (nstack == TOT - 1)
      {
	print_stack ();
	return;
      }

    bits = board & BOARD_to_RIGHT;
    while (bits != 0)
      {
	from = POW2[BITScanForward (bits)];

	capture = from >> 1;
	if ((board & capture) != 0)
	  {
	    uint64 to = from >> 2;
	    if ((board & to) == 0)
	      {
		makemove (from, to, capture);
		gen ();
		undomove ();
	      }
	  };
	bits &= ~from;
      }

    bits = board & BOARD_to_UP;
    while (bits != 0)
      {
	from = POW2[BITScanForward (bits)];
	capture = from << 8;
	if ((board & capture) != 0)
	  {
	    uint64 to = from << 16;
	    if ((board & to) == 0)
	      {
		makemove (from, to, capture);
		gen ();
		undomove ();
	      }
	  };
	bits &= ~from;
      };

    bits = board & BOARD_to_DOWN;
    while (bits != 0)
      {
	from = POW2[BITScanForward (bits)];
	capture = from >> 8;
	if ((board & capture) != 0)
	  {
	    uint64 to = from >> 16;
	    if ((board & to) == 0)
	      {
		makemove (from, to, capture);
		gen ();
		undomove ();
	      }
	  }
	bits &= ~from;
      };

    bits = board & BOARD_to_LEFT;
    while (bits != 0)
      {
	from = POW2[BITScanForward (bits)];
	capture = from << 1;
	if ((board & capture) != 0)
	  {
	    uint64 to = from << 2;
	    if ((board & to) == 0)
	      {
		makemove (from, to, capture);
		gen ();
		undomove ();
	      }
	  }
	bits &= ~from;
      };

    hash_array[board % HASH_SIZE] = board;

  }

  void run ()
  {
    board = INIT_BOARD;
    nmoves = 0;
    nstack = 0;
    cut = 0;
    Nsolution = 0;
    TOT = popCount (board);
    hash_array = (uint64 *) malloc (HASH_SIZE * sizeof (uint64));

    print (board);
    start_time = get_ms ();

    gen ();
  }

  public static int main (string[]args)
  {
    var sample = new ChineseCheckers ();
    sample.run ();

    return 0;
  }

}
