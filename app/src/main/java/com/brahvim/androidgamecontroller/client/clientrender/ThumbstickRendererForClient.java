package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.ThumbstickRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.configs.ThumbstickConfig;
import com.brahvim.androidgamecontroller.serial.states.ThumbstickState;

import processing.core.PVector;
import processing.event.TouchEvent;

public class ThumbstickRendererForClient extends ThumbstickRendererBase implements ClientRenderer {
    public ThumbstickRendererForClient(ThumbstickConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);
    }

    // No `draw()` - it is inherited, I guess.

    // region Inherited from `ClientRenderer`
    @Override
    public ThumbstickConfig getConfig() {
        return super.config;
    }

    @Override
    public ThumbstickState getState() {
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
    public PVector getPosition() {
        return super.config.transform;
    }

    @Override
    public void addToPosition(PVector p_posAddent) {
        super.config.transform.add(p_posAddent);
    }

    @Override
    public void addToPosition(float p_x, float p_y) {
        super.config.transform.add(p_x, p_y);
    }

    @Override
    public void setScale(PVector p_pos) {
        super.config.scale.set(p_pos);
    }

    @Override
    public void setScale(float p_x, float p_y) {
        super.config.scale.set(p_x, p_y);
    }

    @Override
    public PVector getScale() {
        return super.config.scale;
    }

    @Override
    public void addToScale(PVector p_posAddent) {
        super.config.scale.add(p_posAddent);
    }

    @Override
    public void addToScale(float p_x, float p_y) {
        super.config.scale.add(p_x, p_y);
    }

    @Override
    public void recordTouch() {
        boolean measureOnlyDir = false;

        for (PVector v : Sketch.listOfUnprojectedTouches) {
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
    // endregion

    private void sendStateIfChanged() {
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

    // region Utlities for this class.
    // Not using `PVector.equals()` since the `z` may not always match.
    // ...what if you compare it with the touches from the previous frame,
    // ..before which you changed the camera or projection matrices? ;)
    public boolean isDraggingTouch(PVector p_vector) {
        return this.draggingTouch.x == p_vector.x
          && this.draggingTouch.y == p_vector.y;
    }

    public boolean isDraggingTouch(TouchEvent.Pointer p_pointer) {
        return this.draggingTouch.x == p_pointer.x
          && this.draggingTouch.y == p_pointer.y;
    }
    // endregion

    // region Touch events.
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

}
