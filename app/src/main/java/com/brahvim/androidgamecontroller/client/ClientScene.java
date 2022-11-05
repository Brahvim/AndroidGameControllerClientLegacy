package com.brahvim.androidgamecontroller.client;

import com.brahvim.androidgamecontroller.Scene;

import java.util.ArrayList;

public class ClientScene extends Scene {
    static ClientScene currentScene;
    static ArrayList<ClientScene> SCENES = new ArrayList<ClientScene>(3);

    public static void setScene(ClientScene p_scene) {
        ClientScene.currentScene = p_scene;
        p_scene.setup();
    }

    public static void addScene(ClientScene p_scene) {
        ClientScene.SCENES.add(p_scene);
    }

    public ClientScene() {
        ClientScene.SCENES.add(this);
    }

    public void touchStarted(processing.event.TouchEvent p_touchEvent) {
    }

    public void touchMoved(processing.event.TouchEvent p_touchEvent) {
    }

    public void touchEnded(processing.event.TouchEvent p_touchEvent) {
    }

    public void onBackPressed() {
    }

    public void onPause() {
    }

    public void onResume() {
    }

}
