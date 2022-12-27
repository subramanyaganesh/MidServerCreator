package com.midServer.SetMid.Model;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Constants {
    String GLIDE_TABLE_API = "/api/now/table/";
    String SYSTEM_PROPERTIES_TABLE = "sys_properties";
    String SYSTEM_USER = "/sys_user";
    String SYSTEM_USER_ROLE = "/sys_user_has_role";
    Map<String, String> HEADER = Collections.singletonMap("Content-Type", "application/json");
    String MID_NAME = "/ecc_agent";
    String MID_CAPABILITY_M2M = "/ecc_agent_capability_m2m";
    String MID_APPLICATION_M2M = "/ecc_agent_application_m2m";
    String MID_IP_RANGE_M2M = "/ecc_agent_ip_range_m2m";
    String command = "ls\n" +
            "if test -d $HOME/##FileName##; then rm -rf ##FileName##;" +
            "for pid in `ps -ef|grep ##FileName##/agent/bin|grep -v grep|awk {'pid=$2'}{'print pid'}`;do kill -9 $pid;done;" +
            "echo \"removing ##FileName## and stopping process \"; else echo \"creating new mid\";fi\n" +
            "mkdir ##FileName##\n" +
            "cd ##FileName## && wget https://install.service-now.com/glide/distribution/builds/package/mid/##date##/mid.##VeRsIon##.linux.x86-64.zip && unzip mid.##VeRsIon##.linux.x86-64.zip\n" +
            "cd ##FileName##/agent && sed 's/YOUR_INSTANCE/##Instance##/1' config.xml > changedconfig.xml && mv changedconfig.xml config.xml\n" +
            "cd ##FileName##/agent && sed 's/##Instance##_USER_NAME_HERE/@@Username@@/' config.xml > changedconfig.xml && mv changedconfig.xml config.xml\n" +
            "cd ##FileName##/agent && sed 's/##Instance##_PASSWORD_HERE/$$Password$$/' config.xml > changedconfig.xml && mv changedconfig.xml config.xml\n" +
            "cd ##FileName##/agent && sed 's/YOUR_MIDSERVER_NAME_GOES_HERE/$$midserver$$/' config.xml > changedconfig.xml && mv changedconfig.xml config.xml\n" +
            "cd ##FileName##/agent && sh start.sh && echo $(pid=$!)";
    String DeletionCommand = "ls\n" +
            "if test -d $HOME/##FileName##; then rm -rf ##FileName##;" +
            "for pid in `ps -ef|grep ##FileName##/agent/bin|grep -v grep|awk {'pid=$2'}{'print pid'}`;do kill -9 $pid;done;" +
            " echo \"removing ##FileName## and stopping the process\"; else echo \"creating new mid\";fi";
String winCommand="cd C:\\Users\\Administrator\\Desktop && if exist ##FileName## ( cd ##FileName##/agent && stop.bat && cd ../.. && rmdir /s /q ##FileName## ) else ( echo creating new file)\n" +
        "cd C:\\Users\\Administrator\\Desktop && mkdir ##FileName##\n" +
        "cd C:\\Users\\Administrator\\Desktop\\##FileName## && Powershell (New-Object System.Net.WebClient).DownloadFile( ‘https://install.service-now.com/glide/distribution/builds/package/mid/##date##/mid.##VeRsIon##.windows.x86-64.zip',’mid-dist-##VeRsIon##-windows-x86-64.zip’) && tar.exe -xf mid-dist-##VeRsIon##-windows-x86-64.zip && cd agent && powershell -Command  \"(gc config.xml) -replace 'YOUR_INSTANCE_USER_NAME_HERE','@@Username@@' -replace 'YOUR_INSTANCE_PASSWORD_HERE','$$Password$$' -replace 'https://YOUR_INSTANCE.service-now.com', 'https://##Instance##.service-now.com' -replace 'YOUR_MIDSERVER_NAME_GOES_HERE','$$midserver$$' | Out-File -encoding ASCII config.xml\" && cd conf && powershell -Command   \"(gc wrapper-override.conf) -replace 'snc_mid','snc_mid_$$midserver$$'  -replace 'MID Server', 'MID Server_$$midserver$$' | Out-File -encoding ASCII wrapper-override.conf\"\n" +
        "cd C:\\Users\\Administrator\\Desktop\\##FileName##\\agent && start.bat";
    String localCommand = "https://install.service-now.com/glide/distribution/builds/package/mid/##date##/mid.##VeRsIon##.linux.x86-64.zip";//if it does not work change .linux.x86-64.zip to-> .osx.x86-64.zip
    List<String> list = Arrays.asList("glide.security.policy=none",
            "glide.servlet.port = ####",
            "glide.db.name = $$$$",
            "glide.db.user = &&&&",
            "glide.db.password =****",
            "glide.db.url = jdbc:mysql://localhost:3306/",
            "glide.installation.developer=true",
            "glide.plugins.directories=/plugin");

    MultiValuedMap<String, String> preMap = new ArrayListValuedHashMap<String, String>() {{
        put("", "show databases;");
        put("", "create database $$$$;");
    }};
    MultiValuedMap<String, String> postMap = new ArrayListValuedHashMap<String, String>() {{
        put("", "use $$$$;");
        put("$$$$", "show tables;");
        put("$$$$", "SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));");
    }};



}
