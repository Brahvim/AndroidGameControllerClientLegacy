package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.TouchpadRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.TouchpadConfig;

import processing.core.PVector;

public class TouchpadRenderForClient extends TouchpadRendererBase implements ClientRenderer {
    TouchpadRenderForClient(TouchpadConfig p_config) {
        super(p_config);
    }

    public void touchStarted() {
        super.state.ppressed = super.state.pressed;
        this.recordTouch();
        this.sendStateIfChanged();
    }

    public void touchMoved() {
        // Record previous state:
        super.state.ppressed = super.state.pressed;

        // Get current state:
        this.recordTouch();

        // If changes took place, send 'em over! ":D
        this.sendStateIfChanged();
    }

    public void touchEnded() {
        super.state.ppressed = super.state.pressed;
        this.recordTouch();
        this.sendStateIfChanged();
    }

    private void sendStateIfChanged() {
        super.state.controlNumber = super.config.controlNumber;

        // If the state didn't change, let's go back!:
        if (super.state.ppressed == super.state.pressed)
            return;

        System.out.printf("It was previously %s pressed and is now %spressed.\n",
          super.state.ppressed? "" : "not ",
          super.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    private void recordTouch() {
        super.state.pressed = false;

        for (PVector v : Sketch.listUnprojectedTouches) {
            PVector transform = super.config.transform,
              scale = super.config.scale;

            super.state.pressed = CollisionAlgorithms
              .ptRect(v.x, v.y,
                transform.x - (scale.x * 0.5f),
                transform.y - (scale.y * 0.5f),
                transform.x + (scale.x * 0.5f),
                transform.y + (scale.y * 0.5f));

            if (super.state.pressed)
                break;
        }
    }

}
