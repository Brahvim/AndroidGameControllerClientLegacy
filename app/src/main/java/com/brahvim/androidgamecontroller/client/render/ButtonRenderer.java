package com.brahvim.androidgamecontroller.client.render;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.ButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.state.ButtonState;

import processing.core.PVector;

public class ButtonRenderer extends ButtonRendererBase {
    private ButtonConfig config;
    private ButtonState state;

    public ButtonRenderer(ButtonConfig p_config) {
        super(p_config);
    }

    public void touchStarted() {
        Sketch.unprojectTouches();
        int lastTouchArrayId = Sketch.listUnprojectedTouches.size() - 1;

        if (lastTouchArrayId == -1)
            return;

        PVector touch = Sketch.listUnprojectedTouches.get(lastTouchArrayId);

        switch (this.config.shape) {
            case ROUND -> this.state.pressed = CollisionAlgorithms
              .ptCircle(touch, this.config.transform, this.config.scale.x);

            case RECTANGLE -> this.state.pressed = CollisionAlgorithms
              .ptRect(touch, this.config.transform, this.config.scale);
        }

        Sketch.socket.send(ByteSerial.encode(this.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    public void touchReleased() {
        this.state.pressed = false;
        //socket.send();
    }
}
