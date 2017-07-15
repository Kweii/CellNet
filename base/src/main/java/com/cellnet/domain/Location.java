package com.cellnet.domain;

/**
 * Created by gui on 2017/1/7.
 */
public class Location {
    private int x;

    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location setX(int x) {
        this.x = x;
        return this;
    }

    public Location setY(int y) {
        this.y = y;
        return this;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
