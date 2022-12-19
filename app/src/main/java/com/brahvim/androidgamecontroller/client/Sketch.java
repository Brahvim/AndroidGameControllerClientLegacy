package com.brahvim.androidgamecontroller.client;

import android.content.Context;
import android.view.WindowManager;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.serial.configs.AgcConfigurationPacket;

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
    // region Stuff that makes AGC *GO!*:
    public static float refreshRate;
    public static AgcConfigurationPacket config = new AgcConfigurationPacket();
    public static AgcClientSocket socket;
    public static String serverIp;
    // endregion

    // region Boilerplate-y stuff.
    public final static PVector mouse = new PVector();
    public final static ArrayList<PVector> listOfUnprojectedTouches = new ArrayList<>(10);
    public volatile static ArrayList<PVector> plistOfUnprojectedTouches = new ArrayList<>(10);
    // ^^^ Funny how `ArrayList`s have a capacity of `10` by default, haha.
    public static PFont DEFAULT_FONT;
    public static final float DEFAULT_FONT_SIZE = 72;
    public static float cx, cy, qx, qy, q3x, q3y, scr, fov = PI / 3;

    public PVector cameraPos, cameraCenter, cameraUp;

    public PGraphicsOpenGL glGraphics;
    public float frameStartTime, pframeTime, frameTime;
    // endregion
    // endregion

    // region Settings-Setup-Exit...
    @Override
    public void settings() {
        fullScreen(P3D);
    }

    public static void main(String[] args) {
        System.out.println("""
          Hey! You see where it says he name of the connected device up there? d
          Click the option right beside it! Select "app"!""");
    }

    @Override
    public void setup() {
        //orientation(LANDSCAPE); // Forced it in the manifest, bois. It's *faster* now!
        updateRatios();

        Sketch.refreshRate =
          ((WindowManager)MainActivity.appAct.getSystemService(Context.WINDOW_SERVICE))
            .getDefaultDisplay().getRefreshRate();
        socket = new AgcClientSocket();
        glGraphics = (PGraphicsOpenGL)g;
        cameraUp = new PVector(0, 1, 0);
        cameraPos = new PVector(cx, cy, 600);
        cameraCenter = new PVector(cx, cy);

        Sketch.config.initLists();

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
            socket.sendCode(RequestCode.CLIENT_CLOSE, serverIp, RequestCode.SERVER_PORT);
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

    // Both projection and mapping are inaccurate...
    // (...with the `Unprojector` class turning out as useless...)
    // ...but I use mapping!:
    public static void unprojectTouches() {
        Sketch.listOfUnprojectedTouches.clear();
        TouchEvent.Pointer[] touches = MainActivity.sketch.touches;

        for (int i = 0; i < touches.length; i++) {
            // [WORKS, CHEAPEST, SAME LEVEL OF ACCURACY]
            // My own 'mapping' method!:
            /*
            PVector u = new PVector(
              PApplet.map(touches[i].x, 0, MainActivity.sketch.displayWidth,
                0, MainActivity.sketch.width),
              PApplet.map(touches[i].y, 0, MainActivity.sketch.displayHeight,
                0, MainActivity.sketch.height));
             */
            //u.add(MainActivity.sketch.cameraPos);

            // [WORKS, NOT CHEAP + STILL INACCURATE]
            // Unprojection of my own:
            PVector u = new PVector(touches[i].x, touches[i].y);
            u = MainActivity.sketch.glGraphics.modelviewInv.mult(u, null);
            //u = MainActivity.sketch.glGraphics.cameraInv.mult(u, null);
            u.sub(MainActivity.sketch.width, MainActivity.sketch.height);
            u.add(MainActivity.sketch.cx, MainActivity.sketch.cy);

            // [FAILURE] Unprojection using the `Unprojector` class:
            /*
            PVector u = new PVector(touches[i].x, touches[i].y);
            // Believe in the JIT!~
            Unprojector.captureViewMatrix(((PGraphics3D)MainActivity.sketch.getGraphics()));
            System.out.printf("Was unprojection successful? %s\n",
              Unprojector.gluUnProject(u.x, u.y, u.z, u) // Yes, you can do that. Passing by value.
                ? "Yes!" : "No...");
            u.x *= 1.2f; //MainActivity.sketch.qx;
            u.y *= 1.2f; //MainActivity.sketch.qy;
             */

            // (...here's longer text explaining that.. :)
            /*
            // [sic] "As different streams having their sources in different places all mingles
            // their water in the sea, so, O Lord, the different paths which men take through
            // tendencies, various touch as they appear, crooked or straight, all lead to Thee."
            // - Swami Vivekananda, quoting a hymn in his `1893` Chicago convention speech.
            // (((I do not guarantee complete correctness in the copying of that quote.)))
             */
            // ^^^ (...that basically, this `u.z` modfication will go unchanged,
            // no matter what un-projection method you use!:)
            u.z = touches[i].pressure; // Should be accessed in some other way, but whatever...
            Sketch.listOfUnprojectedTouches.add(u);
        }
    }

    /*
    public static void unprojectTouches() {
        Unprojector.captureViewMatrix((PGraphics3D)MainActivity.sketch.g);
        Sketch.listOfUnprojectedTouches.clear();

        for (int i = 0; i < MainActivity.sketch.touches.length; i++) {
            TouchEvent.Pointer p = MainActivity.sketch.touches[i];
            PVector u = new PVector();
            if (p != null)
                Unprojector.gluUnProject(
                  p.x, MainActivity.sketch.height - p.y, 0, u);
            Sketch.listOfUnprojectedTouches.add(u);
            u.sub(Sketch.cx, Sketch.cy);
        }

        System.out.println("Unprojected touches:");

        for (PVector v : Sketch.listOfUnprojectedTouches) {
            System.out.println(v);
        }
    }
     */
    // endregion

    @Override
    public void draw() {
        frameStartTime = millis(); // Timestamp.
        frameTime = frameStartTime - pframeTime;
        pframeTime = frameStartTime;

        // No camera, since I'm using mapping for un-projection.
        // Would be a good idea to just *use* un-projection!...

        //camera(cameraPos.x, cameraPos.y, cameraPos.z,
        //cameraCenter.x, cameraCenter.y, cameraCenter.z,
        //cameraUp.x, cameraUp.y, cameraUp.z);

        perspective(fov, scr, 0.1f, 10_000);

        if (Sketch.listOfUnprojectedTouches.size() > 0)
            mouse.set(Sketch.listOfUnprojectedTouches.get(0));

        // Render the current scene!:
        {
            Scene currentScene = Scene.getCurrentScene();
            if (currentScene != null) {
                pushMatrix();
                pushStyle();
                currentScene.draw();
                popStyle();
                popMatrix();
            } else println("Current scene is `null`!");
        }

        pushStyle();
        fill(0, 255, 0);
        for (PVector v : Sketch.listOfUnprojectedTouches) {
            ellipse(v.x, v.y, 20, 20);
        }
        popStyle();

        // Also record the `mouse` vector here, haha:
        if (Sketch.listOfUnprojectedTouches.size() > 0)
            mouse.set(Sketch.listOfUnprojectedTouches.get(0));

        Sketch.plistOfUnprojectedTouches = Sketch.listOfUnprojectedTouches;
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
    // region Mouse events (they are literally detected before touch ones!).
    public void mousePressed() {
        Sketch.unprojectTouches();
        Scene.getCurrentScene().mousePressed();
    }

    // Never called by Processing!:
    public void mouseMoved() {
        Scene.getCurrentScene().mouseMoved();
    }

    @SuppressWarnings("unused")
    public void mouseWheel(MouseEvent p_mouseEvent) {
        Scene.getCurrentScene().mouseWheel(p_mouseEvent);
    }

    // ...also never called:
    public void mouseClicked() {
        Scene.getCurrentScene().mouseClicked();
    }

    public void mouseDragged() {
        Scene.getCurrentScene().mouseDragged();
    }

    public void mouseReleased() {
        Sketch.unprojectTouches();
        Scene.getCurrentScene().mouseReleased();
    }
    // endregion

    // region Keyboard events.
    public void keyPressed() {
        Scene.getCurrentScene().keyPressed();
    }

    public void keyTyped() {
        Scene.getCurrentScene().keyTyped();
    }

    public void keyReleased() {
        Scene.getCurrentScene().keyReleased();
    }
    // endregion

    // region Touch events (Android only, of course!).
    @Override
    public void touchStarted(TouchEvent p_touchEvent) {
        Scene.getCurrentScene().touchStarted(p_touchEvent);
    }

    // "YE ONLY MOVEMENT EVENT".
    @Override
    public void touchMoved(processing.event.TouchEvent p_touchEvent) {
        Sketch.unprojectTouches();
        Scene.getCurrentScene().touchMoved(p_touchEvent);
    }
    // "FAIR WINDS T' YE!"

    @Override
    public void touchEnded(processing.event.TouchEvent p_touchEvent) {
        Scene.getCurrentScene().touchEnded(p_touchEvent);
    }
    // endregion
    // endregion

}
