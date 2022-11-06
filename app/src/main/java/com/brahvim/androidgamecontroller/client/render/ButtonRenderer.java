package com.brahvim.androidgamecontroller.client.render;

import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.state.ButtonState;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class ButtonRenderer {
    private ButtonConfig config;
    private ButtonState state;

    public ButtonRenderer(ButtonConfig p_config) {
        this.config = p_config;
        this.state = new ButtonState();
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
                break;

            case RECTANGLE:
                //p_graphics.rectMode(PConstants.CENTER);
                p_graphics.rect(0, 0, 1.2f, 0.55f,
                  0.1f, 0.1f, 0.1f, 0.1f);
                break;

            default:
                break;
        }

        p_graphics.textSize(0.4f);
        p_graphics.textAlign(PConstants.CENTER, PConstants.CENTER);
        p_graphics.text(this.config.text, 0, 0);

        p_graphics.popMatrix();
        p_graphics.popStyle();
    }

    public void touchStarted() {
        Sketch.unprojectTouches();

        PVector touch = Sketch.listUnprojectedTouches
          .get(Sketch.listUnprojectedTouches.size());

        switch (this.config.shape) {
            case ROUND -> this.state.pressed = CollisionAlgorithms
              .ptCircle(touch, this.config.transform, this.config.scale.x);

            case RECTANGLE -> this.state.pressed = CollisionAlgorithms
              .ptRect(touch, this.config.transform, this.config.scale);
        }

        //Sketch.socket.sendCode();
    }

    public void touchReleased() {
        this.state.pressed = false;
        //socket.send();
    }

    public boolean wasPressed() {
        return this.state.ppressed;
    }

    public boolean isPressed() {
        return this.state.pressed;
    }
}
