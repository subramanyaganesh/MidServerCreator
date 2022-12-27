package com.midServer.SetMid.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.midServer.SetMid.Model.ServerModel;
import expectj.ExpectJ;
import expectj.ExpectJException;
import expectj.Spawn;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class tryyyi {
    public static void main(String[] args) {
        String s="ls<==>pwd<==>ps -ef|grep -v grep|grep -i oracle";

        ServerModel serverModel = ServerModel.builder()
                .command("ls")
                .jumpBoxIP("3.94.28.71")
                .jumpBoxUser("ruty")
                .ipAddress("172.33.1.148")
                .pemFilePath("/Users/subramanya.ganesh/Documents/OracleMid/rnd_ruty_id_rsa.pem")
                .serverUsername("ruty")
                .build();
        new tryyyi().sshExec(serverModel);
    }

    public void sshExec(ServerModel serverModel) {
        try {

            JSch jsch = new JSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            Session session = jsch.getSession(serverModel.getJumpBoxUser(), serverModel.getJumpBoxIP(), 22);
            session.setConfig(config);
            jsch.addIdentity(serverModel.getPemFilePath());
            session.connect();
            Channel channel = session.openChannel("shell");
            execJumpBox(channel,serverModel);
            session.disconnect();
            System.out.println("Script completed");

        } catch (Exception e) {
            System.out.println("SSH connection failed");
            e.printStackTrace();
        }
    }

    public void execJumpBox(Channel channel, ServerModel serverModel) {
        try {
            ExpectJ ex = new ExpectJ(30);
            Spawn spawn = ex.spawn(channel);

            spawn.send("ssh " + serverModel.getServerUsername() + "@" + serverModel.getIpAddress() + "\n");

            for (String command : serverModel.getCommand().split("<==>")) {
                spawn.send(command + "\n");
            }

            TimeUnit.SECONDS.sleep(3);
            String lsResults = spawn.getCurrentStandardOutContents();
            System.out.println("\n");
            System.out.println("\n");

            if (!(lsResults.equals(""))) {
                String[] lsRows1 = lsResults.split("\r\r\n");
                String[] lsRows = lsRows1[2].split("\r\n");
                int number = 1;
                for (int i = 0; i < lsRows.length; i++) {
                    if (lsRows[i].contains(serverModel.getServerUsername()) && lsRows[i].contains("$")) {
                        StringBuilder sb = new StringBuilder();
                        System.out.println(number++ + ":" + lsRows[i]);
                        while ((i + 1) < lsRows.length && !(lsRows[(i + 1)].contains(serverModel.getServerUsername()) && lsRows[(i + 1)].contains("$"))) {
                            i++;
                            sb.append(lsRows[i]).append("\n");
                        }
                        System.out.println(sb);
                    }
                }
            } else {
                String err = spawn.getCurrentStandardOutContents();
                String[] lsRows1 = err.split("\r\n");
                for (int i = 0; i < lsRows1.length; i++) {
                    System.out.println(i + ":" + lsRows1[i]);
                }
            }
            spawn.send("exit\n");
            if (!spawn.isClosed()) {
                System.out.println("Stopping spawn");
                spawn.stop();
            } else System.out.println("The Exit value is :" + spawn.getExitValue());

        } catch (IOException | ExpectJException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
