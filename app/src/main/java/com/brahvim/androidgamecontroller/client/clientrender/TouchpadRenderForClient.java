package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.TouchpadRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.configs.TouchpadConfig;

import processing.core.PVector;

public class TouchpadRenderForClient extends TouchpadRendererBase implements ClientRenderer {
    //private int pTouchCount; // No stupid optimizations.
    private TrackingPolicy trackingPolicy = TrackingPolicy.FIRST;

    // TODO: Make a variation of a touchpad allowing the PC screen to act like a touch screen,
    //  and not just a mouse-only touchpad! (Sadly, this is where screencasting makes sense!)

    // Determines which touch this touchpad tracks when there are multiple moving on it!
    // Might be a redundant feature!
    public enum TrackingPolicy {
        FIRST(), // The touch that touched the touchpad before the others is tracked.
        LAST(), // The latest touch that touched the touchpad is tracked.
        MID(); // The midpoint of all touches is the position the touchpad tracks.
    }

    public TouchpadRenderForClient(TouchpadConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);
    }

    private void sendStateIfChanged() {
        // TODO: Make the touchpad respond to two-finger taps as a right-click, otherwise, let it
        //  respond only to single-taps.
        // ^^^ The tab is necessary in comments that 'continue'!

        System.out.println("A touchpad's state changed, sending it over...");

        //if (super.state.pressed != super.state.ppressed)
        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //super.state.ppressed? "" : "not ",
        //super.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    private void recordTouch() {
        int listSize = Sketch.listOfUnprojectedTouches.size();
        if (listSize == 0)
            return;

        switch (this.trackingPolicy) {
            case FIRST:
                super.state.mouse.set(Sketch.listOfUnprojectedTouches.get(0));
                break;

            case LAST:
                super.state.mouse.set(Sketch.listOfUnprojectedTouches.get(listSize - 1));
                break;

            case MID:
                PVector mid = new PVector();

                for (int i = 0; i < listSize; i++) {
                    mid.add(Sketch.listOfUnprojectedTouches.get(i));
                }

                mid.div(listSize);
                break;

            default:
        }
    }

    // region Touch events.
    public void touchStarted() {
        // Get current state:
        this.recordTouch();

        // If changes took place, send 'em over! ":D
        this.sendStateIfChanged();
    }

    public void touchMoved() {
        this.recordTouch();
        this.sendStateIfChanged();
    }

    public void touchEnded() {
        this.sendStateIfChanged();
    }
    // endregion

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
