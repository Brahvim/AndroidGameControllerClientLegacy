package com.brahvim.androidgamecontroller.client.render;

import androidx.annotation.NonNull;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.MainActivity;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.ButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;

import processing.core.PGraphics;
import processing.core.PVector;

public class ButtonRenderer extends ButtonRendererBase {
    public ButtonRenderer(ButtonConfig p_config) {
        super(p_config);
    }

    @Override
    public void draw(@NonNull PGraphics p_graphics) {
        this.recordTouch();
        super.draw(p_graphics);
    }

    public void touchStarted() {
        this.sendState();
    }

    public void touchMoved() {
    }

    public void touchReleased() {
        this.sendState();
    }

    private void sendState() {
        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    private void recordTouch() {
        this.state.pressed = false;

        for (PVector v : Sketch.listUnprojectedTouches) {
            switch (super.config.shape) {
                case ROUND -> super.state.pressed = CollisionAlgorithms
                  .ptCircle(v, super.config.transform, super.config.scale.x);

                case RECTANGLE -> {
                    PVector transform = super.config.transform,
                      scale = super.config.scale;

                    super.state.pressed = CollisionAlgorithms
                      .ptRect(v.x, v.y,
                        transform.x - (scale.x * 0.5f),
                        transform.y - (scale.y * 0.5f),
                        transform.x + (scale.x * 0.5f),
                        transform.y + (scale.y * 0.5f));
                }
            }

            if (this.state.pressed)
                break;
        }

        this.state.pressed &= MainActivity.sketch.mousePressed;
    }

} // End of class.
