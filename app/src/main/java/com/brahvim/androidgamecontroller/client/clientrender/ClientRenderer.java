package com.brahvim.androidgamecontroller.client.clientrender;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import processing.core.PGraphics;
import processing.core.PVector;

public interface ClientRenderer {
    ArrayList<ClientRenderer> all = new ArrayList<>(5);

    <T> T getConfig();

    <T> T getState();

    void setPosition(float p_x, float p_y);

    void setPosition(PVector p_pos);

    void setScale(float p_x, float p_y);

    void setScale(PVector p_pos);

    void draw(@NotNull PGraphics p_graphics);

    void recordTouch();

    // region Touch events.
    void touchStarted();

    void touchMoved();

    void touchEnded();
    // endregion

}
