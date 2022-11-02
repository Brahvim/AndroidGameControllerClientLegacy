package com.brahvim.androidgamecontroller.client.render;

import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.state.ButtonState;

public class ButtonRenderer {
  private ButtonConfig config;
  private ButtonState state;

  ButtonRenderer(ButtonConfig p_config) {
    this.config = p_config;
  }

  public void draw() {
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
