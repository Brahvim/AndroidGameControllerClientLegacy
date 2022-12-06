package com.brahvim.androidgamecontroller.client;

import java.util.ArrayList;

public class Scene {
    public final static ArrayList<Scene> SCENES = new ArrayList<Scene>(3);
    private static Scene currentScene, previousScene;

    // region "`static`s".
    public static void setScene(Scene p_scene) {
        Scene.previousScene = currentScene;
        Scene.currentScene = p_scene;
        p_scene.setup();
    }

    public static Scene getCurrentScene() {
        return Scene.currentScene;
    }

    public static void addScene(Scene p_scene) {
        Scene.SCENES.add(p_scene);
    }

    public Scene() {
        Scene.SCENES.add(this);
    }
    // endregion

    // region Application callback structure.
    public void setup() {
    }

    public void draw() {
    }

    public void pre() {
    }

    public void post() {
    }

    // @SuppressWarnings("unused")
    public void onReceive(byte[] p_data, String p_ip, int p_port) {
    }
    // endregion

    // region Mouse events.
    public void mousePressed() {
    }

    public void mouseMoved() {
    }

    public void mouseWheel(processing.event.MouseEvent p_mouseEvent) {
    }

    public void mouseClicked() {
    }

    public void mouseDragged() {
    }

    public void mouseReleased() {
    }

    public void mouseExited() {
    }

    public void mouseEntered() {
    }
    // endregion

    // region Keyboard events.
    public void keyPressed() {
    }

    public void keyTyped() {
    }

    public void keyReleased() {
    }
    // endregion

    public void touchStarted(processing.event.TouchEvent p_touchEvent) {
    }

    public void touchMoved(processing.event.TouchEvent p_touchEvent) {
    }

    public void touchEnded(processing.event.TouchEvent p_touchEvent) {
    }

    // region Activity events.
    public void onBackPressed() {
    }

    public void onPause() {
    }

    public void onResume() {
    }
    // endregion

}
