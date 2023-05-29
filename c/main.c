/*
    CHINESE CHECKERS - find all solutions - c source

	run: gcc -O3 main.c -o checkers;./checkers

 	 	O	O	O
 	 	O	O	O
O	O	O	O	O	O	O
O	O	O	-	O	O	O
O	O	O	O	O	O	O
 	 	O	O	O
 	 	O	O	O
 */

#include "stdio.h"
#include "stdlib.h"
#include <assert.h>
#include <sys/timeb.h>

typedef long long unsigned u64;

u64 board, nmoves, cut;

#define HASH_SIZE        1229498

u64 TERRAIN;

int Nsolution, nstack, TOT;
long start_time;
u64 *hash_array;
u64 stack[64];

#if __WORDSIZE == 64

static inline int BITScanForward(u64 bits) {
    return __builtin_ffsll(bits) - 1;
}

#else
static inline int BITScanForward(u64 bits) {
        return ((unsigned) bits) ? __builtin_ffs(bits) - 1 : __builtin_ffs(bits >> 32) + 31;
    }

#endif

const u64 POW2[64] = {0x1ULL, 0x2ULL, 0x4ULL, 0x8ULL, 0x10ULL, 0x20ULL,
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
                      0x4000000000000000ULL, 0x8000000000000000ULL};

void undomove() {
    nstack--;
    board = stack[nstack];
}

void makemove(const u64 from, const u64 to, const u64 capture) {
    stack[nstack] = board;
    board &= ~from;
    board |= to;
    board &= ~capture;
    nstack++;
}

int popCount(u64 x) {
    int count = 0;
    while (x) {
        count++;
        x &= x - 1;
    }
    return count;
}

long get_ms() {
    struct timeb timebuffer;
    ftime(&timebuffer);
    return (timebuffer.time * 1000) + timebuffer.millitm;
}

void print(u64 board) {
    int k;
    for (k = 64; k >= 1; k--) {
        if ((board & POW2[k - 1]) == 0) {
            if ((TERRAIN & POW2[k - 1]) == 0)
                printf(" \t");
            else
                printf("-\t");
        } else
            printf("O\t");
        if ((k - 1) % 8 == 0)
            printf("\n");
    }
}

void print_stack() {
    long time = get_ms() - start_time;
    Nsolution++;
    printf("\nSolution# %d ms: %ld ----------------- start stack moves ------------------------  \n",
           Nsolution, time);
    int i;
    for (i = 0; i < nstack; i++) {
        print(stack[i]);
    }
    print(board);
    printf("\nstack solution: %d | ", Nsolution);
    for (i = 0; i < nstack; i++) {
        printf("0x%llxL, ", stack[i]);
    }

    printf("Nmoves: %llu Hash cut: %llu (%llu%%) ", nmoves, cut, cut * 100 / nmoves);
    printf("\nSolution# %d ms: %ld ----------------- end stack moves ------------------------  \n",
           Nsolution, time);

}

void gen() {

    u64 from, bits, to, capture;

    if (!((++nmoves) % 5000000000)) {
        printf("Nmoves: %llu Cut:%llu (%llu%%) ", nmoves, cut, cut * 100 / nmoves);
    }

    if (hash_array[board % HASH_SIZE] == board) {
        cut++;
        return;
    }

    if (nstack == TOT - 1) {
        print_stack();
        return;
    }

    bits = board & 0xfcfcfcfcfcfcfcfcULL;
    while (bits) {
        from = POW2[BITScanForward(bits)];

        capture = from >> 1;
        if (board & capture && !(board & (to = (from >> 2))) && to & TERRAIN) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    }

    bits = board & 0xffffffffffffULL;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from << 8;
        if (board & capture && !(board & (to = (from << 16))) && to & TERRAIN) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    }

    bits = board & 0xffffffffffff0000ULL;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from >> 8;
        if (board & capture && !(board & (to = (from >> 16))) && to & TERRAIN) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    }

    bits = board & 0x3f3f3f3f3f3f3f3fULL;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from << 1;
        if (board & capture && !(board & (to = (from << 2))) && to & TERRAIN) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    }

    hash_array[board % HASH_SIZE] = board;
}

int main(int argc, char *argv[]) {

    nmoves = nstack = cut = Nsolution = 0;

    hash_array = (u64 *) calloc(HASH_SIZE, sizeof(u64));
    board = 0x3838FEEEFE3838ULL;
    TERRAIN = 0x3838FEFEFE3838ULL;
//    board = 0x3cffdcd7fd2400ULL;
//    TERRAIN = 0xffffffffff3e14ULL;
    TOT = popCount(board);
    start_time = get_ms();
    print(board);
    gen();
    free(hash_array);
    return 0;
}
