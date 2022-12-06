package com.brahvim.androidgamecontroller.client;

import android.os.Build;
import android.os.Process;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.clientrender.ButtonRendererForClient;
import com.brahvim.androidgamecontroller.client.clientrender.ClientRenderer;
import com.brahvim.androidgamecontroller.client.clientrender.DpadButtonRendererForClient;
import com.brahvim.androidgamecontroller.client.clientrender.ThumbstickRendererForClient;
import com.brahvim.androidgamecontroller.client.clientrender.TouchpadRenderForClient;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.DpadDirection;
import com.brahvim.androidgamecontroller.serial.configs.AgcConfigurationPacket;
import com.brahvim.androidgamecontroller.serial.configs.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.configs.DpadButtonConfig;
import com.brahvim.androidgamecontroller.serial.configs.ThumbstickConfig;
import com.brahvim.androidgamecontroller.serial.configs.TouchpadConfig;

import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.TouchEvent;

public class SketchWithScenes extends Sketch {
    public final String BROADCAST_ADDRESS = "255.255.255.255"; //getBroadAddr();

    void appStart() {
        Scene.setScene(loadScene);
    }

    PImage configShot;

    Scene loadScene = new Scene() {
        public final int ADD_ME_REQUEST_INTERVAL = 4;

        @Override
        public void onReceive(byte[] p_data, String p_ip, int p_port) {
            System.out.printf(
              "[LOAD SCENE] Received `%d` bytes from IP: `%s`, port:`%d`.\n",
              p_data.length, p_ip, p_port);

            if (RequestCode.packetHasCode(p_data)) {
                RequestCode code = RequestCode.fromReceivedPacket(p_data);
                System.out.printf("[LOAD SCENE] It was a code, `%s`!\n", code.toString());
                switch (code) {
                    case CLIENT_WAS_REGISTERED:
                        serverIp = p_ip;
                        SketchWithScenes.super.inSession = true;
                        Scene.setScene(workScene);
                        break;

                    default:
                        break;
                }
            } // End of `packetHasCode()` check.
        } // End of `onReceive()`.

        public void sendAddMeRequest(boolean p_hotspotMode, boolean p_noServers,
                                     ArrayList<String> p_networks) {
            if (p_hotspotMode) {
                // Send an `ADD_ME` request to all servers on the LAN!~:
                if (!(p_noServers || SketchWithScenes.super.inSession)) {
                    for (String s : p_networks)
                        socket.sendCode(RequestCode.ADD_ME,
                          // The manufacturer - assigned name of the Android device:
                          Build.MODEL,
                          // Finally, our IP and port number !:
                          s, RequestCode.SERVER_PORT);
                }
            } else {
                socket.sendCode(RequestCode.ADD_ME,
                  // The manufacturer - assigned name of the Android device:
                  Build.MODEL,
                  // Finally, the universal LAN broadcast IP and port number!:
                  "255.255.255.255", RequestCode.SERVER_PORT);
            }
        }

        @Override
        public void setup() {
            frameRate(30);
        }

        @Override
        public void draw() {
            background(0);

            ArrayList<String> possibleServers = getNetworks();
            boolean noServers = possibleServers == null,

              // If they're pressing on the screen, it means that
              // they want to search on their WiFi hotspot instead.
              // ..that's probably going to be only me!
              hotspotMode = touches.length > 0 && !noServers;

            if (frameCount % ADD_ME_REQUEST_INTERVAL == 0)
                sendAddMeRequest(hotspotMode, noServers, possibleServers);

            // Rendering!:
            if (hotspotMode) {
                textSize(Sketch.DEFAULT_FONT_SIZE * 1.5f);
                float gap1 = textAscent() - textDescent() * 12;

                text(MainActivity.appAct.getString(
                  R.string.loadScene_hotspot_mode_title), cx, cy);

                textSize(Sketch.DEFAULT_FONT_SIZE * 0.75f);
                text(MainActivity.appAct.getString(
                  R.string.loadScene_hotspot_mode_description), cx, cy - gap1);
            } else {
                textSize(Sketch.DEFAULT_FONT_SIZE);
                text(MainActivity.appAct.getString(
                  noServers? R.string.loadScene_no_wifi
                    : R.string.loadScene_looking_for_servers), cx, cy);

                if (!noServers) {
                    float gap1 = textAscent() - textDescent() * 16;
                    textSize(Sketch.DEFAULT_FONT_SIZE * 0.5f);

                    text(MainActivity.appAct.getString(
                        R.string.loadScene_press_for_hotspot),
                      cx, cy - gap1);
                }

            } // End of `hotspotMode` check.
        } // End of `loadScene.draw()`.

        @Override
        public void onBackPressed() {
            agcExit();
        }
    };

