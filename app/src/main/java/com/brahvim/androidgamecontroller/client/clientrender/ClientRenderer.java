package com.brahvim.androidgamecontroller.client.clientrender;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import processing.core.PGraphics;

public interface ClientRenderer {
    ArrayList<ClientRenderer> all = new ArrayList<>(5);

    void draw(@NotNull PGraphics p_graphics);

    // region Touch events.
    void touchStarted();

    void touchMoved();

    void touchEnded();
    // endregion

}
