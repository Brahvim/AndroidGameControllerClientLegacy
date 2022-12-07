package com.brahvim.androidgamecontroller.client;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Process;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.client.clientrender.ButtonRendererForClient;
import com.brahvim.androidgamecontroller.client.clientrender.ClientRenderer;
import com.brahvim.androidgamecontroller.client.clientrender.DpadButtonRendererForClient;
import com.brahvim.androidgamecontroller.client.clientrender.ThumbstickRendererForClient;
import com.brahvim.androidgamecontroller.client.clientrender.TouchpadRenderForClient;
import com.brahvim.androidgamecontroller.client.easings.SineWave;
import com.brahvim.androidgamecontroller.serial.ByteSerial;
import com.brahvim.androidgamecontroller.serial.DpadDirection;
import com.brahvim.androidgamecontroller.serial.configs.AgcConfigurationPacket;
import com.brahvim.androidgamecontroller.serial.configs.ButtonConfig;
import com.brahvim.androidgamecontroller.serial.configs.DpadButtonConfig;
import com.brahvim.androidgamecontroller.serial.configs.ThumbstickConfig;
import com.brahvim.androidgamecontroller.serial.configs.TouchpadConfig;

import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PVector;
import processing.event.TouchEvent;

public class SketchWithScenes extends Sketch {
    public static final String BROADCAST_ADDRESS = "255.255.255.255"; //getBroadAddr();

    void appStart() {
        Scene.setScene(loadScene);
    }

    Scene loadScene = new Scene() {
        final int ADD_ME_REQUEST_INTERVAL = 4;
        int startFrame;

        SineWave welcomeTextWave, searchTextWave, hotspotTextWave;
        WifiManager wifiMan;

        boolean isHotspotOn, isWifiOn, noCon, pnoCon;
        boolean searchTextWaveEventh;
        final int SEARCH_TEXT_WAVE_SLEEP_MILLIS = 4000;

        @Override
        public void setup() {
            frameRate(Sketch.refreshRate);
            startFrame = (int)Sketch.refreshRate;
            this.wifiMan = (WifiManager)MainActivity.appAct.getSystemService(Context.WIFI_SERVICE);
            HotspotStatus.init(this.wifiMan);

            this.welcomeTextWave = new SineWave(MainActivity.sketch, 0.001f);
            this.searchTextWave = new SineWave(MainActivity.sketch, 0.0015f);
            this.hotspotTextWave = new SineWave(MainActivity.sketch, 0.001f);

            this.welcomeTextWave.endWhenAngleIs(90);
            this.hotspotTextWave.endWhenAngleIs(90);

            this.noCon = false;
            this.pnoCon = false;
        }

        @Override
        public void draw() {
            background(0);
            if (frameCount < this.startFrame)
                return;
            else if (frameCount == this.startFrame) {
                this.welcomeTextWave.start(new Runnable() {
                    @Override
                    public void run() {
                        hotspotTextWave.start();
                        searchTextWave.start();
                    }
                });
            }

            // region Connection stuff!
            ArrayList<String> possibleServers = getNetworks();
            // If this is `true`, it means that the search
            // will be done on the WiFi hotspot instead.
            // ..that's probably going to be only me!
            this.isHotspotOn = HotspotStatus.isEnabled();
            this.isWifiOn = this.wifiMan.isWifiEnabled();
            this.pnoCon = this.noCon;
            this.noCon = !(this.isHotspotOn || this.isWifiOn);

            if (frameCount % ADD_ME_REQUEST_INTERVAL == 0)
                sendAddMeRequest(isHotspotOn, possibleServers);
            // endregion

            // region Rendering!
            textSize(72);
            float welcomeTextWave = this.welcomeTextWave.get();
            fill(255, welcomeTextWave * 255);
            text(MainActivity.appAct.getString(R.string.loadScene_welcome),
              cx, cy - (welcomeTextWave * qy));

            if (this.noCon) {
                // If we WERE connected previously,
                // (..which is also assumed in the first frame!)
                if (!this.pnoCon) {
                    // TODO: I just realized that I could use two sine wave instances for this...
                    this.searchTextWave = new SineWave(MainActivity.sketch, 0.01f);
                    this.searchTextWave.start(new Runnable() {
                        @Override
                        public void run() {
                            searchTextWaveEventh = !searchTextWaveEventh;

                            if (searchTextWaveEventh)
                                searchTextWave.start(/*this*/);
                            else {
                                searchTextWave.active = false;
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(SEARCH_TEXT_WAVE_SLEEP_MILLIS);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        searchTextWave.start();
                                    }
                                }.start();
                            }
                        }
                    });
                    this.searchTextWave.endWhenAngleIs(180);
                }

                fill(200, 100, 100);
                textSize(48 + this.searchTextWave.get() * 20);
                text(MainActivity.appAct.getString(R.string
                  .loadScene_no_network), cx, cy);

                fill(255);
                textSize(24);
                text(MainActivity.appAct.getString(R.string.loadScene_how_to_net), cx, q3y);
            } else {
                // If there previously was no connection,
                if (this.pnoCon) {
                    this.searchTextWave.end();
                    this.searchTextWave = new SineWave(MainActivity.sketch, 0.0015f);
                    this.searchTextWave.start();
                }

                textSize(48);
                fill(255, this.searchTextWave.get() * 255);
                text(MainActivity.appAct.getString(R.string
                  .loadScene_looking_for_servers), cx, cy);
            }

