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
        //System.out.printf("Sent code `%s` to IP: `%s`, port: `%d`.\n", p_code, p_ip, p_port);
    }

    public void sendCode(RequestCode p_code, String p_extraData, String p_ip, int p_port) {
        this.sendCode(p_code, p_extraData.getBytes(StandardCharsets.UTF_8), p_ip, p_port);
    }

    public void sendCode(RequestCode p_code, byte[] p_extraData, String p_ip, int p_port) {
        //byte[] p_extraData = p_extraData.getBytes(StandardCharsets.UTF_8);

        byte[] toSend = new byte[
          RequestCode.CODE_SUFFIX.length + Integer.BYTES + p_extraData.length];
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
        //p_extraData.length, toSend.length);

        // Copy over extra bytes!:
        int startIdExtDataCopy = RequestCode.CODE_SUFFIX.length + codeBytes.length;
        for (i = 0; i < p_extraData.length; i++) {
            //System.out.printf("Value of iterator: `%d`.\n", RequestCode.CODE_SUFFIX.length + i);
            toSend[startIdExtDataCopy + i] = p_extraData[i];
        }

        super.send(toSend, p_ip, p_port);
        //System.out.printf("Sent `%s` to IP: `%s`, port: `%d`.\n",
        // new String(toSend).replace('\n', '\0'), p_ip, p_port);
    }
    // endregion

    @Override
    public void onReceive(@NotNull byte[] p_data, String p_ip, int p_port) {
        RequestCode code = null;
        byte[] extraData = RequestCode.getPacketExtras(p_data);

        if (RequestCode.packetHasCode(p_data)) {
            code = RequestCode.fromReceivedPacket(p_data);
            System.out.printf("It was a code, `%s`!\n", code.toString());

            switch (code) {
                case SERVER_CLOSE:
                    MainActivity.inSession = false;
                    break;

                default:
                    break;
            }
        } // End of `packetHasCode()` check.

        Scene currentScene = Scene.getCurrentScene();
        if (currentScene != null)
            if (code == null)
                currentScene.onReceive(null, p_data, p_ip, p_port);
            else
                currentScene.onReceive(code, extraData, p_ip, p_port);
    }

    // region Non-so-important Overrides.
    @Override
    protected void onStart() {
        System.out.println("The socket has begun!");
        System.out.printf("Socket-Stats!:\n\t- IP: `%s`\n\t- Port: `%d`\n", super.getIp(),
          super.getPort());
    }

    @Override
    protected void onClose() {
        System.out.println("The socket's been disposed off, thanks for taking the service :)");
    }
    // endregion

}
