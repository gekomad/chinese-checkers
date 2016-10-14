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

#define HASH_SIZE        122949823

// Bitboard Calculator: http://cinnamonchess.altervista.org/bitboard_calculator/Calc.html

#define INIT_BOARD       0x3838FEEEFE3838ULL
#define BOARD            0x3838FEFEFE3838ULL
#define BOARD_to_RIGHT   0x2020F8F8F82020ULL
#define BOARD_to_LEFT    0x8083E3E3E0808ULL
#define BOARD_to_UP      0x3838FE3838ULL
#define BOARD_to_DOWN    0x3838FE38380000ULL

int Nsolution, count, nstack, TOT;
long start_time;
u64 *hash_array;
u64 stack[64];
const int index64[64] = {
        0,  1, 48,  2, 57, 49, 28,  3,
        61, 58, 50, 42, 38, 29, 17,  4,
        62, 55, 59, 36, 53, 51, 43, 22,
        45, 39, 33, 30, 24, 18, 12,  5,
        63, 47, 56, 27, 60, 41, 37, 16,
        54, 35, 52, 21, 44, 32, 23, 11,
        46, 26, 40, 15, 34, 20, 31, 10,
        25, 14, 19,  9, 13,  8,  7,  6
};

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
//    assert(nstack >= 0);
    board = stack[nstack];
}

void makemove(const u64 from, const u64 to, const u64 capture) {
    stack[nstack] = board;
    board &= ~from;
    board |= to;
    board &= ~capture;

//    assert(nstack < TOT);
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

void print(u64 board) {return;
	int k;
    for (k = 64; k >= 1; k--) {
        if ((board & POW2[k - 1]) == 0) {
            if ((BOARD & POW2[k - 1]) == 0)
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
    printf("Nmoves: %llu Hash cut: %llu (%llu%%) ", nmoves, cut, cut * 100 / nmoves);
    printf("\nSolution# %d ms: %ld ----------------- end stack moves ------------------------  \n",
           Nsolution, time);
	if (Nsolution==30)exit(0);
    
}

void gen() {

    u64 from, bits, to, capture;
    int found = 0;
    if (!((++nmoves) % 5000000000)) {
        printf("Nmoves: %llu Cut:%llu (%llu%%) ", nmoves, cut, cut * 100 / nmoves);
       
    }

    if (hash_array[board % HASH_SIZE] == board) {
        cut++;
        return;
    }
    if (nstack >= count) {
        count = nstack;
        if (nstack == TOT - 1) {
            found = 1;
            print_stack();
        }
    }
    bits = board & BOARD_to_LEFT;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from << 1;
        if (board & capture && !(board & (to = from << 2))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };

    bits = board & BOARD_to_RIGHT;
    while (bits) {
        from = POW2[BITScanForward(bits)];

        capture = from >> 1;
        if (board & capture && !(board & (to = from >> 2))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };

    bits = board & BOARD_to_DOWN;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from >> 8;
        if (board & capture && !(board & (to = from >> 16))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };

    bits = board & BOARD_to_UP;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from << 8;
        if (board & capture && !(board & (to = from << 16))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };
    if (!found)
        hash_array[board % HASH_SIZE] = board;
}

int main(int argc, char *argv[]) {
    board = INIT_BOARD;
    nmoves = count = nstack = cut = Nsolution = 0;
    TOT = popCount(board);
    hash_array = (u64 *) calloc(HASH_SIZE, sizeof(u64));
    print(board);
    start_time = get_ms();
    gen();
	free(hash_array);
    return 0;
}
