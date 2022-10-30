package com.brahvim.androidgamecontroller.client;

import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import com.brahvim.androidgamecontroller.RequestCode;

import java.util.ArrayList;

public class SketchWithScenes extends Sketch {
  void appStart() {
    ClientScene.setScene(loadScene);
  }

  ClientScene loadScene = new ClientScene() {
    String deviceNames;

    @Override
    //@RequiresApi(api = Build.VERSION_CODES.N_MR1)
    // ^^^ Better put this annotation for a function
    // that RETURNS the strings, concatenated.
    public void setup() {
      // Waah! No spam! Slow down!:
      frameRate(4);

      deviceNames = Build.MODEL;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        String btName =
          Settings.Secure.getString(MainActivity.appAct.getContentResolver(),
            Settings.Global.DEVICE_NAME);

        if (btName != null) {
          deviceNames = deviceNames.concat(Character.toString('\n'));
          deviceNames = deviceNames.concat(btName);
        }
      }
    }

    @Override
    public void draw() {
      background(0);

      ArrayList<String> possibleServers = getNetworks();
      boolean noServers = possibleServers == null;

      // Send an `ADD_ME` request to all servers!:
      if (!(noServers || SketchWithScenes.super.inSession)) {
        for (String s : possibleServers)
          socket.sendCode(RequestCode.ADD_ME,
            // Send the device's names (the manufacturer-assigned name, along with the
            // user-assigned bluetooth name):
            "CPH2083\nBrahvim's Oppo A12",
            // Finally, our IP and port number!:
            s, RequestCode.SERVER_PORT);
      }

      // Give a prompt accordingly:
      text(MainActivity.appAct.getString(
        noServers? R.string.loadScene_no_wifi
          : R.string.loadScene_looking_for_servers), cx, cy);
    }

    @Override
    public void onReceive(byte[] p_data, String p_ip, int p_port) {
      System.out.printf(
        "[LOAD SCENE] Received `%d` bytes saying \"%s\" from IP: `%s`, port:`%d`.\n",
        p_data.length, new String(p_data), p_ip, p_port);

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

    @Override
    public void onBackPressed() {
      completeExit();
    }
  };

  ClientScene workScene = new ClientScene() {
    @Override
    public void setup() {
      // Ok bois, time for a little speed!...
      frameRate(30);
    }

    @Override
    public void draw() {
      background(0);
      text("We're working! \":D!", cx, cy);
    }

    @Override
    public void onReceive(byte[] p_data, String p_ip, int p_port) {
      System.out.printf("Received *some* bytes from IP: `%s`, on port:`%d`.\n", p_ip, p_port);

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
      //quickExit();
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
