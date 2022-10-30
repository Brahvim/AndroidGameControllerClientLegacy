package com.brahvim.androidgamecontroller.client;

import com.brahvim.androidgamecontroller.RequestCode;
import com.brahvim.androidgamecontroller.UdpSocket;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class AgcClientSocket extends UdpSocket {
  AgcClientSocket() {
    super();
  }

  // region Custom methods.
  public void sendCode(RequestCode p_code, String p_ip, int p_port) {
    byte[] codeBytes = p_code.toBytes();
    byte[] toSend = new byte[codeBytes.length + RequestCode.CODE_SUFFIX.length];

    //System.out.printf("Copying the suffix, which takes `%d` out of `%d` bytes.\n",
    //RequestCode.CODE_SUFFIX.length, toSend.length);

    int i = 0;

    // Copy over the suffix,
    for (; i < RequestCode.CODE_SUFFIX.length; i++) {
      //System.out.printf("Value of iterator: `%d`.\n", i);
      toSend[i] = RequestCode.CODE_SUFFIX[i];
    }

    //System.out.printf("Copying the CODE, which takes `%d` out of `%d` bytes.\n",
    //codeBytes.length, toSend.length);

    // Put the code in!:
    for (i = 0; i < Integer.BYTES; i++) {
      //System.out.printf("Value of iterator: `%d`.\n", RequestCode.CODE_SUFFIX.length + i);
      toSend[RequestCode.CODE_SUFFIX.length + i] = codeBytes[i];
    }

    super.send(toSend, p_ip, p_port);
    System.out.printf("Sent code `%s` to IP: `%s`, port: `%d`.\n", p_code.toString(), p_ip, p_port);
  }

  public void sendCode(RequestCode p_code, String p_extraData, String p_ip, int p_port) {
    byte[] extraBytes = p_extraData.getBytes(StandardCharsets.UTF_8);

    byte[] toSend = new byte[RequestCode.CODE_SUFFIX.length + Integer.BYTES + extraBytes.length];
    byte[] codeBytes = p_code.toBytes();

    //System.out.printf("Copying the suffix, which takes `%d` out of `%d` bytes.\n",
    ///RequestCode.CODE_SUFFIX.length, toSend.length);

    int i = 0;

    // Copy over the suffix,
    for (; i < RequestCode.CODE_SUFFIX.length; i++) {
      //System.out.printf("Value of iterator: `%d`.\n", i);
      toSend[i] = RequestCode.CODE_SUFFIX[i];
    }

    //System.out.printf("Copying the CODE, which takes `%d` out of `%d` bytes.\n",
    //codeBytes.length, toSend.length);

    // Put the code in!:
    for (i = 0; i < Integer.BYTES; i++) {
      //System.out.printf("Value of iterator: `%d`.\n", RequestCode.CODE_SUFFIX.length + i);
      toSend[RequestCode.CODE_SUFFIX.length + i] = codeBytes[i];
    }

    //System.out.printf("Copying EXTRA DATA, which takes `%d` out of `%d` bytes.\n",
    //extraBytes.length, toSend.length);

    // Copy over extra bytes!:
    int startIdExtDataCopy = RequestCode.CODE_SUFFIX.length + codeBytes.length;
    for (i = 0; i < extraBytes.length; i++) {
      //System.out.printf("Value of iterator: `%d`.\n", RequestCode.CODE_SUFFIX.length + i);
      toSend[startIdExtDataCopy + i] = extraBytes[i];
    }

    super.send(toSend, p_ip, p_port);
    System.out.printf("Sent `%s` to IP: `%s`, port: `%d`.\n",
      new String(toSend).replace('\n', '\0'), p_ip, p_port);
  }
  // endregion

  @Override
  public void onReceive(@NotNull byte[] p_data, String p_ip, int p_port) {
    if (ClientScene.currentScene != null)
      ClientScene.currentScene.onReceive(p_data, p_ip, p_port);
  }

  // region Non-so-important Overrides.
  @Override
  protected void onStart() {
    System.out.println("The socket has begun, boiiii!");
    System.out.printf("Socket-Stats!:\n\t- IP: `%s`\n\t- Port: `%d`\n", super.getIp(),
      super.getPort());
  }

  @Override
  protected void onClose() {
    System.out.println("The socket's been disposed off, thanks for taking the service :)");
  }
  // endregion

}
