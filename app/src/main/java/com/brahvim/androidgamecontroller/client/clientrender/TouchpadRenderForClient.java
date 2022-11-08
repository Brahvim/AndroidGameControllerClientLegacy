package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.MainActivity;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.TouchpadRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.TouchpadConfig;

import processing.core.PVector;

public class TouchpadRenderForClient extends TouchpadRendererBase implements ClientRenderer {
    int pTouchCount;

    public TouchpadRenderForClient(TouchpadConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);
    }

    public void touchStarted() {
        this.recordTouch();
        this.sendStateIfChanged();
    }

    public void touchMoved() {
        // Get current state:
        this.recordTouch();

        // If changes took place, send 'em over! ":D
        this.sendStateIfChanged();
    }

    public void touchEnded() {
        this.recordTouch();
        this.sendStateIfChanged();
    }

    private void recordTouch() {
        // Record previous state:
        super.state.ppressed = super.state.pressed;
        this.pTouchCount = this.state.touches.size();

        super.state.touches.clear();

        // Touch state detection:

        boolean recordedState = false, isTouching;

        for (PVector v : Sketch.listUnprojectedTouches) {
            PVector transform = super.config.transform,
              scale = super.config.scale;

            // Yeah, weird numbering here... kinda' cursed.
            isTouching =
              CollisionAlgorithms.ptRect(v.x, v.y,
                transform.x - (scale.x * 0.625f),
                transform.y - (scale.y * 0.28125f),
                transform.x + (scale.x * 0.75f),
                transform.y + (scale.y * 0.25f));

            if (!recordedState) {
                super.state.pressed = isTouching;
                recordedState = true;
            }
        }

        this.state.pressed &= MainActivity.sketch.mousePressed;
    }

    private void sendStateIfChanged() {
        super.state.controlNumber = super.config.controlNumber;

        // If the state didn't change, let's go back!:
        if (super.state.ppressed == super.state.pressed)
            //|| this.pTouchCount == super.state.touches.size())
            return;

        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //super.state.ppressed? "" : "not ",
        //super.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

}
