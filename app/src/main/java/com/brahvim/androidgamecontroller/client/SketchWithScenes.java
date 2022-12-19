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

import java.io.File;
import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PVector;
import processing.event.TouchEvent;

public class SketchWithScenes extends Sketch {
    public static final String BROADCAST_ADDRESS = "255.255.255.255"; //getBroadAddr();

    void appStart() {
        Scene.setScene(loadScene);
    }

    // Scene flow:

    // `loadScene` -> `workScene`
    // `loadScene` -> `configSelectionScene` -> `loadScene` -> `workScene`

    // `loadScene` -> `configSelectionScene` -> `editorScene` -> `workScene`
    // `loadScene` -> `configSelectionScene` -> `editorScene` -> `controlChoiceScene` -> `workScene`


    // region Scene 'defs'!
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
              Sketch.cx, Sketch.cy - (welcomeTextWave * Sketch.qy));

            if (this.noCon) {
                // If we WERE connected previously,
                // (..which is also assumed in the first frame!)
                if (!this.pnoCon) {
                    // I just realized that I could use two sine wave instances for this...
                    // ...but I won't touch this till I need to.
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
                  .loadScene_no_network), Sketch.cx, Sketch.cy);

                fill(255);
                textSize(24);
                text(MainActivity.appAct.getString(R.string.loadScene_how_to_net), Sketch.cx,
                  Sketch.q3y);
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
                  .loadScene_looking_for_servers), Sketch.cx, Sketch.cy);
            }

            textSize(24);
            fill(255, abs(this.hotspotTextWave.get() * 255));
            text(MainActivity.appAct.getString(R.string
              .loadScene_how_to_net), Sketch.cx, Sketch.q3y);
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
                  R.string.loadScene_hotspot_mode_title), Sketch.cx, Sketch.cy);

                textSize(Sketch.DEFAULT_FONT_SIZE * 0.75f);
                text(MainActivity.appAct.getString(
                  R.string.loadScene_hotspot_mode_description), Sketch.cx, Sketch.cy - gap1);
            } else {
                textSize(Sketch.DEFAULT_FONT_SIZE);
                text(MainActivity.appAct.getString(
                  noServers? R.string.loadScene_no_wifi
                    : R.string.loadScene_looking_for_servers), Sketch.cx, Sketch.cy);

                if (!noServers) {
                    float gap1 = textAscent() - textDescent() * 16;
                    textSize(Sketch.DEFAULT_FONT_SIZE * 0.5f);

                    text(MainActivity.appAct.getString(
                        R.string.loadScene_press_for_hotspot),
                      Sketch.cx, Sketch.cy - gap1);
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
                        MainActivity.inSession = true;
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
                if (!(p_networks == null || MainActivity.inSession)) {
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
                new PVector(Sketch.q3x - 90, Sketch.q3y + 50),
                new PVector(150, 150),
                "A"))
            ));

            this.buttonRenderers.add(new ButtonRendererForClient(
              this.config.addConfig(new ButtonConfig(
                new PVector(Sketch.q3x + 90, Sketch.q3y + 50),
                new PVector(150, 150),
                "B"))
            ));
            // endregion

            // region Making DPAD buttons!
            this.dpadButtonRenderers.add(new DpadButtonRendererForClient(
              this.config.addConfig(new DpadButtonConfig(
                new PVector(Sketch.qx - 80, Sketch.q3y),
                new PVector(100, 100),
                DpadDirection.LEFT))
            ));

            this.dpadButtonRenderers.add(new DpadButtonRendererForClient(
              this.config.addConfig(new DpadButtonConfig(
                new PVector(Sketch.qx + 80, Sketch.q3y),
                new PVector(100, 100),
                DpadDirection.RIGHT))
            ));
            // endregion

            // region A touchpad!
            this.touchpadRenderers.add(new TouchpadRenderForClient(
              this.config.addConfig(new TouchpadConfig(
                new PVector(600, 800),
                new PVector(Sketch.q3x, Sketch.qy)
              ))));
            // endregion

            // region A thumbstick too! ...yeah, I gotta test things out, sorry...
            this.thumbstickRenderers.add(new ThumbstickRendererForClient(
              this.config.addConfig(new ThumbstickConfig(
                new PVector(80, 80),
                new PVector(Sketch.qx, Sketch.qy)
              ))
            ));
            // endregion

            //System.out.println("Configuration-to-state mapping numbers:");
            //for (ButtonRendererForClient r : this.buttonRenderers) {
            //System.out.printf("\t`%d`\n",r.this.config.controlNumber);
            //}

            MainActivity.config = this.config;

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
            System.out.printf("Received *some* bytes from IP: `%s`, on port:`%d`.\n",
              p_ip, p_port);

            if (RequestCode.packetHasCode(p_data)) {
                RequestCode code = RequestCode.fromReceivedPacket(p_data);
                System.out.printf("It was a code, `%s`!\n", code.toString());

                switch (code) {
                    case SERVER_CLOSE:
                        MainActivity.inSession = false;
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
        Scene previousScene;

        int waveEndMillis;
        SineWave fadeWave;

        // region This isn't in a class.
        // ...note that these are basically just offsets from the center.
        PVector yesPos = new PVector(), noPos = new PVector(), setPos = new PVector();

        // "Dy" AKA "Dynamic":
        PVector yesPosDy = new PVector(), noPosDy = new PVector(), setPosDy = new PVector();

        boolean yesPressed, noPressed, setPressed, canFillButtonColors;
        // endregion
        // endregion

        // Giving up on class "HoldableText" for performance. There'll be only 3 instances anyway.
        /*
        class HoldableText {
            private final static ArrayList<HoldableText> INSTANCES = new ArrayList<>(3);
            public PVector pos;

            private SineWave fadeWave;
            private boolean pressed;
            private String text;

            public HoldableText(PVector p_pos, String p_text) {
                this.pos = p_pos;
                this.text = p_text;
            }

            public HoldableText(float p_x, float p_y, String p_text) {
                this.pos = new PVector(p_x, p_y);
                this.text = p_text;
            }

            public void draw() {
                pushMatrix();
                pushStyle();

                popStyle();
                popMatrix();
            }

            public boolean isTouching(PVector p_touch) {
                return true;
            }

            public static void buttonCheck() {
                for (HoldableText t : HoldableText.INSTANCES) {
                    t.pressed = t.isTouching(Sketch.mouse);
                }
            }
        }
         */

        @Override
        public void setup() {
            frameRate(Sketch.refreshRate);

            this.previousScene = Scene.getPreviousScene();
            this.fadeWave = new SineWave(MainActivity.sketch, 0.001f);
            this.fadeWave.endWhenAngleIs(90);
            this.fadeWave.start(new Runnable() {
                @Override
                public void run() {
                    waveEndMillis = millis();
                }
            });

            this.yesPos.set(-Sketch.qx + 100, Sketch.qy * 0.5f);
            this.noPos.set(this.previousScene == editorScene?
              Sketch.qx - 150 : Sketch.qx - 100, this.yesPos.y);
            this.setPos.set(0, this.yesPos.y);

            this.canFillButtonColors = false;

            // These could be `true`, since the scene object retains information!
            //this.yesPressed = false; // Of course this one can't!
            this.noPressed = false;
            this.setPressed = false;
        }

        @Override
        public void draw() {
            background(0);
            //background(configShot);

            // region The box! (And prompt!):
            float wave = this.fadeWave.get();
            textSize(this.TEXT_SCALE * wave);

            float yPos = !this.fadeWave.active
              ? Sketch.cy + sin((millis() - this.waveEndMillis) * 0.001f) * 25
              : Sketch.cy * wave;

            float alpha = wave * 255;

            this.noPosDy.x = Sketch.cx + this.noPos.x;
            this.noPosDy.y = yPos + this.noPos.y;

            this.yesPosDy.x = Sketch.cx + this.yesPos.x;
            this.yesPosDy.y = yPos + this.yesPos.y;

            this.setPosDy.x = Sketch.cx + this.setPos.x;
            this.setPosDy.y = yPos + this.setPos.y;

            pushMatrix();
            translate(Sketch.cx, yPos);

            pushMatrix();
            scale(Sketch.cx, height);
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

            int noStrId = R.string.exitScene_no;

            if (this.previousScene == editorScene)
                noStrId = MainActivity.inSession?
                  R.string.exitScene_back_to_wait :
                  R.string.exitScene_back_to_work;

            text(MainActivity.appAct.getString(noStrId),
              this.noPosDy.x, this.noPosDy.y);
            // endregion

            // region "Configure" / "To Editor!" / "Add Control"
            // ...brings up the `editorScene` / `controlChoiceScene`:
            fill(0, 64, 214, alpha);
            if (this.canFillButtonColors && this.setPressed)
                fill(214, 214, 64, alpha);

            int setStrId = R.string.exitScene_set;

            if (this.previousScene == editorScene)
                setStrId = R.string.exitScene_add_control;
            else if (this.previousScene == controlChoiceScene)
                setStrId = R.string.exitScene_back_to_editor;

            text(MainActivity.appAct.getString(setStrId),
              this.setPosDy.x, this.setPosDy.y);
            // endregion
            // endregion
        }

        public void buttonCheck() {
            //if (Sketch.listOfUnprojectedTouches.size() == 0)
            //return;

            PVector touch = Sketch.mouse;
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

            this.setPressed = CollisionAlgorithms
              .ptRect(touch.x, touch.y,
                this.setPosDy.x - this.BOX_GAP,
                this.setPosDy.y - this.BOX_GAP,
                this.setPosDy.x + this.BOX_GAP,
                this.setPosDy.y + this.BOX_GAP);
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

            Scene previousScene = Scene.getPreviousScene();
            if (this.yesPressed) {
                completeExit();
            } else if (this.noPressed) {
                if (this.previousScene == editorScene)
                    Scene.setScene(MainActivity.inSession? workScene : loadScene);
                else
                    Scene.setScene(this.previousScene);
            } else if (this.setPressed) {
                Scene.setScene(previousScene == editorScene? controlChoiceScene : editorScene);
            }
        }

        @Override
        public void onBackPressed() {
            completeExit();
        }
        // endregion
    };

    // Load up the last used configuration
    // into `ClientRenderers.all`, receive changes,
    // ..save 'em new changes to another file or something!
    Scene editorScene = new Scene() {
        AgcConfigurationPacket config = MainActivity.config;

        @Override
        public void setup() {
            if (MainActivity.config == null) {
                this.config = MainActivity.config = new AgcConfigurationPacket();
            }
        }

        @Override
        public void draw() {
            background(0);
        }

        @Override
        public void onBackPressed() {
            // Let the user work. They'd expect AGC to be smart - gracefully
            // set the current configuration to be the one they were editing,
            // then let them do what they *were* doing.

            // In other words, don't drop them back into the decision of
            // "which confguration to use". They already know.
            //Scene.setScene(MainActivity.inSession? workScene : loadScene);
            Scene.setScene(exitScene);
        }
    };

    // You just add the control to `MainActivity.config`, positioned at (0, 0),
    // ...or at the CENTER of every controller!.
    // `SketchWithScenes::editorScene` handles the rest.

    Scene controlChoiceScene = new Scene() {
        // Layout:
        /*
        ________________________________
        |   Select a control to add:   |
        |GitHubLink|    2    |    3    |
        |----------|---------|---------|
        |     1    |    4    |    5    |
        |__________|_________|_________|
        */

        AgcConfigurationPacket config = Sketch.config;

        float headingTextY;
        float horizontalGridCenterLineY;
        float verticalGridLine1x, verticalGridLine2x;
        float gridBoxSize, gridBoxHalfSize, gridBoxQuarterSize;

        final AgcRectangle[] allRects = new AgcRectangle[6];
        int page = 0; // Can have more pages for more controls!

        @Override
        public void setup() {
            this.headingTextY = Sketch.qy - (Sketch.qy * 0.5f);

            this.gridBoxSize = width / 3;
            this.gridBoxHalfSize = this.gridBoxSize * 0.5f;
            this.gridBoxQuarterSize = this.gridBoxHalfSize * 0.5f;

            this.horizontalGridCenterLineY = Sketch.qy + (height - Sketch.qy) * 0.5f;

            this.verticalGridLine1x = this.gridBoxSize;
            this.verticalGridLine2x = width - this.verticalGridLine1x;

            float upperColEndY = Sketch.qy + this.gridBoxHalfSize,
              lowerColStartY = upperColEndY + 18,
              secondRectStartX = this.gridBoxSize + 1,
              thirdRectStartX = (this.gridBoxSize + 2) * 2;

            allRects[0] = new AgcRectangle(0, Sketch.qy, this.gridBoxSize, upperColEndY);
            allRects[1] = new AgcRectangle(secondRectStartX, Sketch.qy, thirdRectStartX,
              upperColEndY);
            allRects[2] = new AgcRectangle(thirdRectStartX, Sketch.qy, width, upperColEndY);

            allRects[3] = new AgcRectangle(0, lowerColStartY, this.gridBoxSize, height);
            allRects[4] = new AgcRectangle(secondRectStartX, lowerColStartY, thirdRectStartX,
              height);
            allRects[5] = new AgcRectangle(thirdRectStartX, lowerColStartY, width, height);
        }

        @Override
        public void draw() {
            background(0);

            pushMatrix();
            pushStyle();

            // Intersection detection and rendering for "selected" rectangle:
            if (mousePressed && Sketch.listOfUnprojectedTouches.size() > 0) {
                PVector touch = Sketch.mouse;
                if (touch.y > Sketch.qy) {
                    pushStyle();
                    rectMode(CORNER);
                    fill(255, 150);
                    for (AgcRectangle r : this.allRects)
                        if (r.contains(touch)) {
                            rect(r.start.x, r.start.y,
                              this.gridBoxSize, this.gridBoxHalfSize + 20);
                        }
                    popStyle();
                }

                // region Old method...
                //rect(
                //touch.x % this.gridBoxSize * (touch.x * this.gridBoxSize),
                //(touch.y % this.gridBoxHalfSize * (touch.x * this.gridBoxHalfSize)) - Sketch.qy,
                //this.gridBoxSize, this.gridBoxHalfSize);
                // endregion
            }

            textSize(72);
            text(MainActivity.appAct.getString(
              R.string.controlChoiceScene_heading), Sketch.cx, this.headingTextY);

            // The bar right below:
            stroke(255);
            strokeWeight(2);
            line(0, Sketch.qy, width, Sketch.qy);

            // region Draw a grid!
            // The bar at "half":
            line(0, this.horizontalGridCenterLineY, width, this.horizontalGridCenterLineY);

            // Vertical grid lines!:
            line(this.verticalGridLine1x, Sketch.qy, this.verticalGridLine1x, height);
            line(this.verticalGridLine2x, Sketch.qy, this.verticalGridLine2x, height);
            // endregion

            // The GitHub link!:
            pushStyle();
            fill(0, 64, 214);
            textSize(48);
            text(MainActivity.appAct.getString(
                R.string.controlChoiceScene_add_more),
              this.gridBoxHalfSize, Sketch.qy + this.gridBoxQuarterSize);
            popStyle();

            popMatrix();
            popStyle();
        }

        @Override
        public void touchEnded(TouchEvent p_touchEvent) {
            if (Sketch.listOfUnprojectedTouches.size() < 1)
                return;

            PVector touch = Sketch.mouse, controlPos = new PVector(/*Sketch.cx, Sketch.cy*/);

            if (this.config == null)
                controlPos.set(Sketch.cx, Sketch.cy);
            else /* Place at the midpoint of all controls! */ {
                int totalControls = 0;
                //ArrayList<> list;

                if (this.config.buttons != null) {
                    for (ButtonConfig c : this.config.buttons)
                        controlPos.add(c.transform);
                    totalControls += this.config.buttons.size();
                }

                if (this.config.dpadButtons != null) {
                    for (DpadButtonConfig c : this.config.dpadButtons)
                        controlPos.add(c.transform);
                    totalControls += this.config.dpadButtons.size();
                }

                if (this.config.thumbsticks != null) {
                    for (ThumbstickConfig c : this.config.thumbsticks)
                        controlPos.add(c.transform);
                    totalControls += this.config.thumbsticks.size();
                }

                if (this.config.touchpads != null) {
                    for (TouchpadConfig c : this.config.touchpads)
                        controlPos.add(c.transform);
                    totalControls += this.config.touchpads.size();
                }

                controlPos.div(totalControls);
            }

            if (touch.y > Sketch.qy)
                for (int i = 0; i < 6; i++)
                    if (allRects[i].contains(touch)) {
                        switch (this.page) {
                            case 0:
                                switch (i) {
                                    case 0:
                                        Sketch.config.buttons.add(null);
                                        break;

                                    case 1:
                                        break;

                                    case 2:
                                        break;

                                    case 3:
                                        break;

                                    case 4:
                                        break;

                                    case 5:
                                        break;

                                    default:
                                }
                                break;

                            case 1:
                                switch (i) {
                                    case 0:
                                        Sketch.config.buttons.add(null);
                                        break;

                                    case 1:
                                        break;

                                    case 2:
                                        break;

                                    case 3:
                                        break;

                                    case 4:
                                        break;

                                    case 5:
                                        break;

                                    default:
                                }
                                break;
                        }
                    }
        }

        @Override
        public void onBackPressed() {
            Scene.setScene(MainActivity.sketch.editorScene);
        }
    };

    // The user gets a file picker to do that here.
    // Just get to the point - scan only the directory AGC creates for configuration records!
    // And please, use "initialization" (`.ini`) files! Serialization will break with updates!
    Scene configSelectionScene = new Scene() {
        ArrayList<AgcListElement> listElements;
        File configsDir = new File(MainActivity.AGC_DIR.getAbsolutePath().concat("configs"));
        File[] configFiles;

        final float LIST_LABEL_TEXT_SIZE = 24, LIST_ELT_RECT_HEIGHT = 64;

        class AgcListElement {
            public AgcRectangle rect;
            public String label;

            public AgcListElement(PVector p_start, PVector p_end, String p_label) {
                this.label = p_label;
                this.rect = new AgcRectangle(p_start, p_end);
            }

            public AgcListElement(
              String p_label,
              float p_startX, float p_startY,
              float p_endX, float p_endY) {
                this.label = p_label;
                this.rect = new AgcRectangle(p_startX, p_startY, p_endX, p_endY);
            }
        }

        @Override
        public void setup() {
            if (!this.configsDir.exists()) {
                this.configsDir.mkdir();
            } else {
                this.configFiles = this.configsDir.listFiles();

                float startY = 0, endY = 0;
                for (int i = 0; i < this.configFiles.length; i++) {
                    startY = endY + height / i;
                    endY = startY + this.LIST_ELT_RECT_HEIGHT;
                    this.listElements.add(new AgcListElement(
                      this.configFiles[i].getName(),
                      Sketch.qx, startY, Sketch.q3x, endY));
                }
            }
        }

        @Override
        public void draw() {
            if (this.configFiles == null) {
                textSize(48);
                text(MainActivity.appAct.getString(R.string.configSelectionScene_no_configs),
                  Sketch.cx, Sketch.cy);

                textSize(24);
                text(MainActivity.appAct.getString(R.string.configSelectionScene_how_to_make),
                  Sketch.cx, Sketch.cy);
                return;
            }

            // Else, list files:

            pushStyle();
            strokeWeight(4);
            rectMode(CORNER);
            for (AgcListElement e : this.listElements) {
                if (mousePressed && e.rect.contains(Sketch.mouse)) {
                    fill(255, 150);
                } else noFill();
                //SketchWithScenes.super.Sketch.cy = 0; // Ayo!
                rect(e.rect.start.x, e.rect.start.y, Sketch.cx, this.LIST_ELT_RECT_HEIGHT);
            }
            popStyle();

        }

        @Override
        public void onBackPressed() {
            // Depending on whether or not you already were "in a session" with a server, AGC
            // should drop you back into the correct scene.

            // If you were in a session, you're dropped into workScene.
            // If not, loadScene.

            Scene.setScene(MainActivity.inSession? workScene : loadScene);
        }
    };
    // endregion

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
        MainActivity.inSession = false;

        System.out.println("Ending activity, killing process...");
        MainActivity.appAct.finish();
        Process.killProcess(Process.myPid());
    }
    // endregion

}