    Scene workScene = new Scene() {
        AgcConfigurationPacket config;

        ArrayList<ButtonRendererForClient> buttonRenderers;
        ArrayList<DpadButtonRendererForClient> dpadButtonRenderers;
        ArrayList<TouchpadRenderForClient> touchpadRenderers;
        ArrayList<ThumbstickRendererForClient> thumbstickRenderers;

        @Override
        public void setup() {
            // Ok bois! Time for a little speed!...
            frameRate(1000);
            textSize(Sketch.DEFAULT_FONT_SIZE);

            this.config = new AgcConfigurationPacket();

            // region Preparing the this.configuration packet.
            // TODO: Make a settings file for these little things!
            this.config.agcVersion = "v1.0.0";
            this.config.appStartMilliSinceEpoch =
              System.currentTimeMillis() - MainActivity.sketch.millis();
            this.config.screenDimensions = new PVector(width, height);

            this.config.buttons = new ArrayList<>();
            this.config.dpadButtons = new ArrayList<>();
            this.config.thumbsticks = new ArrayList<>();
            this.config.touchpads = new ArrayList<>();
            // endregion

            // region Makin' `ArrayList`s!
            this.buttonRenderers = new ArrayList<>();
            this.dpadButtonRenderers = new ArrayList<>();
            this.touchpadRenderers = new ArrayList<>();
            this.thumbstickRenderers = new ArrayList<>();
            // endregion

            // Don't forget `this.config.addObject()` when making new this.configurations!

            // region Making buttons!
            this.buttonRenderers.add(new ButtonRendererForClient(
              this.config.addConfig(new ButtonConfig(
                new PVector(q3x - 90, q3y + 50),
                new PVector(150, 150),
                "A"))
            ));

            this.buttonRenderers.add(new ButtonRendererForClient(
              this.config.addConfig(new ButtonConfig(
                new PVector(q3x + 90, q3y + 50),
                new PVector(150, 150),
                "B"))
            ));
            // endregion

            // region Making DPAD buttons!
            this.dpadButtonRenderers.add(new DpadButtonRendererForClient(
              this.config.addConfig(new DpadButtonConfig(
                new PVector(qx - 80, q3y),
                new PVector(100, 100),
                DpadDirection.LEFT))
            ));

            this.dpadButtonRenderers.add(new DpadButtonRendererForClient(
              this.config.addConfig(new DpadButtonConfig(
                new PVector(qx + 80, q3y),
                new PVector(100, 100),
                DpadDirection.RIGHT))
            ));
            // endregion

            // region A touchpad!
            this.touchpadRenderers.add(new TouchpadRenderForClient(
              this.config.addConfig(new TouchpadConfig(
                new PVector(600, 800),
                new PVector(q3x, qy)
              ))));
            // endregion

            // region A thumbstick too! ...yeah, I gotta test things out, sorry...
            this.thumbstickRenderers.add(new ThumbstickRendererForClient(
              this.config.addConfig(new ThumbstickConfig(
                new PVector(80, 80),
                new PVector(qx, qy)
              ))
            ));
            // endregion

            //System.out.println("Configuration-to-state mapping numbers:");
            //for (ButtonRendererForClient r : this.buttonRenderers) {
            //System.out.printf("\t`%d`\n",r.this.config.controlNumber);
            //}

            socket.sendCode(RequestCode.CLIENT_SENDS_CONFIG,
              ByteSerial.encode(this.config),
              serverIp, RequestCode.SERVER_PORT);
        }

        // region `draw()` and other event callbacks from Processing.
        @Override
        public void draw() {
            background(0);

            //System.out.printf("Framerate: `%d`.\n", (int)frameRate);
            if (ClientRenderer.all != null)
                for (int i = 0; i < ClientRenderer.all.size(); i++)
                    ClientRenderer.all.get(i).draw(g);
        }

        // region Touch events.
        @Override
        public void touchStarted(TouchEvent p_touchEvent) {
            if (ClientRenderer.all != null)
                for (int i = 0; i < ClientRenderer.all.size(); i++)
                    ClientRenderer.all.get(i).touchStarted();
        }

        @Override
        public void touchMoved(TouchEvent p_touchEvent) {
            if (ClientRenderer.all != null)
                for (int i = 0; i < ClientRenderer.all.size(); i++)
                    ClientRenderer.all.get(i).touchMoved();
        }

        @Override
        public void touchEnded(TouchEvent p_touchEvent) {
            if (ClientRenderer.all != null)
                for (int i = 0; i < ClientRenderer.all.size(); i++)
                    ClientRenderer.all.get(i).touchEnded();
        }
        // endregion
        // endregion

        @Override
        public void onReceive(byte[] p_data, String p_ip, int p_port) {
            System.out.printf("Received *some* bytes from IP: `%s`, on port:`%d`.\n", p_ip,
              p_port);

            if (RequestCode.packetHasCode(p_data)) {
                RequestCode code = RequestCode.fromReceivedPacket(p_data);
                System.out.printf("It was a code, `%s`!\n", code.toString());

                switch (code) {
                    case SERVER_CLOSE:
                        MainActivity.sketch.inSession = false;
                        Scene.setScene(loadScene);
                        break;

                    default:
                        break;
                }
            } // End of `packetHasCode()` check,
        } // End of `onReceive()`.

        @Override
        public void onBackPressed() {
            agcExit();
            //quickExitIfCan(); // Tells the server to exit if it can.
            //completeExit();
        }

        @Override
        public void onPause() {
            // Ok, so the app basically exited. No, I won't use `savedInstanceState` :joy:
            quickExitIfCan();
            completeExit();
        }
    };

