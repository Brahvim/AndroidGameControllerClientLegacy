package com.brahvim.androidgamecontroller.client.clientrender;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.CollisionAlgorithms;
import com.brahvim.androidgamecontroller.client.Sketch;
import com.brahvim.androidgamecontroller.render.DpadButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.DpadButtonConfig;

import org.jetbrains.annotations.NotNull;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class DpadButtonRendererForClient extends DpadButtonRendererBase implements ClientRenderer {

    public DpadButtonRendererForClient(DpadButtonConfig p_config) {
        super(p_config);
        ClientRenderer.all.add(this);
    }

    public void touchStarted() {
        this.state.ppressed = this.state.pressed;
        this.recordTouch();
        this.sendStateIfChanged();
    }

    public void touchMoved() {
        // Record previous state:
        this.state.ppressed = this.state.pressed;

        // Get current state:
        this.recordTouch();

        // If changes took place, send 'em over! ":D
        this.sendStateIfChanged();
    }

    public void touchEnded() {
        this.state.ppressed = this.state.pressed;
        this.recordTouch();
        this.sendStateIfChanged();
    }

    private void sendStateIfChanged() {
        this.state.configHash = this.config.hashCode();

        // If the state didn't change, let's go back!:
        if (this.state.ppressed == this.state.pressed)
            return;

        System.out.printf("`%s` DPAD's state changed, sending it over...\n",
          this.config.dir.toString());
        System.out.printf("It was previously %s pressed and is now %spressed.\n",
          this.state.ppressed? "" : "not ",
          this.state.pressed? "" : "not ");

        Sketch.socket.send(ByteSerial.encode(this.state),
          Sketch.serverIp, RequestCode.SERVER_PORT);
    }

    private void recordTouch() {
        this.state.pressed = false;

        for (PVector v : Sketch.listUnprojectedTouches) {
            PVector transform = super.config.transform,
              scale = super.config.scale;

            super.state.pressed = CollisionAlgorithms
              .ptRect(v.x, v.y,
                transform.x - (scale.x * 0.5f),
                transform.y - (scale.y * 0.5f),
                transform.x + (scale.x * 0.5f),
                transform.y + (scale.y * 0.5f));

            if (this.state.pressed)
                break;
        }
    }

    public void draw(@NotNull PGraphics p_graphics) {
        // this.state.ppressed = this.state.pressed; // Nope! The impl. handles this!

        p_graphics.pushMatrix();
        p_graphics.pushStyle();

        p_graphics.translate(this.config.transform.x,
          this.config.transform.y);
        p_graphics.scale(this.config.scale.x, this.config.scale.x);

        switch (this.config.dir) {
            case DOWN:
                p_graphics.rotate(PConstants.TAU);
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

        p_graphics.fill(230, this.state.pressed? 100 : 50);
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
}
