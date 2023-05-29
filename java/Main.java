public class Main {

    long TERRAIN;
    final long HASH_SIZE;
    long board;
    long nmoves;
    long cut;

    int Nsolution;
    int nstack;
    final int TOT;
    final long start_time;
    final long[] hash_array;
    final long[] stack;

    static public int BITScanForward(long b) {
        if (b < 0) System.exit(1);//TODO
        double x = (double) (b & -b);
        int exp = (int) (Double.doubleToLongBits(x) >>> 52);
        return (exp & 2047) - 1023;
    }

    void undomove() {
        nstack--;
        board = stack[nstack];
    }

    void makemove(long from, long to, long capture) {
        stack[nstack] = board;
        board &= ~from;
        board |= to;
        board &= ~capture;
        nstack++;
    }

    int popCount(long x) {
        int count = 0;
        while (x != 0) {
            count++;
            x &= x - 1;
        }
        return count;
    }

    static long POW2(int k) {
        if (k == 63)
            return 0;
        return (long) Math.pow(2, k);
    }

    long get_ms() {
        return System.currentTimeMillis();
    }

    void print(long board) {
        System.out.println("count: " + popCount(board) + " nstack: " + nstack);
        int k;
        for (k = 64; k >= 1; k--) {
            if ((board & POW2(k - 1)) == 0) {
                if ((TERRAIN & POW2(k - 1)) == 0)
                    System.out.print(" \t");
                else
                    System.out.print("-\t");
            } else
                System.out.print("O\t");
            if ((k - 1) % 8 == 0)
                System.out.print("\n");
        }
    }

    void print_stack() {
        long time = get_ms() - start_time;
        Nsolution++;
        System.out.printf("\nSolution# %d ms: %d ----------------- start stack moves ------------------------  \n",
                Nsolution, time);
        int i;
        for (i = 0; i < nstack; i++) {
            print(stack[i]);
        }
        print(board);
        System.out.printf("\nstack solution: %d | ", Nsolution);
        for (i = 0; i < nstack; i++) {
            System.out.print("0x" + Long.toHexString(stack[i]) + ", ");
        }

        System.out.printf("Nmoves: %d Hash cut: %d (%d) ", nmoves, cut, cut * 100 / nmoves);
        System.out.printf("\nSolution# %d ms: %d ----------------- end stack moves ------------------------  \n",
                Nsolution, time);

    }

    void gen() {

        long from, bits, to, capture;

        if (((++nmoves) % 5000000000L) == 0) {
            System.out.printf("Nmoves: %dlu Cut:%dlu (%dlu%%) ", nmoves, cut, cut * 100 / nmoves);
        }
        if (hash_array[(int) (board % HASH_SIZE)] == board) {
            cut++;
            return;
        }

        if (nstack == TOT - 1) {
            print_stack();
            //System.exit(0);
            return;
        }

        bits = board & 0xfcfcfcfcfcfcfcfcl;
        while (bits != 0) {
            from = POW2(BITScanForward(bits));
            capture = from >>> 1;
            if ((board & capture) != 0 && 0 == (board & (to = (from >> 2))) && 0 != (to & TERRAIN)) {
                makemove(from, to, capture);
                gen();
                undomove();
            }
            bits &= ~from;
        }

        bits = board & 0xffffffffffffL;
        while (bits != 0) {
            from = POW2(BITScanForward(bits));
            capture = from << 8;
            if ((board & capture) != 0 && 0 == (board & (to = (from << 16))) && (to & TERRAIN) != 0) {
                makemove(from, to, capture);
                gen();
                undomove();
            }
            bits &= ~from;
        }
        bits = board & 0xffffffffffff0000L;
        while (bits != 0) {
            from = POW2(BITScanForward(bits));
            capture = from >>> 8;
            if ((board & capture) != 0 && 0 == (board & (to = (from >> 16))) && (to & TERRAIN) != 0) {
                makemove(from, to, capture);
                gen();
                undomove();
            }
            bits &= ~from;
        }
        bits = board & 0x3f3f3f3f3f3f3f3fL;
        while (bits != 0) {
            from = POW2(BITScanForward(bits));
            capture = from << 1;
            if ((board & capture) != 0 && 0 == (board & (to = (from << 2))) && (to & TERRAIN) != 0) {
                makemove(from, to, capture);
                gen();
                undomove();
            }
            bits &= ~from;
        }
        hash_array[(int) (board % HASH_SIZE)] = board;
    }

    public Main(long INIT_BOARD, long terrain, int HASH_SIZE) {
        this.HASH_SIZE = HASH_SIZE;
        hash_array = new long[HASH_SIZE];
        stack = new long[64];
        this.TERRAIN = terrain;
        board = INIT_BOARD;

        TOT = popCount(board);
        print(board);
        start_time = get_ms();
        gen();
    }

    public static void main(String[] args) {
//        new Main(0x3cffdcd7fd2400L, 0xffffffffff3e14L, 1229498);
        new Main(0x3838FEEEFE3838l, 0x3838FEFEFE3838l, 1229498);
    }
}
