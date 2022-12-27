package com.midServer.SetMid.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.midServer.SetMid.Model.ServerModel;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Service
public class ServerUtilService {

    public String createChannel(Session session, String command) throws Exception {
        Channel channel = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            StringBuilder output = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    stringBuilder.append("exit-status: ").append(channel.getExitStatus());
                    if (channel.getExitStatus() == 1)
                        throw new Exception("Server error during execution of command\n" + command);
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(1000);
                //System.out.println("Execution in progress...");
                stringBuilder.append(out);

            }
            if (!output.toString().isEmpty() && output.length() != 0) {
                System.out.println("The output of executing command is ::" + output.toString());
                stringBuilder.append("The output of executing command is ::").append(output.toString());
                return stringBuilder.toString();
            }
        } catch (Exception e) {
            System.out.println("The exception is : " + e.getMessage());
            throw e;
        } finally {
            if (channel != null)
                channel.disconnect();
        }
        return null;
    }

    public String createServerSession(ServerModel serverModel) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println("Trying to ssh connection to the host : " + serverModel.getIpAddress() + " and the username : " + serverModel.getServerUsername());
        Session session = null;
        try {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(serverModel.getServerUsername(), serverModel.getIpAddress(), 22);
            if (serverModel.getPemFilePath() != null) {
                jsch.addIdentity(serverModel.getPemFilePath());
            } else {
                session.setPassword(serverModel.getServerPassword());
            }
            session.setConfig(config);
            session.connect();
            System.out.println("Connected to the host " + serverModel.getServerUsername());
            for (String command : serverModel.getCommand().split("<==>")) {
                System.out.println("Trying to run the command : " + command);
                stringBuilder.append("Trying to run the command : ").append(command);
                stringBuilder.append(createChannel(session, command));

            }
        } catch (Exception error) {
            System.out.println("The exception is : " + error.getMessage());
            System.out.println("Consider checking the creds of server/Check if VPN is required");
           /* counter++;
            if (!error.getLocalizedMessage().contains("Server error during execution of command")) {
                while (counter < 3) createServerSession(serverModel);
            }*/
            throw error;
        } finally {
            if (session != null)
                session.disconnect();
        }

        System.out.println("Completed the ssh connection");
        stringBuilder.append("Completed the ssh connection");
        return stringBuilder.toString();
    }











  /*  public static void main(String[] args) throws UnirestException, IOException {
        Model.InstanceModel instanceModel = Model.InstanceModel.builder()
                .instanceName("test1")
                .instanceUsername("admin")
                .password("admin")
                .midserverName("midserver")
                .midserverPassword("midserver")
                .build();
        instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
        instanceModel.setVersion(new Service().getInstanceVersion(instanceModel));
        Model.ServerModel serverModel = Model.ServerModel.builder()
                .command(new Service().setValuesToCommand(instanceModel))
                .ipAddress("10.198.13.249")
                .serverPassword("cmpdev123")
                .serverUsername("cmpdev")
                .build();
        System.out.println(serverModel.getCommand());
        for (String command : serverModel.getCommand().split("<==>"))
        System.out.println(command);
        //new Service().createServerSession(serverModel);
    }*/
}
