package com.brahvim.androidgamecontroller.client;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.serial.config.ConfigurationPacket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.event.TouchEvent;
import processing.opengl.PGraphicsOpenGL;

public class Sketch extends PApplet {
    // region Fields! ":D!~
    private static final int SERVER_PORT = RequestCode.SERVER_PORT;

    // region Stuff that makes AGC *GO!*:
    public static AgcClientSocket socket;
    public static String serverIp;
    public boolean inSession; // Is the client sending the server data already?
    public static ConfigurationPacket MY_CONFIG;
    // endregion

    // region Boilerplate-y stuff.
    public static final ArrayList<PVector> listUnprojectedTouches = new ArrayList<>(10);
    // ^^^ Funny how `ArrayList`s have a capacity of `10` by default, haha.
    public PGraphicsOpenGL glGraphics;
    public float frameStartTime, pframeTime, frameTime;
    public static PFont DEFAULT_FONT;
    public static final float DEFAULT_FONT_SIZE = 72;
    public float cx, cy, qx, qy, q3x, q3y, scr, fov = PI / 3;
    public PVector cameraPos, cameraCenter, cameraUp;
    // endregion
    // endregion

    // region Settings-Setup-Exit...
    @Override
    public void settings() {
        fullScreen(P3D);
    }

    @Override
    public void setup() {
        //orientation(LANDSCAPE); // Forced it in the manifest, bois. It's *faster* now!
        updateRatios();

        socket = new AgcClientSocket();

        glGraphics = (PGraphicsOpenGL)g;

        cameraUp = new PVector(0, 1, 0);
        cameraPos = new PVector(cx, cy, 600);
        cameraCenter = new PVector(cx, cy);

        // region Processing style settings.
        Sketch.DEFAULT_FONT = createFont("SansSerif", Sketch.DEFAULT_FONT_SIZE);
        textFont(Sketch.DEFAULT_FONT);
        textAlign(CENTER);
        imageMode(CENTER);
        rectMode(CENTER);
        // endregion

        println("Setup was called!");
        MainActivity.sketch.appStart();
    }

    @Override
    public void exit() {
        System.err.println("Sending a `CLIENT_CLOSE` request.");
        if (socket != null) {
            socket.sendCode(RequestCode.CLIENT_CLOSE, serverIp, Sketch.SERVER_PORT);
            socket.close();
        }
        super.exit();
    }
    // endregion

    // region My usual boilerplate methods!
    public void updateRatios() {
        scr = (float)width / (float)height;
        cx = width * 0.5f;
        cy = height * 0.5f;
        qx = cx * 0.5f;
        qy = cy * 0.5f;
        q3x = cx + qx;
        q3y = cy + qy;
    }

    public static void unprojectTouches() {
        Sketch.listUnprojectedTouches.clear();
        TouchEvent.Pointer[] touches = MainActivity.sketch.touches;

        for (int i = 0; i < touches.length; i++) {
            PVector u = new PVector(touches[i].x, touches[i].y);
            u = MainActivity.sketch.glGraphics.modelviewInv.mult(u, null);
            u = MainActivity.sketch.glGraphics.cameraInv.mult(u, null);
            u.sub(MainActivity.sketch.width, MainActivity.sketch.height);
            Sketch.listUnprojectedTouches.add(u);
        }
    }

    /*
    public static void unprojectTouches() {
        Unprojector.captureViewMatrix((PGraphics3D)MainActivity.sketch.g);
        Sketch.listUnprojectedTouches.clear();

        for (int i = 0; i < MainActivity.sketch.touches.length; i++) {
            TouchEvent.Pointer p = MainActivity.sketch.touches[i];
            PVector u = new PVector();
            if (p != null)
                Unprojector.gluUnProject(
                  p.x, MainActivity.sketch.height - p.y, 0, u);
            Sketch.listUnprojectedTouches.add(u);
        }
    }
    */
    // endregion

    @Override
    public void draw() {
        frameStartTime = millis(); // Timestamp.
        frameTime = frameStartTime - pframeTime;
        pframeTime = frameStartTime;

        camera(cameraPos.x, cameraPos.y, cameraPos.z,
          cameraCenter.x, cameraCenter.y, cameraCenter.z,
          cameraUp.x, cameraUp.y, cameraUp.z);
        perspective(fov, scr, 0.1f, 10_000);

        if (ClientScene.currentScene != null)
            ClientScene.currentScene.draw();
        else println("Current scene is `null`!");
    }

    // region Custom methods.
    public ArrayList<String> getNetworks() {
        ArrayList<String> ret = new ArrayList<>(1);

        int ipEnd;

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"))) {
            for (String line; (line = reader.readLine()) != null; ) {
                if (line.isEmpty() || line.contains("IP address"))
                    continue;

                // The Address Resolution Protocol table may be empty! HANDLE IT HERE!
                if ((ipEnd = line.indexOf(' ')) != -1)
                    ret.add(line.substring(0, ipEnd));
                else return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If it's somehow STILL empty,
        return ret.size() == 0? null : ret;
    }

    // Courtesy of [https://stackoverflow.com/a/29238764]
    /*
    public static String getBroadAddr() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (interfaces == null)
            return null;

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            try {
                if (networkInterface.isLoopback())
                    continue;
            } catch (SocketException e) {
                e.printStackTrace();
            } // We don't want to use a loopback interface.

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null)
                    continue;

                String ret = broadcast.toString();
                return ret.substring(1);
            }
        }

        return null;
    }
     */
    // endregion

    // region Event callbacks.
    // region Mouse events.
    public void mousePressed() {
        ClientScene.currentScene.mousePressed();
    }

    public void mouseMoved() {
        ClientScene.currentScene.mouseMoved();
    }

    @SuppressWarnings("unused")
    public void mouseWheel(MouseEvent p_mouseEvent) {
        ClientScene.currentScene.mouseWheel(p_mouseEvent);
    }

    public void mouseClicked() {
        ClientScene.currentScene.mouseClicked();
    }

    public void mouseDragged() {
        ClientScene.currentScene.mouseDragged();
    }

    public void mouseReleased() {
        ClientScene.currentScene.mouseReleased();
    }
    // endregion

    // region Keyboard events.
    public void keyPressed() {
        ClientScene.currentScene.keyPressed();
    }

    public void keyTyped() {
        ClientScene.currentScene.keyTyped();
    }

    public void keyReleased() {
        ClientScene.currentScene.keyReleased();
    }
    // endregion

    // region Touch events (Andorid only, of course!).
    @Override
    public void touchStarted(TouchEvent p_touchEvent) {
        Sketch.unprojectTouches();
        ClientScene.currentScene.touchStarted(p_touchEvent);
    }

    @Override
    public void touchMoved(processing.event.TouchEvent p_touchEvent) {
        Sketch.unprojectTouches();
        ClientScene.currentScene.touchMoved(p_touchEvent);
    }

    @Override
    public void touchEnded(processing.event.TouchEvent p_touchEvent) {
        Sketch.unprojectTouches();
        ClientScene.currentScene.touchEnded(p_touchEvent);
    }
    // endregion
    // endregion

}