    Scene exitScene = new Scene() {
        PVector yesPos = new PVector(), noPos = new PVector();

        @Override
        public void setup() {
            configShot = getGraphics();

            yesPos.set(-qx, qy * 0.9f);
            noPos.set(qx, yesPos.y);
        }

        @Override
        public void draw() {
            background(configShot);
            translate(cx, cy);

            // The box!:
            pushMatrix();
            scale(cx, height);
            fill(64);
            rect(0, 0, 1.2f, 0.55f,
              0.1f, 0.1f, 0.1f, 0.1f);
            popMatrix();

            // The text options :D!~
            fill(0, 64, 214);
            text(MainActivity.appAct.getString(R.string.exitScene_no), noPos.x, noPos.y);
            text(MainActivity.appAct.getString(R.string.exitScene_yes), yesPos.x, yesPos.y);
        }

        @Override
        public void mouseClicked() {
            Sketch.unprojectTouches();
            PVector touch = Sketch.listOfUnprojectedTouches.get(0);

//            boolean yesPressed = CollisionAlgorithms
//              .ptRect(
//
//                     ),
//              noPressed = CollisionAlgorithms
//                .ptRect(
//
//                       );
        }
    };

    // region `backgroundWithAlpha()` overloads.
    void backgroundWithAlpha(float p_grey, float p_alpha) {
        backgroundWithAlpha(p_grey, p_grey, p_grey, p_alpha);
    }

    void backgroundWithAlpha(float p_red, float p_green, float p_blue, float p_alpha) {
        pushMatrix();
        pushStyle();
        hint(PConstants.DISABLE_DEPTH_TEST);

        rectMode(CORNER);
        fill(p_red, p_green, p_blue, p_alpha);
        rect(0, 0, width, height);

        hint(PConstants.ENABLE_DEPTH_TEST);
        pushStyle();
        popMatrix();
    }
    // endregion

    // region Stuff that helps AGC exit.
    void agcExit() {
        Scene.setScene(exitScene);
    }

    void quickExitIfCan() {
        if (serverIp != null) {
            socket.sendCode(RequestCode.CLIENT_CLOSE, serverIp, RequestCode.SERVER_PORT);
            completeExit();
        }
        System.out.println("`serverIp` is `null`, can't perform a quick exit!");
    }

    void completeExit() {
        MainActivity.sketch.inSession = false;
        System.out.println("Ending activity, killing process...");

        MainActivity.appAct.finish();
        Process.killProcess(Process.myPid());
    }
    // endregion
}