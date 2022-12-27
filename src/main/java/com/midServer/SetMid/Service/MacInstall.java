package com.midServer.SetMid.Service;

import com.midServer.SetMid.Model.InstanceModel;
import com.midServer.SetMid.Model.ServerModel;
import com.midServer.SetMid.SetMidApplication;
import net.lingala.zip4j.ZipFile;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ValueRange;
import java.util.Objects;

import static com.midServer.SetMid.Service.InstanceService.*;
import static com.midServer.SetMid.Service.shellUtils.executeShellCommand;


public class MacInstall {
   /* public static void main(String[] args) throws Exception {
        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName("localhost")
                .instanceUsername("admin")
                .password("admin")
                .midserverName("midserver")
                .midserverPassword("midserver")
                .build();
        instanceModel.setInstanceUrl("http://" + instanceModel.getInstanceName() + ":" + 9092);
        createLocalMid(instanceModel, "newMid"
                , "https://install.service-now.com/glide/distribution/builds/package/mid/2021/08/03/mid.quebec-12-09-2020_08-03-2021_1000.linux.x86-64.zip<==>"
                , "9092");
    }*/

    public static void createLocalMid(InstanceModel instanceModel, String midDisplayName, String url, String local) throws Exception {
        url = url.split("<==>")[0];
        URL oracle = new URL(url);
        File baseDirectory = new File(System.getProperty("user.home") + "/Documents/auto_Mid");

        if (!baseDirectory.exists()) {
            if (baseDirectory.mkdir()) {
                System.out.println("The base directory '" + baseDirectory.getAbsolutePath() + "' has been created successfully");
            }
        }
        File directory;
        if (Objects.requireNonNull(local).equalsIgnoreCase("true")) {
            directory = new File(System.getProperty("user.home") + "/Documents/Auto_Mid/saas");

        } else if (ValueRange.of(1024, 49151).isValidValue(Integer.parseInt(Objects.requireNonNull(local)))) {
            directory = new File(System.getProperty("user.home") + "/Documents/Auto_Mid/local");
        } else {
            throw new Exception("Please set the local as \"true\" if Mid is required for SAAS instance or provide the proper port number with range->(1024, 49151)");
        }
        if (directory.exists()) {
            System.out.println("This Directory already existed..Hence deleting this directory");
            FileUtils.forceDelete(directory.getCanonicalFile());
            deletionProcess(directory);
        }
        if (directory.mkdir()) {
            System.out.println();
            System.out.println("directory::->" + directory.getAbsolutePath() + " has been created successfully");
        }
        File file = new File(directory.getAbsolutePath() + File.separator + "Temporary");
        if (file.createNewFile()) {
            System.out.println("created a new file named->Temporary ");
            System.out.println(file.getPath());
        }
        Path targetPath = new File(file.getAbsolutePath()).toPath();
        Instant instant = Instant.now();
        System.out.println("The time when the installation has started  ::  " + instant.getEpochSecond());
        Files.copy(oracle.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        Instant instant1 = Instant.now();
        System.out.println("The time when the installation completed  ::  " + instant1.getEpochSecond());
        System.out.println("The total time for installation is " + Duration.between(instant, instant1).getSeconds());

        new ZipFile(targetPath.toFile())
                .extractAll(directory.toString());
        File agent = new File(directory + "/agent");

        if (agent.exists()) {
            System.out.println("The Agent file has been successfully created");
            File config_xml = new File(agent + "/config.xml");
            if (config_xml.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                Files.lines(config_xml.toPath()).forEach(lines -> {
                    lines = lines.replace("https://YOUR_INSTANCE.service-now.com/", instanceModel.getInstanceUrl());
                    lines = lines.replace("YOUR_INSTANCE_USER_NAME_HERE", instanceModel.getMidserverName());
                    lines = lines.replace("YOUR_INSTANCE_PASSWORD_HERE", instanceModel.getMidserverPassword());
                    lines = lines.replace("YOUR_MIDSERVER_NAME_GOES_HERE", midDisplayName);
                    stringBuilder.append(lines).append("\n");

                });
                Files.copy(new ByteArrayInputStream(stringBuilder.toString().getBytes()), config_xml.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String home = System.getProperty("java.home").split("/jre")[0];
                System.out.println("The $JAVA_HOME of this system is :: " + home);

                File wrapper_conf = new File(agent + "/conf/wrapper.conf");
                StringBuilder wrapper = new StringBuilder();
                Files.lines(wrapper_conf.toPath()).forEach(lines -> {
                    lines = lines.replace("#wrapper.java.command=%JAVA_HOME%/bin/java", "wrapper.java.command=" + home + "/bin/java");
                    wrapper.append(lines).append("\n");

                });
                BufferedWriter br = new BufferedWriter(new FileWriter(wrapper_conf.getAbsolutePath()));
                br.write(wrapper.toString());
                br.flush();
                br.close();

                File bin_work = new File(agent + "/bin/work");
                if (!bin_work.exists()) {
                    if (bin_work.mkdir()) {
                        System.out.println("the work directory is created successfully");
                    }
                } else System.out.println("The work directory in bin already exists");


                //Process process = Runtime.getRuntime().exec("sh start.sh", null, agent);
                Process process = new ProcessBuilder().directory(agent).command("sh", "start.sh").start();
                Instant now = Instant.now().plusSeconds(60);
                while (process.isAlive()) {
                    Instant then = Instant.now();
                    if (then.getEpochSecond() > now.getEpochSecond()) {
                        break;
                    }
                }
                System.out.println("============Output=========================");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                System.out.println("The output of this process ->sh start.sh is ");
                bufferedReader.lines().forEach(System.out::println);
                System.out.println("============Errors=========================");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                System.out.println("The error of this process ->sh start.sh is ");
                bufferedReader.lines().forEach(System.out::println);
                System.out.println("============================================");

            }
        }
    }

    public static void deletionProcess(File directory) throws IOException {
        String command = "for pids in `ps -ef| grep -i '" + directory + "/agent/bin'| grep -v grep|awk '{print$2}'`;do kill -9 $pids;done";
        executeShellCommand(command);
    }

    public static void main(String[] args) throws Exception {
        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName("sdtup1")
                .instanceUsername("admin")
                .password("Admin@123")
                .midserverName("autoMid")
                .midserverPassword("Midserver@123")
                .build();
        instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
        instanceModel.setVersion(getInstanceVersion(instanceModel, "local"));


        String MID_DISPLAY_NAME="test";
        int i=0;
        String serverUserName="serverUserName";

        String command = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(SetMidApplication.class.getClassLoader().getResource("config/WinMid")).toURI())));
        deleteMidInstances(instanceModel, MID_DISPLAY_NAME + i);
        ServerModel serverModel = ServerModel.builder()
                .command(setValuesToCommand(instanceModel, i, serverUserName, MID_DISPLAY_NAME, command))
                .ipAddress( "10.196.39.238")
                .serverUsername( "administrator" )
                .serverPassword("Are1300")
                .build();
        System.out.println("Command to be executed ::: " + serverModel.getCommand());
       String stringBuilder = new ServerUtilService().createServerSession(serverModel);


