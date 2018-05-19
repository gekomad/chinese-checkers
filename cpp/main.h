#ifndef C_MAIN_H
#define C_MAIN_H

#include <iostream>       // std::cout
#include <thread>         // std::thread
#include <stdlib.h>

using namespace std;

typedef long long unsigned u64;

class ChineseCheckers {
    u64 board;
    u64 stack[64];
    int TOT, nstack = 0;

    void makemove(const u64 from, const u64 to, const u64 capture);

    void undomove();

    void print_stack();

public:
    void gen();

    thread start() {
        thread t = thread(&ChineseCheckers::gen, this);
        return t;
    }

public:
    ChineseCheckers(unsigned long long int i);
};

#define HASH_SIZE        1229498

// Bitboard Calculator: http://cinnamonchess.altervista.org/bitboard_calculator/Calc.html

#define INIT_BOARD       0x3838FEEEFE3838ULL
#define BOARD            0x3838FEFEFE3838ULL
#define BOARD_to_RIGHT   0x2020F8F8F82020ULL
#define BOARD_to_LEFT    0x8083E3E3E0808ULL
#define BOARD_to_UP      0x3838FE3838ULL
#define BOARD_to_DOWN    0x3838FE38380000ULL

int Nsolution;
long start_time;
u64 *hash_array;
u64 nmoves = 0, cut = 0;

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



#endif //C_MAIN_H
