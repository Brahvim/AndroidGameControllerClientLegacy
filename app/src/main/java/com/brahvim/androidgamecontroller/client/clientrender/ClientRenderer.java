package com.brahvim.androidgamecontroller.client.clientrender;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import processing.core.PGraphics;
import processing.core.PVector;

public interface ClientRenderer {
    ArrayList<ClientRenderer> all = new ArrayList<>(5);

    <T> T getConfig();

    <T> T getState();

    // region Position modification.
    PVector getPosition();

    void setPosition(PVector p_pos);

    void setPosition(float p_x, float p_y);

    void addToPosition(PVector p_posAddent);

    void addToPosition(float p_x, float p_y);
    // endregion

    // region Scale modification.
    PVector getScale();

    void setScale(PVector p_pos);

    void setScale(float p_x, float p_y);

    void addToScale(PVector p_pos);

    void addToScale(float p_x, float p_y);
    // endregion

    void draw(@NotNull PGraphics p_graphics);

    void recordTouch();

    // region Touch events.
    void touchStarted();

    void touchMoved();

    void touchEnded();
    // endregion

}
