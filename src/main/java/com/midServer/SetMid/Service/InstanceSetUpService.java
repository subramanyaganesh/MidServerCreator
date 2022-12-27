package com.midServer.SetMid.Service;

import com.midServer.SetMid.Model.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.AbstractMultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.midServer.SetMid.Model.Constants.GLIDE_TABLE_API;
import static com.midServer.SetMid.Model.Constants.SYSTEM_USER;
import static com.midServer.SetMid.Service.shellUtils.connectDatabase;
import static com.midServer.SetMid.Service.shellUtils.executeShellCommand;

@Service
public class InstanceSetUpService {
    public static Logger logger = LoggerFactory.getLogger(HttpService.class);

    public static void unzipGlide(ZipModel zipModel) throws ZipException {
        try {
            System.out.println("Trying to unzip " + zipModel.getZipPath());
            new ZipFile(new File(zipModel.getZipPath()))
                    .extractAll(zipModel.getExtractedFilePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void updateGlideParameter(ZipModel zipModel, SQLDBModel sqldbModel) throws IOException {
        File conf;
        if ((conf = new File(zipModel.getExtractedFilePath() + File.separator + "dist/conf")).exists()) {
            FileWriter fileWriter = new FileWriter(conf + "/glide.properties");
            for (String s : processGlideCommands(Constants.list, sqldbModel, zipModel)) {
                fileWriter.write(s);
                fileWriter.append("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        }
    }

    static ExpFunction<SQLDBModel, Boolean> mySQLCommand = sqlDBModel -> {
        String command;
        if (sqlDBModel.getDatabasePassword() != null)
            command = "zcat < " + sqlDBModel.getDBDumpPath() + " | mysql -u" + sqlDBModel.getDatabaseUsername() + " -p" + sqlDBModel.getDatabasePassword() + " " + sqlDBModel.getDBName();
        else
            command = "zcat < " + sqlDBModel.getDBDumpPath() + " | mysql -u" + sqlDBModel.getDatabaseUsername() + " " + sqlDBModel.getDBName();
        executeShellCommand(command);
        return true;
    };

    public static void jdbcs(MultiValuedMap<String, String> commands, SQLDBModel sqldbModel) throws Exception {
        for (Map.Entry<String, String> stringStringEntry : commands.entries()) {
            if (stringStringEntry.getValue().equalsIgnoreCase("show databases;")) {
                if (connectDatabase(stringStringEntry, sqldbModel).toString().contains(sqldbModel.getDBName()))
                    break;
            } else connectDatabase(stringStringEntry, sqldbModel);
        }
    }

    public static Function<String, String> createDBName = fileName -> {
        try {
            return fileName.split("-dist.zip")[0].split("glide-dist-")[1].replace(".", "_");
        } catch (Exception e) {
            System.out.println("Providing random name as this dist path does not have what is required :: " + e.getLocalizedMessage());
        }
        return "myDb";
    };

    public static MultiValuedMap<String, String> processDbCommands(MultiValuedMap<String, String> map, SQLDBModel sqldbModel) {
        return map.entries().parallelStream()
                .collect(ArrayListValuedHashMap::new,
                        (out, input) -> out.put(input.getKey().replace("$$$$", sqldbModel.getDBName()), input.getValue().replace("$$$$", sqldbModel.getDBName())),
                        AbstractMultiValuedMap::putAll);
    }

    public static List<String> processGlideCommands(List<String> list, SQLDBModel sqldbModel, ZipModel zipModel) {
        return list.stream().map(allString -> allString
                .replace("####", zipModel.getPort())
                .replace("****", sqldbModel.getDatabasePassword())
                .replace("&&&&", sqldbModel.getDatabaseUsername())
                .replace("$$$$", sqldbModel.getDBName()))
                .collect(Collectors.toList());
    }

    static int i = 0;

    public static void checkIfInstanceIsUp(InstanceModel instanceModel, Instant startInstant, final Instant finalLimitTime) {
        try {
            if (startInstant.isBefore(finalLimitTime)) {
                logger.info("Waiting for " + (++i) + " minute");
                TimeUnit.MINUTES.sleep(1);
                System.out.println("Trying to hit the Api");
                HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + SYSTEM_USER, null);
                logger.info("\nTHE INSTANCE IS UP, THE URL IS : " + instanceModel.getInstanceUrl());
            }
        } catch (Exception e) {
            logger.info("The Error is : " + e.getLocalizedMessage());
            checkIfInstanceIsUp(instanceModel, Instant.now(), finalLimitTime);
        }
    }


    public static void builder(String dbVersion, String glideVersion, String extractedFilePath, ZipModel zipModel, SQLDBModel dbModel) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        if (!new File(extractedFilePath).exists()) {
            if (new File(extractedFilePath).mkdirs()) {
                System.out.println("Successfully created the directory");
            }
        } else {
            System.out.println("the directory already exists");
        }
        Glide_DB(glideVersion, dbVersion);
        jdbcs(processDbCommands(Constants.preMap, dbModel), dbModel);
        executorService.execute(new Thread(() -> {
            try {
                unzipGlide(zipModel);
                mySQLCommand.apply(dbModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        executorService.shutdown();
        while (!executorService.isTerminated())
            TimeUnit.MILLISECONDS.sleep(30);

        updateGlideParameter(zipModel, dbModel);
        jdbcs(processDbCommands(Constants.postMap, dbModel), dbModel);
        executeShellCommand(zipModel.getExtractedFilePath() + "/dist/startup.sh --snc-policy false");
        //checkIfInstanceIsUp(instanceModel, Instant.now(), Instant.now().plus(15, ChronoUnit.MINUTES));


    }

    public static void Glide_DB(String glideVersion, String DbVersion) throws IOException, InterruptedException {
        StringBuffer sbf = new StringBuffer();
        String[] cmd = new String[2];
        cmd[0] = "mvn -U dependency:get " +
                "-Dartifact=com.snc:glide-dist:" + glideVersion + ":zip:dist " +
                "-Dtransitive=false " +
                "-DremoteRepositories=dev::default::http://nexus.proxy.devsnc.com/content/groups/public";
        cmd[1] = "mvn -U dependency:get " +
                "-Dartifact=com.snc.glide.test:glide-db-dump:" + DbVersion + ":zsql:zboot " +
                "-Dtransitive=false " +
                "-DremoteRepositories=dev::default::https://nexus.devsnc.com/service/rest/repository/browse/public/";
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int j = 0; j < 2; j++) {
            int finalJ = j;
            executorService.execute(new Thread(() -> {
                try {
                    Process process = Runtime.getRuntime().exec(cmd[finalJ]);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    System.out.println("The output of the process ::\n" + "pwd" + "\n is::");
                    //bufferedReader.lines().forEach(System.out::println);
                    AtomicReference<String> prevLine = new AtomicReference<>("");
                    bufferedReader.lines().forEach(line -> {
                        if (!prevLine.get().equals(line)) {
                            System.out.println(line);
                            sbf.append(line);
                        }
                        prevLine.set(line);
                    });
                    System.out.println("============Errors=========================");
                    bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    bufferedReader.lines().forEach(System.out::println);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
        }
        if (sbf.toString().contains("[ERROR]")) {
            System.exit(1);
        }
    }

    /*public static void main(String[] args) throws Exception {
        String version = "21.discocopper.0.875";
        builder(version, version,"dbRootPassword");
    }*/
}
