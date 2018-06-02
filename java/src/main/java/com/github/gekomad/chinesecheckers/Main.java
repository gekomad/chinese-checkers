package com.github.gekomad.chinesecheckers;

class Main {
    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("puzzle")) puzzle(Integer.valueOf(args[1]));
        else {
            long init = 0x3838FEEEFE3838L;
            long terrain = 0x3838FEFEFE3838L;
            ChineseCheckers cc = new ChineseCheckers(init, terrain);
            cc.gen();
        }
    }

    private static void puzzle(int pieces) {
        long terrain = 0x3838FEFEFE3838L;
        long init = new Puzzle(terrain, pieces).puzzle();
        System.out.println(init);
        ChineseCheckers cc = new ChineseCheckers(init, terrain);
        cc.setMaxSolutions(1);
        cc.gen();
    }
}
