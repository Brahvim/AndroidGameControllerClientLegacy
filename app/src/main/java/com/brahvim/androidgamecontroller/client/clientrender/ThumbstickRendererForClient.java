package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.ThumbstickRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.ThumbstickConfig;

import processing.core.PVector;

public class ThumbstickRendererForClient extends ThumbstickRendererBase implements ClientRenderer {
    public ThumbstickRendererForClient(ThumbstickConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);
    }

    // region Touch event callbacks.
    @Override
    public void touchStarted() {
        this.recordTouch();
        this.sendStateIfChanged();
    }

    @Override
    public void touchMoved() {
        this.recordTouch();
        this.sendStateIfChanged();
    }

    @Override
    public void touchEnded() {
        this.recordTouch();
        this.sendStateIfChanged();
    }
    // endregion

    private void recordTouch() {
        boolean measureOnlyDir = false;

        for (PVector v : Sketch.listUnprojectedTouches) {
            if (CollisionAlgorithms.ptCircle(
              v, super.config.transform,
              // Whadda' I do then? Take an average? No! Please! No Ellipses!
              super.config.scale.x)) {
                if (!measureOnlyDir) {
                    super.state.pressed = true;

                    synchronized (super.draggingTouch) {
                        super.draggingTouch.set(v);
                    }

                    super.state.mag = v.magSq();
                }

                super.state.dir = v.heading();
                measureOnlyDir = true;
            }
        }
    }

    private void sendStateIfChanged() {
        this.state.controlNumber = this.config.controlNumber;

        // If the state didn't change, let's go back!:
        if (this.state.ppressed == this.state.pressed)
            return;

        System.out.printf(
          "Magnitude: `%.7f`, Angle: `%.7f` Thumbstick's state changed, sending it over...\n",
          this.state.mag, this.state.dir);
        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //this.state.ppressed? "" : "not ",
        //this.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(this.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

}
