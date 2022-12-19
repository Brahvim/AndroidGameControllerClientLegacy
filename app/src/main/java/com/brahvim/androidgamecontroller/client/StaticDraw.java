package com.brahvim.androidgamecontroller.client;

import com.brahvim.androidgamecontroller.render.ButtonRendererBase;
import com.brahvim.androidgamecontroller.render.DpadButtonRendererBase;
import com.brahvim.androidgamecontroller.serial.ButtonShape;
import com.brahvim.androidgamecontroller.serial.DpadDirection;

import processing.core.PGraphics;

public class StaticDraw {
    public static void drawRoundButton(PGraphics p_graphics, String p_text, AgcRectangle p_rect) {
        p_graphics.pushMatrix();
        p_graphics.pushStyle();
        p_graphics.translate(p_rect.center.x, p_rect.center.y);
        p_graphics.scale(150, 150);
        // "Break your methods at logical points" - GFG article on Java optimization.
        // I trust this advice because the JIT exists! ":D!
        ButtonRendererBase.displayDraw(p_graphics, p_text, ButtonShape.ROUND, false);
        p_graphics.popStyle();
        p_graphics.popMatrix();
    }

    public static void drawRectButton(PGraphics p_graphics, String p_text, AgcRectangle p_rect) {
        p_graphics.pushMatrix();
        p_graphics.pushStyle();
        p_graphics.translate(p_rect.center.x, p_rect.center.y);
        p_graphics.scale(150, 150);
        // "Break your methods at logical points" - GFG article on Java optimization.
        // I trust this advice because the JIT exists! ":D!
        ButtonRendererBase.displayDraw(p_graphics, p_text, ButtonShape.RECTANGLE, false);
        p_graphics.popStyle();
        p_graphics.popMatrix();
    }

    public static void drawDpadButton(PGraphics p_graphics, DpadDirection p_dir,
                                      AgcRectangle p_rect) {
        p_graphics.pushMatrix();
        p_graphics.pushStyle();
        p_graphics.translate(p_rect.center.x, p_rect.center.y);
        p_graphics.scale(100, 100);
        DpadButtonRendererBase.displayDraw(p_graphics, p_dir, false);
        p_graphics.popMatrix();
        p_graphics.popStyle();
    }

    public static void drawThumbstick(PGraphics p_graphics, AgcRectangle p_rect) {
        p_graphics.pushMatrix();
        p_graphics.pushStyle();

        p_graphics.noFill();
        p_graphics.strokeWeight(6);
        p_graphics.ellipse(p_rect.center.x, p_rect.center.y, 80, 80);

        p_graphics.noStroke();
        p_graphics.fill(255);
        p_graphics.ellipse(p_rect.center.x, p_rect.center.y, 20, 20);

        p_graphics.popStyle();
        p_graphics.popMatrix();
    }

    public static void drawTouchpad(PGraphics p_graphics, AgcRectangle p_rect) {
        StaticDraw.drawRectButton(p_graphics, "", p_rect);
    }
}
