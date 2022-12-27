package com.midServer.SetMid.Controller;

import com.midServer.SetMid.Model.*;
import com.midServer.SetMid.Service.InstanceSetUpService;
import com.midServer.SetMid.Service.MacInstall;
import com.midServer.SetMid.Service.ServerUtilService;
import com.midServer.SetMid.SetMidApplication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.temporal.ValueRange;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.midServer.SetMid.Repository.mongodb.*;
import static com.midServer.SetMid.Service.InstanceService.*;
import static com.midServer.SetMid.Service.InstanceSetUpService.builder;

@RestController
public class Controllers {
    String stringBuilder;

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping(value = {"/instance/{glideVersion}/{dbVersion}/{dbuser}/{dbpassword}",
            "/instance/{glideVersion}/{dbVersion}/{dbuser}"})
    public void instance(@PathVariable String glideVersion, String dbVersion, String dbuser, String dbpassword) {
        String basePath = System.getProperty("user.home") + "/.m2/repository/com/snc/";
        String extractionBasePath = System.getProperty("user.home") + "/Documents/LOCAL_INSTANCE/automationInstance/";
        String filePath = basePath + "glide-dist/" + glideVersion + "/glide-dist-" + glideVersion + "-dist.zip";
        String dbpath = basePath + "glide/test/glide-db-dump/" + dbVersion + "/glide-db-dump-" + dbVersion + "-zboot.zsql";
        String extractedFilePath = extractionBasePath + InstanceSetUpService.createDBName.apply(filePath);
        ZipModel zipModel = ZipModel.builder().zipPath(filePath).extractedFilePath(extractedFilePath).port("9089").build();
        SQLDBModel dbModel = SQLDBModel.builder().databaseUsername(dbuser).databasePassword(dbpassword).DBDumpPath(dbpath).DBName(InstanceSetUpService.createDBName.apply(filePath)).build();
        InstanceModel instanceModel = InstanceModel.builder().instanceUrl("http://localhost:" + zipModel.getPort() + "/").instanceUsername("admin").password("admin").build();
        try {
            builder(dbVersion, dbVersion, extractedFilePath, zipModel, dbModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = {"/noo/{id}", "/noo"})
    public String getgooById(@PathVariable(required = false) String id) {
        if (id == null) {
            return "the value is null bro";
        }
        return "ID: " + id;
    }

    @GetMapping(value = {"/moo"})
    public String getmooById(@RequestParam(required = false) String id, @RequestParam(required = false) String name) {
        if (id == null && name == null) {
            return "the values are null bro";
        }
        return "ID: " + id + "name: " + name;
    }

    @GetMapping(value = {"/createmid/{instanceName}/{username}/{password}/{midName}/{midPassword}",
            "/createmid/{instanceName}/{username}/{password}"})
    public String creation(@PathVariable String instanceName,
                           @PathVariable String username,
                           @PathVariable String password,
                           @PathVariable(required = false) String midName,
                           @PathVariable(required = false) String midPassword,
                           @RequestParam(required = false) String ipaddress,
                           @RequestParam(required = false) String serverUserName,
                           @RequestParam(required = false) String serverPassword,
                           @RequestParam(required = false) String pemPath,
                           @RequestParam(required = false) String local,
                           @RequestParam(required = false) String numberOfMid
    ) {
        try {
            String MID_DISPLAY_NAME = local == null ? "pikachu_" + instanceName : local.equalsIgnoreCase("true") ? "local_pikachu" : "local_richu";
            ServerModel serverModel = null;
            if (numberOfMid == null) numberOfMid = "1";
            InstanceModel instanceModel = InstanceModel.builder()
                    .instanceName(instanceName)
                    .instanceUsername(username)
                    .password(password)
                    .midserverName(midName == null ? "autoMid" : midName)
                    .midserverPassword(midPassword == null ? "Midserver@123" : midPassword)
                    .build();

            if (local == null || Objects.requireNonNull(local).equalsIgnoreCase("true")) {
                instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
                if (midName == null) createMidServerUser(instanceModel);
                instanceModel.setVersion(getInstanceVersion(instanceModel));
            } else {
                if (ValueRange.of(1024, 49151).isValidValue(Integer.parseInt(local))) {
                    instanceModel.setInstanceUrl("http://" + instanceModel.getInstanceName() + ":" + local + "/");
                    if (midName == null) createMidServerUser(instanceModel);
                    instanceModel.setVersion(getInstanceVersion(instanceModel, local));
                } else
                    throw new Exception("Please set the QueryParam 'local' as \"true\" if Mid is required for SAAS instance or provide the proper port number with range->(1024, 49151)");
            }

            for (int i = 0; i < Integer.parseInt(numberOfMid); i++) {
                deleteMidInstances(instanceModel, MID_DISPLAY_NAME + i);
                if (local != null) {
                    try {
                        MacInstall.createLocalMid(instanceModel, (MID_DISPLAY_NAME + i), setValuesToCommand(instanceModel, i, serverUserName, (MID_DISPLAY_NAME + i), Constants.localCommand), local);
                    } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                        System.out.println("Please make sure that you are not connected to VPN");
                    }
                } else {
                    serverModel = ServerModel.builder()
                            .command(setValuesToCommand(instanceModel, i, serverUserName, MID_DISPLAY_NAME, Constants.command))
                            .ipAddress(ipaddress == null ? "10.198.17.215" : ipaddress)
                            .serverUsername(serverUserName == null ? "root" : serverUserName)
                            .serverPassword(serverPassword == null ? "sundar@123" : serverPassword)
                            .pemFilePath(pemPath)
                            .build();
                    System.out.println("Command to be executed ::: " + serverModel.getCommand());
                    stringBuilder = new ServerUtilService().createServerSession(serverModel);
                }


                System.out.println("checking if mid is created in the instance");
                checkIfMidCreated(instanceModel, MID_DISPLAY_NAME + i, local);
                System.out.println("inserting data into the database");
                insertData(instanceModel, serverModel, local,
                        instanceModel.getInstanceName() + "-" + instanceModel.getMidserverName() + "-" + i,
                        MID_DISPLAY_NAME + i, new Date());
                System.out.println("validating mid" + MID_DISPLAY_NAME + i + "that is created in the instance");
                validateInstance(instanceModel, MID_DISPLAY_NAME + i);
                System.out.println("adding capabilities mid that is created in the instance");
                addMidCapabilities(instanceModel, MID_DISPLAY_NAME + i);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    @GetMapping(value = {"/createwinmid/{instanceName}/{username}/{password}/{midName}/{midPassword}",
            "/createwinmid/{instanceName}/{username}/{password}"})
    public String creationWindowsMid(@PathVariable String instanceName,
                                     @PathVariable String username,
                                     @PathVariable String password,
                                     @PathVariable(required = false) String midName,
                                     @PathVariable(required = false) String midPassword,
                                     @RequestParam(required = false) String ipaddress,
                                     @RequestParam(required = false) String serverUserName,
                                     @RequestParam(required = false) String serverPassword,
                                     @RequestParam(required = false) String local,
                                     @RequestParam(required = false) String numberOfMid
    ) {
        try {
            String MID_DISPLAY_NAME = "winPikachu_" + instanceName;
            ServerModel serverModel;
            if (numberOfMid == null) numberOfMid = "1";
            InstanceModel instanceModel = InstanceModel.builder()
                    .instanceName(instanceName)
                    .instanceUsername(username)
                    .password(password)
                    .midserverName(midName == null ? "autoMid" : midName)
                    .midserverPassword(midPassword == null ? "Midserver@123" : midPassword)
                    .build();
            instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
            if (midName == null) createMidServerUser(instanceModel);
            instanceModel.setVersion(getInstanceVersion(instanceModel));

            for (int i = 0; i < Integer.parseInt(numberOfMid); i++) {
                //String command = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(SetMidApplication.class.getClassLoader().getResource("config/WinMid")).toURI())));
                deleteMidInstances(instanceModel, MID_DISPLAY_NAME + i);
                serverModel = ServerModel.builder()
                        .command(setValuesToCommand(instanceModel, i, serverUserName, MID_DISPLAY_NAME, Constants.winCommand))
                        .ipAddress(ipaddress == null ? "10.196.102.169" : ipaddress)
                        .serverUsername(serverUserName == null ? "administrator" : serverUserName)
                        .serverPassword(serverPassword == null ? "Tol3r8t3now!" : serverPassword)
                        .build();
                System.out.println("Command to be executed ::: " + serverModel.getCommand());
                stringBuilder = new ServerUtilService().createServerSession(serverModel);


                System.out.println("checking if mid is created in the instance");
                checkIfMidCreated(instanceModel, MID_DISPLAY_NAME + i, local);
                System.out.println("inserting data into the database");
                insertData(instanceModel, serverModel, local,
                        instanceModel.getInstanceName() + "-" + instanceModel.getMidserverName() + "-" + i,
                        MID_DISPLAY_NAME + i, new Date());
                System.out.println("validating mid" + MID_DISPLAY_NAME + i + "that is created in the instance");
                validateInstance(instanceModel, MID_DISPLAY_NAME + i);
                System.out.println("adding capabilities mid that is created in the instance");
                addMidCapabilities(instanceModel, MID_DISPLAY_NAME + i);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    @GetMapping(value = {"/deleteExistingMidsInstance/{instanceName}/{username}/{password}/{midName}",
            "/deleteExistingMidsInstance/{instanceName}/{username}/{password}"
    })
    public String midInstaDeletion(@PathVariable String instanceName,
                                   @PathVariable String username,
                                   @PathVariable String password,
                                   @RequestParam(required = false) String ipaddress,
                                   @RequestParam(required = false) String serverUserName,
                                   @RequestParam(required = false) String local,
                                   @RequestParam(required = false) String serverPassword) {
        String MID_DISPLAY_NAME = local == null ? "local_pikachu" : "pikachu";
        try {
            InstanceModel instanceModel = InstanceModel.builder()
                    .instanceName(instanceName)
                    .instanceUsername(username)
                    .password(password)
                    .build();
            instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
            List<String> midList = listCollection().stream().parallel()
                    .filter(document -> document.containsKey("MidFileName") && ((String) document.get("MidFileName")).contains(instanceName))
                    .map(document -> (String) document.get("MidFileName"))
                    .collect(Collectors.toList());
            int i = 0;
            for (String midFileName : midList) {
                ServerModel serverModel = ServerModel.builder()
                        .command(setValuesToDeletionCommand(midFileName))
                        .ipAddress(ipaddress == null ? "10.198.13.249" : ipaddress)
                        .serverUsername(serverUserName == null ? "cmpdev" : serverUserName)
                        .serverPassword(serverPassword == null ? "cmpdev123" : serverPassword)
                        .build();
                System.out.println(serverModel.getCommand());
                stringBuilder = new ServerUtilService().createServerSession(serverModel);
                deleteMidInstance(instanceModel, MID_DISPLAY_NAME + i);
                i++;
            }
            deleteCollection(instanceModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    @GetMapping(value = {"/validateMid/{instanceName}/{username}/{password}/midName/{midName}",
            "/validateMid/{instanceName}/{username}/{password}"})
    public void midValidation(@PathVariable String instanceName,
                              @PathVariable String username,
                              @PathVariable String password,
                              @RequestParam(required = false) String local,
                              @PathVariable(required = false) String midName) {
        String MID_DISPLAY_NAME = local == null ? "local_pikachu" : "pikachu";

        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName(instanceName)
                .instanceUsername(username)
                .password(password).build();
        instanceModel.setInstanceUrl("https://" + instanceModel.getInstanceName() + ".service-now.com/");
        try {
            if (midName != null) {
                System.out.println("validating mid that is created in the instance");
                validateInstance(instanceModel, midName);
                System.out.println("adding capabilities mid that is created in the instance");
                addMidCapabilities(instanceModel, midName);
            } else {
                List<String> midList = listCollection().stream().parallel()
                        .filter(document -> document.containsKey("MidFileName") && ((String) document.get("MidFileName")).contains(instanceName))
                        .map(document -> (String) document.get("MidFileName"))
                        .collect(Collectors.toList());
                for (int i = 0; i < midList.size(); i++) {
                    System.out.println("validating mid that is created in the instance");
                    validateInstance(instanceModel, MID_DISPLAY_NAME + i);
                    System.out.println("adding capabilities mid that is created in the instance");
                    addMidCapabilities(instanceModel, MID_DISPLAY_NAME + i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public static void main(String[] args) throws Exception {
        ServerModel serverModel = null;
        String local = "true";
        String MID_DISPLAY_NAME = "local_pikachu";
        int i = 0;
        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName("localhost:8080")
                .instanceUsername("admin")
                .password("admin")
                .midserverName("midserver")
                .midserverPassword("midserver")
                .build();
//https://install.service-now.com/glide/distribution/builds/package/mid/2021/07/21/mid.trackdiscocopper-10-09-2020_07-21-2021_1900.linux.x86-64.zip
        MacInstall.createLocalMid(instanceModel, MID_DISPLAY_NAME, "https://install.service-now.com/glide/distribution/builds/package/mid/2021/07/21/mid.trackdiscocopper-10-09-2020_07-21-2021_1900.linux.x86-64.zip");

    }*/
    public static void main(String[] args) {
        System.out.println( System.getProperty("user.dir"));
    }
}
