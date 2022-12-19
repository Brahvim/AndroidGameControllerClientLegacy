package com.brahvim.androidgamecontroller.client;

import processing.core.PVector;

public class AgcRectangle {
    //public PVector center;
    public PVector start, end;

    public AgcRectangle(PVector p_start, PVector p_end) {
        this.end = p_end;
        this.start = p_start;
        //this.center = PVector.add(this.start, this.end).mult(0.5f);
    }

    public AgcRectangle(float p_startX, float p_startY, float p_endX, float p_endY) {
        this.end = new PVector(p_endX, p_endY);
        this.start = new PVector(p_startX, p_startY);
        //this.center = PVector.add(this.start, this.end).mult(0.5f);
    }

    public boolean contains(PVector p_point) {
        return CollisionAlgorithms.ptRect(
          p_point, this.start, this.end);
    }
}
