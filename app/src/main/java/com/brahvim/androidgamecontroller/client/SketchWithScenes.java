package com.brahvim.androidgamecontroller.client;

import android.os.Build;
import android.os.Process;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.clientrender.ButtonRendererForClient;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.config.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.config.ConfigurationPacket;

import java.util.ArrayList;

import processing.core.PVector;
import processing.event.TouchEvent;

public class SketchWithScenes extends Sketch {
    public final String BROADCAST_ADDRESS = "255.255.255.255"; //getBroadAddr();

    void appStart() {
        ClientScene.setScene(loadScene);
    }

    ClientScene loadScene = new ClientScene() {
        public final int ADD_ME_REQUEST_INTERVAL = 4;

        @Override
        public void onReceive(byte[] p_data, String p_ip, int p_port) {
            System.out.printf(
              "[LOAD SCENE] Received `%d` bytes from IP: `%s`, port:`%d`.\n",
              p_data.length, p_ip, p_port);

            if (RequestCode.packetHasCode(p_data)) {
                RequestCode code = RequestCode.fromPacket(p_data);
                System.out.printf("[LOAD SCENE] It was a code, `%s`!\n", code.toString());
                switch (code) {
                    case CLIENT_WAS_REGISTERED:
                        serverIp = p_ip;
                        SketchWithScenes.super.inSession = true;
                        ClientScene.setScene(workScene);
                        break;

                    default:
                        break;
                }
            } // End of `packetHasCode()` check.
        } // End of `onReceive()`.

        public void sendAddMeRequest(boolean p_hotspotMode, boolean p_noServers,
                                     ArrayList<String> p_networks) {
            if (p_hotspotMode) {
                // Send an `ADD_ME`request to all servers on the LAN!~:
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
            completeExit();
        }
    };

    ClientScene workScene = new ClientScene() {
        ArrayList<ButtonRendererForClient> buttonRenderers;
        //ArrayList<DpadButtonRenderer> dpadButtonRenderers;
        //ArrayList<ThumbstickRenderer> thumbstickRenderers;
        //ArrayList<TouchpadRenderer> touchpadRenderers;

        @Override
        public void setup() {
            // Ok bois, time for a little speed!...
            frameRate(1000);
            textSize(Sketch.DEFAULT_FONT_SIZE);

            ConfigurationPacket configsToSend = new ConfigurationPacket();

            // region Preparing the packet.
            configsToSend.AGC_VERSION = RequestCode.CLIENT_CURRENT_VERSION;
            configsToSend.appStartMilliSinceEpoch =
              System.currentTimeMillis() - MainActivity.sketch.millis();
            configsToSend.screenDimensions = new PVector(width, height);

            configsToSend.buttons = new ArrayList<>();
            configsToSend.dpadButtons = new ArrayList<>();
            configsToSend.thumbsticks = new ArrayList<>();
            configsToSend.touchpads = new ArrayList<>();
            // endregion

            buttonRenderers = new ArrayList<>();

            // region Making buttons!
            buttonRenderers.add(
              new ButtonRendererForClient(configsToSend.addObject(
                new ButtonConfig(qx, q3y, "A"))
              ));

            buttonRenderers.add(
              new ButtonRendererForClient(configsToSend.addObject(
                new ButtonConfig(q3x, q3y, "B"))
              ));

            buttonRenderers.add(
              new ButtonRendererForClient(configsToSend.addObject(
                new ButtonConfig(cx, cy, "START"))
              ));
            // endregion

            System.out.println("Configuration hashes:");
            for (ButtonRendererForClient r : buttonRenderers) {
                System.out.println(r.configHash());
            }

            Sketch.MY_CONFIG = configsToSend;
            socket.sendCode(RequestCode.CLIENT_SENDS_CONFIG,
              ByteSerial.encode(Sketch.MY_CONFIG),
              serverIp, RequestCode.SERVER_PORT);
        }

        @Override
        public void draw() {
            background(0);
            if (buttonRenderers != null)
                for (ButtonRendererForClient r : buttonRenderers) {
                    r.draw(g);
                }
        }

        @Override
        public void touchStarted(TouchEvent p_touchEvent) {
            if (buttonRenderers != null)
                for (ButtonRendererForClient r : buttonRenderers) {
                    r.touchStarted();
                }
        }

        @Override
        public void touchMoved(TouchEvent p_touchEvent) {
            if (buttonRenderers != null)
                for (ButtonRendererForClient r : buttonRenderers) {
                    r.touchMoved();
                }
        }

        @Override
        public void touchEnded(TouchEvent p_touchEvent) {
            if (buttonRenderers != null)
                for (ButtonRendererForClient r : buttonRenderers) {
                    r.touchReleased();
                }
        }

        @Override
        public void onReceive(byte[] p_data, String p_ip, int p_port) {
            System.out.printf("Received *some* bytes from IP: `%s`, on port:`%d`.\n", p_ip,
              p_port);

            if (RequestCode.packetHasCode(p_data)) {
                RequestCode code = RequestCode.fromPacket(p_data);
                System.out.printf("It was a code, `%s`!\n", code.toString());

                switch (code) {
                    case SERVER_CLOSE:
                        MainActivity.sketch.inSession = false;
                        ClientScene.setScene(loadScene);
                        break;

                    default:
                        break;
                }
            } // End of `packetHasCode()` check,
        } // End of `onReceive()`.

        @Override
        public void onBackPressed() {
            System.out.println("Back key pressed...");
            quickExitIfCan(); // Tells the server to exit if it can.
            completeExit();
        }

        @Override
        public void onPause() {
            // Ok, so the app basically exited. No, I won't use `savedInstanceState` :joy:
            quickExitIfCan();
            completeExit();
        }
    };

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
}