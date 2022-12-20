package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.MainActivity;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.DpadButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.configs.DpadButtonConfig;
import com.brahvim.androidgamecontroller.serial.states.DpadButtonState;

import org.jetbrains.annotations.NotNull;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class DpadButtonRendererForClient extends DpadButtonRendererBase implements ClientRenderer {
    CollisionCheckFunction colFxn;
    // OOP *does* aid optimization! It's not all about speed in wrapped classes!

    private interface CollisionCheckFunction {
        boolean check(PVector p_touch, PVector p_scale, PVector p_transform);
    }

    public DpadButtonRendererForClient(DpadButtonConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);

        // Select a `colFxn`:
        switch (super.config.dir) {
            case UP:
                this.colFxn = new CollisionCheckFunction() {
                    @Override
                    public boolean check(PVector p_touch, PVector p_scale, PVector p_transform) {
                        return CollisionAlgorithms
                          .ptRect(p_touch.x, p_touch.y,
                            p_transform.x - (p_scale.x * 0.75f),
                            p_transform.y - p_scale.y,
                            p_transform.x + (p_scale.x * 0.5f),
                            p_transform.y + (p_scale.y * 0.5f));
                    }
                };
                break;

            case LEFT:
                this.colFxn = new CollisionCheckFunction() {
                    @Override
                    public boolean check(PVector p_touch, PVector p_scale, PVector p_transform) {
                        return CollisionAlgorithms
                          .ptRect(p_touch.x, p_touch.y,
                            p_transform.x - (p_scale.x * 1.25f),
                            p_transform.y - (p_scale.y * 0.75f),
                            p_transform.x + (p_scale.x * 0.3825f),
                            p_transform.y + (p_scale.y * 0.75f));
                    }
                };
                break;

            case DOWN:
                this.colFxn = new CollisionCheckFunction() {
                    @Override
                    public boolean check(PVector p_touch, PVector p_scale, PVector p_transform) {
                        return CollisionAlgorithms
                          .ptRect(p_touch.x, p_touch.y,
                            p_transform.x - (p_scale.x * 0.75f),
                            p_transform.y - (p_scale.y * 0.5f),
                            p_transform.x + (p_scale.x * 0.5f),
                            p_transform.y + p_scale.y);
                    }
                };
                break;

            case RIGHT:
                this.colFxn = new CollisionCheckFunction() {
                    @Override
                    public boolean check(PVector p_touch, PVector p_scale, PVector p_transform) {
                        return CollisionAlgorithms
                          .ptRect(p_touch.x, p_touch.y,
                            p_transform.x - (p_scale.x * 0.75f),
                            p_transform.y - (p_scale.y * 0.75f),
                            p_transform.x + p_scale.x,
                            p_transform.y + (p_scale.y * 0.75f));
                    }
                };
                break;
        }

    }

    public void draw(@NotNull PGraphics p_graphics) {
        // super.state.ppressed = super.state.pressed; // Nope! The impl. handles this!

        p_graphics.pushMatrix();
        p_graphics.pushStyle();

        p_graphics.translate(this.config.transform.x,
          this.config.transform.y);
        p_graphics.scale(this.config.scale.x, this.config.scale.x);

        switch (this.config.dir) {
            case DOWN:
                p_graphics.scale(-1);
                break;

            case LEFT:
                p_graphics.rotate(-PConstants.HALF_PI);
                break;

            case RIGHT:
                p_graphics.rotate(PConstants.HALF_PI);
                break;

            // No rotation. It's already pointing up, ":D!
            // case UP:
            // break;

            default:
                break;
        }

        p_graphics.fill(230, super.state.pressed? 100 : 50);
        p_graphics.noStroke();

        p_graphics.beginShape(PConstants.POLYGON);
        p_graphics.vertex(-0.5f, 0.35f);
        p_graphics.vertex(0.5f, 0.35f);
        p_graphics.vertex(0.5f, -0.35f);
        // p_graphics.edge(true);
        p_graphics.vertex(0, -0.85f);
        // p_graphics.edge(false);
        p_graphics.vertex(-0.5f, -0.35f);
        p_graphics.endShape(PConstants.CLOSE);

        p_graphics.popMatrix();
        p_graphics.popStyle();
    }

    // region Inherited from `ClientRenderer`
    @Override
    public DpadButtonConfig getConfig() {
        return super.config;
    }

    @Override
    public DpadButtonState getState() {
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
        super.state.pressed = false;

        for (PVector v : Sketch.listOfUnprojectedTouches) {
            super.state.pressed = MainActivity.sketch.mousePressed &&
              this.colFxn.check(v, super.config.scale, super.config.transform);
            if (super.state.pressed)
                break;
        }
    }
    // endregion

    private void sendStateIfChanged() {
        // If the state didn't change, let's go back!:
        if (super.state.ppressed == super.state.pressed)
            return;

        //System.out.printf("`%s` DPAD's state changed, sending it over...\n",
        //this.config.dir.toString());

        //System.out.printf("It was previously %s pressed and is now %spressed.\n",
        //super.state.ppressed? "" : "not ",
        //super.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(super.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    // region Touch events.
    public void touchStarted() {
        // Record previous state:
        super.state.ppressed = super.state.pressed;

        // Get current state:
        this.recordTouch();

        // If changes took place, send 'em over! ":D
        this.sendStateIfChanged();
    }

    public void touchMoved() {
        super.state.ppressed = super.state.pressed;
        this.recordTouch();
        this.sendStateIfChanged();
    }

    public void touchEnded() {
        super.state.ppressed = super.state.pressed;
        this.recordTouch();
        this.sendStateIfChanged();
    }
    // endregion

}
