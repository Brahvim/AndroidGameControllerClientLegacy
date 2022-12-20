package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.MainActivity;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.ButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.configs.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.states.ButtonState;

import processing.core.PVector;

public class ButtonRendererForClient extends ButtonRendererBase implements ClientRenderer {
    private PVector colStart, colEnd; // Collision info, cached.

    // TODO: make a class specific to only round buttons!
    //  Their configuration information could then have just the transform and radius...

    public ButtonRendererForClient(ButtonConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);

        PVector transform = super.config.transform,
          scale = super.config.scale;

        this.colEnd = new PVector(
          transform.x + (scale.x * 0.5f),
          transform.y + (scale.y * 0.5f));

        this.colStart = new PVector(
          transform.x - (scale.x * 0.5f),
          transform.y - (scale.y * 0.5f));
    }

    // No `draw()` - it is inherited.

    @Override
    public ButtonConfig getConfig() {
        return super.config;
    }

    @Override
    public ButtonState getState() {
        return super.state;
    }

    @Override
    public void setPosition(PVector p_pos) {
        super.config.transform.set(p_pos);
    }

    @Override
    public void setPosition(float p_x, float p_y) {
        super.config.transform.set(p_x, p_y);
    }

    @Override
    public void setScale(PVector p_pos) {
        super.config.scale.set(p_pos);
    }

    @Override
    public void setScale(float p_x, float p_y) {
        super.config.scale.set(p_x, p_y);
    }

    public void recordTouch() {
        this.state.pressed = false;

        for (PVector v : Sketch.listOfUnprojectedTouches) {
            switch (super.config.shape) {
                case ROUND -> super.state.pressed = CollisionAlgorithms
                  .ptCircle(v, super.config.transform, super.config.scale.x);

                case RECTANGLE -> super.state.pressed = CollisionAlgorithms
                  .ptRect(v, this.colStart, this.colEnd);
            }

            // Break out! We don't want other failing tests to affect this!:
            if (super.state.pressed)
                break;
        }

        super.state.pressed &= MainActivity.sketch.mousePressed;
    }

    private void sendStateIfChanged() {
        // If the state didn't change, let's go back!:
        if (super.state.ppressed == super.state.pressed)
            return;

        System.out.printf("Button `%s`'s state changed, sending it over...\n", super.config.text);
        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //super.state.ppressed? "" : "not ",
        //super.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    // region Touch events.
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
    // endregion

} // End of class.
