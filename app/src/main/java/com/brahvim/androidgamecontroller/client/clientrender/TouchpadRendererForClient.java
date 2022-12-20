package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.MainActivity;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.TouchpadRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.configs.TouchpadConfig;
import com.brahvim.androidgamecontroller.serial.states.TouchpadState;

import processing.core.PVector;

public class TouchpadRendererForClient extends TouchpadRendererBase implements ClientRenderer {
    private TouchpadConfig.TrackingPolicy trackingPolicy = TouchpadConfig.TrackingPolicy.FIRST;
    private PVector colStart, colEnd;
    private int lastTouchMillis;
    private final int DOUBLE_TAP_THRESHOLD = 5000;

    // TODO: Make a variation of a touchpad allowing the PC screen to act like a touch screen,
    //  and not just a mouse-only touchpad! (Sadly, this is where screencasting makes sense!)

    public TouchpadRendererForClient(TouchpadConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);

        PVector transform = super.config.transform,
          scale = super.config.scale;

        this.colEnd = new PVector(
          transform.x + (scale.x * 0.75f),
          transform.y + (scale.y * 0.25f));

        this.colStart = new PVector(
          transform.x - (scale.x * 0.625f),
          transform.y - (scale.y * 0.28125f));
    }

    // region Inherited from `ClientRenderer`
    @Override
    public TouchpadConfig getConfig() {
        return super.config;
    }

    @Override
    public TouchpadState getState() {
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
        super.state.ppressed = super.state.pressed;

        int listSize = Sketch.listOfUnprojectedTouches.size();
        if (listSize == 0) {
            super.state.pressed = false;
            return;
        }

        switch (this.trackingPolicy) {
            case FIRST -> super.state.mouse.set(Sketch.mouse);
            case LAST -> super.state.mouse.set(Sketch.listOfUnprojectedTouches.get(listSize - 1));
            case MID -> {
                if (listSize == 1) {
                    super.state.mouse.set(Sketch.mouse);
                    return;
                }
                PVector mid = super.state.mouse.set(0, 0, 0);
                for (int i = 0; i < listSize; i++) {
                    mid.add(Sketch.listOfUnprojectedTouches.get(i));
                }
                mid.div(listSize);
            }
            //default -> { }
        }

        super.state.pressed = CollisionAlgorithms.ptRect(
          super.state.mouse, this.colStart, this.colEnd) &&
          MainActivity.sketch.mousePressed;
    }
    // endregion

    private void sendState() {
        // TODO: Make the touchpad respond to two-finger taps as a right-click, otherwise, let it
        //  respond only to single-taps.
        // ^^^ The tab is necessary in comments that 'continue'!

        //System.out.println("A touchpad's state changed, sending it over...");

        //if (super.state.pressed != super.state.ppressed)
        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //super.state.ppressed? "" : "not ",
        //super.state.pressed? "" : "not ");

        Sketch.socket.send(
          ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    // region Touch events.
    public void touchStarted() {
        if (Sketch.listOfUnprojectedTouches.size() == 0)
            return;

        if (super.state.pressed) {
            this.lastTouchMillis = MainActivity.sketch.millis();
        }

        super.state.pdoubleTapped = super.state.doubleTapped;
        if (this.lastTouchMillis < MainActivity.sketch.millis() + this.DOUBLE_TAP_THRESHOLD)
            super.state.doubleTapped = true;
        else super.state.doubleTapped = false;

        // Get current state:
        this.recordTouch();

        // If changes took place, send 'em over! ":D
        this.sendState();
    }

    public void touchMoved() {
        this.recordTouch();
        this.sendState();
    }

    public void touchEnded() {
        this.recordTouch();
        this.sendState();
    }
    // endregion

    // Older implementation of methods:
    /*
    private void recordTouch() {
        // Record previous state:
        super.state.ppressed = super.state.pressed;
        this.pTouchCount = super.state.touches.size();

        super.state.touches.clear();

        // Touch state detection:

        boolean recordedState = false, isTouching;

        for (PVector v : Sketch.listOfUnprojectedTouches) {
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

            if (isTouching) {
                // Send relative coordinates:
                super.state.touches.add(PVector.sub(v, this.config.transform));
            }
        }

        super.state.pressed &= MainActivity.sketch.mousePressed;
    }

    private void sendStateIfChanged() {
        boolean touchesChanged =
          Sketch.plistOfUnprojectedTouches.size() != Sketch.listOfUnprojectedTouches.size();

        if (!touchesChanged) {
            int leastSize = Math.min(Sketch.plistOfUnprojectedTouches.size(),
              Sketch.listOfUnprojectedTouches.size()) - 1;

            if (leastSize > 0)
                for (int i = leastSize; i != 0; i--) {
                    if (!Sketch.listOfUnprojectedTouches.get(i)
                      .equals(Sketch.plistOfUnprojectedTouches.get(i))) {
                        touchesChanged = true;
                        return;
                    }
                }
        }

        // If the state didn't change, let's go back!:
        if (super.state.ppressed == super.state.pressed || !touchesChanged)
            return;

        System.out.println("A touchpad's state changed, sending it over...");

        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //super.state.ppressed? "" : "not ",
        //super.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    // region Touch events.
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
    // endregion
     */

}
