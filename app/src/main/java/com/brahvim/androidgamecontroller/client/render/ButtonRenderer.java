package com.brahvim.androidgamecontroller.client.render;

import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.state.ButtonState;

import processing.core.PGraphics;

public class ButtonRenderer {
    private ButtonConfig config;
    private ButtonState state;

    ButtonRenderer(ButtonConfig p_config) {
        this.config = p_config;
    }

    public void draw(PGraphics p_graphics) {
        p_graphics.pushMatrix();
        p_graphics.pushStyle();

        p_graphics.translate(this.config.transform.x,
          this.config.transform.y);
        p_graphics.scale(this.config.scale.x, this.config.scale.x);
        p_graphics.rotate(this.config.transform.z);

        p_graphics.fill(230, this.state.pressed? 100 : 50);
        p_graphics.noStroke();

        switch (this.config.shape) {
            case ROUND:
                p_graphics.ellipse(0, 0, 1, 1);
                p_graphics.textSize(0.4f);
                //gr.textAlign(CENTER, CENTER);
                p_graphics.text("Y", 0, 0);
                break;

            case RECTANGLE:
                //gr.rectMode(CENTER);
                p_graphics.rect(0, 0, 1.2f, 0.55f,
                  0.1f, 0.1f, 0.1f, 0.1f);
                p_graphics.textSize(0.4f);
                //gr.textAlign(CENTER, CENTER);
                p_graphics.text("L", 0, 0);
                break;

            default:
                break;
        }

        p_graphics.popMatrix();
        p_graphics.popStyle();
    }

    public void mousePressed() {
    }

    public void mouseReleased() {
    }

    public boolean wasPressed() {
        return this.state.ppressed;
    }

    public boolean isPressed() {
        return this.state.pressed;
    }
}
