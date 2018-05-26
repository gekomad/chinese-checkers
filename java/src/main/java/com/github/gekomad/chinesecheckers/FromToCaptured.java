package com.github.gekomad.chinesecheckers;

class FromToCaptured{

    private long from,to,captured;

    public FromToCaptured(long captured, long from, long to) {
        this.from = from;
        this.to = to;
        this.captured = captured;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getCaptured() {
        return captured;
    }

    public void setCaptured(long captured) {
        this.captured = captured;
    }
}