        /*
        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName("localhost")
                .instanceUsername("admin")
                .password("Admin@123")
                .midserverName("newMid999")
                .midserverPassword("Midserver@123")
                .build();
        instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");




        File directory = new File(System.getProperty("user.home") + "/Documents/Auto_Mid/saas");
        File file = new File(directory.getAbsolutePath() + File.separator + "Temporary");
        Path targetPath = new File(file.getAbsolutePath()).toPath();
        new ZipFile(targetPath.toFile())
                .extractAll(directory.toString());
        File agent = new File(directory + "/agent");
        File config_xml = new File(agent + "/config.xml");
        StringBuilder stringBuilder = new StringBuilder();
        Files.lines(config_xml.toPath()).forEach(lines -> {
            lines = lines.replace("https://YOUR_INSTANCE.service-now.com/", instanceModel.getInstanceUrl());
            lines = lines.replace("YOUR_INSTANCE_USER_NAME_HERE", instanceModel.getMidserverName());
            lines = lines.replace("YOUR_INSTANCE_PASSWORD_HERE", instanceModel.getMidserverPassword());
            lines = lines.replace("YOUR_MIDSERVER_NAME_GOES_HERE", "local_pikachu");
            stringBuilder.append(lines).append("\n");

        });
        System.out.println(stringBuilder);*/
    }
}