            textSize(24);
            fill(255, abs(this.hotspotTextWave.get() * 255));
            text(MainActivity.appAct.getString(R.string
              .loadScene_how_to_net), cx, q3y);
            // endregion

        }

        /*
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
            fill(255);

            // region
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
        */

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

        @Override
        public void onBackPressed() {
            agcExit();
        }

        public void sendAddMeRequest(boolean p_hotspotMode, ArrayList<String> p_networks) {
            if (p_hotspotMode) {
                // Send an `ADD_ME` request to all servers on the LAN!~:
                if (!(p_networks == null || SketchWithScenes.super.inSession)) {
                    for (String ip : p_networks)
                        socket.sendCode(RequestCode.ADD_ME,
                          // Manufacturer-assigned name, IP and port!:
                          Build.MODEL, ip, RequestCode.SERVER_PORT);
                }
            } else {
                socket.sendCode(RequestCode.ADD_ME,
                  // The manufacturer-assigned name of the Android device:
                  Build.MODEL,
                  // Finally, the universal LAN broadcast IP and port number!:
                  SketchWithScenes.BROADCAST_ADDRESS, RequestCode.SERVER_PORT);
            }
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
            frameRate(Sketch.refreshRate);
            textSize(Sketch.DEFAULT_FONT_SIZE);

            this.config = new AgcConfigurationPacket();

            // region Preparing the configuration packet.
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
            for (int i = 0; i < ClientRenderer.all.size(); i++)
                ClientRenderer.all.get(i).draw(g);
        }

        // region Touch events.
        @Override
        public void touchStarted(TouchEvent p_touchEvent) {
            for (int i = 0; i < ClientRenderer.all.size(); i++)
                ClientRenderer.all.get(i).touchStarted();
        }

        @Override
        public void touchMoved(TouchEvent p_touchEvent) {
            for (int i = 0; i < ClientRenderer.all.size(); i++)
                ClientRenderer.all.get(i).touchMoved();
        }

        @Override
        public void touchEnded(TouchEvent p_touchEvent) {
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
        // region Fields (seriously! :|).
        final float TEXT_SCALE = 48; //, TEXT_SCALE_HALF = this.TEXT_SCALE * 0.5f;
        final float BOX_GAP = this.TEXT_SCALE;

        int waveEndMillis;
        SineWave fadeWave;

        // "Dy" AKA "Dynamic":
        PVector yesPos = new PVector(), noPos = new PVector();
        PVector yesPosDy = new PVector(), noPosDy = new PVector();
        boolean yesPressed, noPressed, canFillButtonColors;
// endregion

        @Override
        public void setup() {
            frameRate(Sketch.refreshRate);

            this.fadeWave = new SineWave(MainActivity.sketch, 0.001f);
            this.fadeWave.endWhenAngleIs(90);
            this.fadeWave.start(new Runnable() {
                @Override
                public void run() {
                    waveEndMillis = millis();
                }
            });

            this.yesPos.set(-qx + 100, qy * 0.5f);
            this.noPos.set(qx - 100, this.yesPos.y);

            this.canFillButtonColors = false;

            // These could be `true`, since the scene object retains information!
            //this.yesPressed = false; // Of course this one can't!
            this.noPressed = false;
        }

        @Override
        public void draw() {
            background(0);
            //background(configShot);

            // region The box! (And prompt!):
            float wave = this.fadeWave.get();
            textSize(this.TEXT_SCALE * wave);

            float yPos = !this.fadeWave.active
              ? cy + sin((millis() - this.waveEndMillis) * 0.001f) * 25
              : cy * wave;

            float alpha = wave * 255;

            this.noPosDy.x = cx + this.noPos.x;
            this.noPosDy.y = yPos + this.noPos.y;

            this.yesPosDy.x = cx + this.yesPos.x;
            this.yesPosDy.y = yPos + this.yesPos.y;

            pushMatrix();
            translate(cx, yPos);

            pushMatrix();
            scale(cx, height);
            fill(64, alpha);
            rect(0, 0, 1.2f, 0.55f,
              0.1f, 0.1f, 0.1f, 0.1f);
            popMatrix();

            fill(255, alpha);
            text(MainActivity.appAct.getString(R.string.exitScene_prompt),
              0, yPos - this.yesPosDy.y);
            popMatrix();
            // endregion

            // region The text options :D!~
            // region "Yes" - exits the application:
            fill(0, 64, 214, alpha);
            if (this.canFillButtonColors && this.yesPressed)
                fill(214, 64, 0, alpha);

            text(MainActivity.appAct
              .getString(R.string.exitScene_yes), this.yesPosDy.x, this.yesPosDy.y);
            // endregion

            // region "No" - brings up the last scene:
            fill(0, 64, 214, alpha);
            if (this.canFillButtonColors && this.noPressed)
                fill(0, 214, 64, alpha);
            text(MainActivity.appAct.getString(R.string.exitScene_no), this.noPosDy.x,
              this.noPosDy.y);
            // endregion
            // endregion
        }

        public void buttonCheck() {
            //if (Sketch.listOfUnprojectedTouches.size() == 0)
            //return;

            PVector touch = Sketch.listOfUnprojectedTouches.get(0);
            this.yesPressed = CollisionAlgorithms
              .ptRect(touch.x, touch.y,
                this.yesPosDy.x - this.BOX_GAP,
                this.yesPosDy.y - this.BOX_GAP,
                this.yesPosDy.x + this.BOX_GAP,
                this.yesPosDy.y + this.BOX_GAP);
            this.noPressed = CollisionAlgorithms
              .ptRect(touch.x, touch.y,
                this.noPosDy.x - this.BOX_GAP,
                this.noPosDy.y - this.BOX_GAP,
                this.noPosDy.x + this.BOX_GAP,
                this.noPosDy.y + this.BOX_GAP);
        }

        // region Events.
        @Override
        public void touchStarted(TouchEvent p_touchEvent) {
            this.canFillButtonColors = true;
        }

        @Override
        public void touchMoved(TouchEvent p_touchEvent) {
            //if (!this.fadeWave.active && this.fadeWave.wasActive() &&
            if (this.canFillButtonColors && Sketch.listOfUnprojectedTouches.size() != 0)
                this.buttonCheck();
        }

        @Override
        public void touchEnded(TouchEvent p_touchEvent) {
            if (Sketch.listOfUnprojectedTouches.size() == 0)
                return;

            this.buttonCheck();

            if (this.yesPressed) {
                completeExit();
            }

            if (this.noPressed) {
                Scene.setScene(Scene.getPreviousScene());
            }
        }

        @Override
        public void onBackPressed() {
            completeExit();
        }
        // endregion
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
    public void agcExit() {
        Scene.setScene(exitScene);
    }

    public void quickExitIfCan() {
        if (serverIp != null) {
            completeExit();
        }
        System.out.println("`serverIp` is `null`, can't perform a quick exit!");
    }

    public void completeExit() {
        socket.sendCode(RequestCode.CLIENT_CLOSE, serverIp, RequestCode.SERVER_PORT);
        MainActivity.sketch.inSession = false;

        System.out.println("Ending activity, killing process...");
        MainActivity.appAct.finish();
        Process.killProcess(Process.myPid());
    }
    // endregion

}