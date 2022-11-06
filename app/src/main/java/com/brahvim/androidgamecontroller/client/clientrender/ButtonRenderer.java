package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.MainActivity;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.ButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;

import processing.core.PVector;

public class ButtonRenderer extends ButtonRendererBase {
    public ButtonRenderer(ButtonConfig p_config) {
        super(p_config);
    }

    public void touchStarted() {
        this.recordTouch();
        this.sendState();
    }

    public void touchMoved() {
        super.state.ppressed = super.state.pressed;
        this.recordTouch();

        if (super.state.ppressed != super.state.pressed)
            this.sendState();
    }

    public void touchReleased() {
        this.recordTouch();
        this.sendState();
    }

    private void sendState() {
        super.state.configHash = super.config.hashCode();

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
