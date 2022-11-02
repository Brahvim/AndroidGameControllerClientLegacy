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
    PGraphics gr = p_graphics;

    gr.pushMatrix();
    gr.pushStyle();

    gr.translate(this.config.transform.x,
      this.config.transform.y);
    gr.scale(this.config.scale.x, this.config.scale.x);
    gr.rotate(this.config.transform.z);

    gr.fill(230, this.state.pressed? 100 : 50);
    gr.noStroke();

    switch (this.config.shape) {
      case ROUND:
        gr.ellipse(0, 0, 1, 1);
        gr.textSize(0.4f);
        //gr.textAlign(CENTER, CENTER);
        gr.text("Y", 0, 0);
        break;

      case RECTANGLE:
        //gr.rectMode(CENTER);
        gr.rect(0, 0, 1.2f, 0.55f,
          0.1f, 0.1f, 0.1f, 0.1f);
        gr.textSize(0.4f);
        //gr.textAlign(CENTER, CENTER);
        gr.text("L", 0, 0);
        break;

      default:
        break;
    }

    gr.popMatrix();
    gr.popStyle();
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
