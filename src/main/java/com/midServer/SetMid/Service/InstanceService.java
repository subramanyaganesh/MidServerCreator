package com.midServer.SetMid.Service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.midServer.SetMid.Controller.Controllers;
import com.midServer.SetMid.Model.InstanceModel;
import com.midServer.SetMid.Model.ServerModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.midServer.SetMid.Model.Constants.*;

@Service
public class InstanceService {
    @Autowired
    Controllers controller;
   /* public static StringBuilder textReader(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        return reader.lines().collect(StringBuilder::new, (sb, s1) -> sb.append(s1).append("<==>"), StringBuilder::append);
    }*/

    public static StringBuilder textReaders(String command) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(command));
        return reader.lines().collect(StringBuilder::new, (sb, s1) -> sb.append(s1).append("<==>"), StringBuilder::append);
    }

    public static String setValuesToCommand(InstanceModel instanceModel, int numberOfMid, String serverUserName, String MID_DISPLAY_NAME, String command) throws IOException {
        //StringBuilder str = InstanceService.textReader(System.getProperty("user.dir") + "/config/Commands");
        StringBuilder str = InstanceService.textReaders(command);
        String date = new StringBuilder(instanceModel.getVersion()).reverse().toString().split("_")[1];
        date = new StringBuilder(date).reverse().toString();
        return str.toString().trim()
                .replace("##FileName##", instanceModel.getInstanceName() + "-" + instanceModel.getMidserverName() + "-" + numberOfMid)
                .replace("##Instance##", instanceModel.getInstanceName())
                .replace("##serverUser##", serverUserName == null ? "" : serverUserName)
                .replace("@@Username@@", instanceModel.getMidserverName())
                .replace("$$Password$$", instanceModel.getMidserverPassword())
                .replace("##VeRsIon##", instanceModel.getVersion())
                .replace("$$midserver$$", MID_DISPLAY_NAME + numberOfMid)
                .replace("##date##", date.split("-")[2] + "/" + date.split("-")[0] + "/" + date.split("-")[1]);

    }

    public static String setValuesToDeletionCommand(String fileName) throws IOException {
        //StringBuilder str = InstanceService.textReader(System.getProperty("user.dir") + "/config/DeletionCommand");
        StringBuilder str = InstanceService.textReaders(DeletionCommand);
        return str.toString().trim()
                .replace("##FileName##", fileName);
    }

    public static String getInstanceVersion(InstanceModel instanceModel) throws UnirestException {
        Map<String, Object> query = new HashMap<>();
        query.put("name", "glide.war");
        query.put("sys_name", "glide.war");
        query.put("sysparm_fields", "value");
        JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + SYSTEM_PROPERTIES_TABLE, query);
        System.out.println("JSON for getInstanceVersion :::: " + jsonArray);
        AtomicReference<String> value = new AtomicReference<>("");
        jsonArray.forEach(json -> value.set(((JSONObject) json).getString("value")));
        return value.get().split("glide-")[1].split(".zip")[0];
    }

    public static String getInstanceVersion(InstanceModel instanceModel, String local) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("name", "mid.version");
        query.put("sysparm_fields", "value");
        JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + SYSTEM_PROPERTIES_TABLE, query);
        System.out.println("JSON for version :::: " + jsonArray);
        if (!jsonArray.isNull(0))
            return jsonArray.getJSONObject(0).getString("value");
        else
            throw new NullPointerException("The mid.version properties is not present for:: http://" + instanceModel.getInstanceName() + ":" + local);
    }

    public static void validateInstance(InstanceModel instanceModel, String midName) throws UnirestException {
        Map<String, Object> query = new HashMap<>();
        query.put("name", midName);
        query.put("sysparm_fields", "name,status,validated,sys_id");
        JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
        AtomicReference<String> value = new AtomicReference<>("");
        jsonArray.forEach(json -> value.set(((JSONObject) json).getString("sys_id")));

        System.out.println(HttpService.instancePutCall(instanceModel, GLIDE_TABLE_API + MID_NAME + "/" + value.get(), Collections.singletonMap("Content-Type", "application/json"), query, "{\"validated\":\"true\"}"));
        while (!jsonArray.getJSONObject(0).getString("validated").equalsIgnoreCase("true")) {
            System.out.println("validating the instance...");
            jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
        }
        System.out.println("validation of instance successful");

    }

    public static void addMidCapabilities(InstanceModel instanceModel, String midName) throws UnirestException {
        Map<String, Object> query = new HashMap<>();
        query.put("name", midName);
        query.put("sysparm_fields", "name,status,validated,sys_id");
        JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
        AtomicReference<String> value = new AtomicReference<>("");
        jsonArray.forEach(json -> value.set(((JSONObject) json).getString("sys_id")));

        Map<String, Object> postQuery = new HashMap<>();
        postQuery.put("sysparm_exclude_reference_link", "true");
        postQuery.put("sysparm_display_value", "true");
        postQuery.put("sysparm_fields", "capability,application,ip_range");

        System.out.println(HttpService.instancePostCall(instanceModel, GLIDE_TABLE_API + MID_CAPABILITY_M2M, HEADER, postQuery, "{\"agent\":\"" + value.get() + "\",\"capability\":\"eeab973fd7802200bdbaee5b5e610381\"}"));
        System.out.println(HttpService.instancePostCall(instanceModel, GLIDE_TABLE_API + MID_APPLICATION_M2M, HEADER, postQuery, "{\"agent\":\"" + value.get() + "\",\"application\":\"35aa573fd7802200bdbaee5b5e610375\"}"));
        System.out.println(HttpService.instancePostCall(instanceModel, GLIDE_TABLE_API + MID_IP_RANGE_M2M, HEADER, postQuery, "{\"agent\":\"" + value.get() + "\",\"ip_range\":\"2e0c973fd7802200bdbaee5b5e6103fd\"}"));

        jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_APPLICATION_M2M, postQuery);
        JSONArray jsonArray1 = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_CAPABILITY_M2M, postQuery);
        JSONArray jsonArray2 = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_IP_RANGE_M2M, postQuery);
        if (!jsonArray.isNull(0) && !jsonArray1.isNull(0) && !jsonArray2.isNull(0))
            System.out.println("capability added to instance successful");
        else System.out.println("error adding the capabilities");
    }

    public static void deleteMidInstance(InstanceModel instanceModel, String midName) throws Exception {
        try {
            Map<String, Object> query = new HashMap<>();
            query.put("name", midName);
            checkIfMidDeleted(instanceModel, midName, null);
            JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
            AtomicReference<String> value = new AtomicReference<>("");
            jsonArray.forEach(json -> value.set(((JSONObject) json).getString("sys_id")));
            System.out.println(HttpService.instanceDeleteCall(instanceModel, GLIDE_TABLE_API + MID_NAME + "/" + value.get(), HEADER));
            System.out.println("deletion of instance successful");
        } catch (Exception e) {
            System.out.println("some issues with the instance");
            throw new Exception("Instance issues");
        }
    }

    public static void deleteMidInstances(InstanceModel instanceModel, String midName) throws Exception {
        try {
            Map<String, Object> query = new HashMap<>();
            query.put("name", midName);
            JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
            AtomicReference<String> value = new AtomicReference<>("");
            if (!jsonArray.isNull(0)) {
                jsonArray.forEach(json -> value.set(((JSONObject) json).getString("sys_id")));
                HttpService.instanceDeleteCall(instanceModel, GLIDE_TABLE_API + MID_NAME + "/" + value.get(), HEADER);
            }
            System.out.println("successfully deleted midServer with name: " + midName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkIfMidCreated(InstanceModel instanceModel, String midName, String local) throws Exception {
        System.out.println("inside func");
        int p = local == null ? 5 : 9;
        Map<String, Object> query = new HashMap<>();
        JSONArray jsonArray;
        int i = 0;
        do {
            i++;
            query.put("name", midName);
            query.put("sysparm_fields", "name,user_name,status,sys_id");
            jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
            System.out.println("----------------------------------------------------------------");
            System.out.println("jsonArray::" + jsonArray);
            System.out.println("----------------------------------------------------------------");
            System.out.println("waiting for 15 secs");

            if (jsonArray.isNull(0) || !jsonArray.getJSONObject(0).getString("status").equalsIgnoreCase("up")) {
                TimeUnit.SECONDS.sleep(15);
            }
        } while (jsonArray.isNull(0) || !jsonArray.getJSONObject(0).getString("status").equalsIgnoreCase("up") && i <= p);
        if (i == 3 && jsonArray.isNull(0)) {
            System.out.println("The mid did not reach the instance");
            throw new Exception("The mid did not reach the instance");
        } else {
            System.out.println("The mid is created/present in the instance");
        }
    }

    public static void checkIfMidDeleted(InstanceModel instanceModel, String midName, String local) throws Exception {
        System.out.println("inside func");
        int p = local == null ? 1 : 2;
        Map<String, Object> query = new HashMap<>();
        JSONArray jsonArray;
        int i = 0;
        do {
            i++;
            query.put("name", midName);
            query.put("sysparm_fields", "name,user_name,status,sys_id");
            jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + MID_NAME, query);
            System.out.println("----------------------------------------------------------------");
            System.out.println("jsonArray::" + jsonArray);
            System.out.println("----------------------------------------------------------------");
            System.out.println("waiting for 15 secs");

            if (jsonArray.isNull(0) || !jsonArray.getJSONObject(0).getString("status").equalsIgnoreCase("up")) {
                TimeUnit.SECONDS.sleep(15);
            }
        } while (jsonArray.isNull(0) || !jsonArray.getJSONObject(0).getString("status").equalsIgnoreCase("up") && i <= p);
        if (i == 3 && jsonArray.isNull(0)) {
            System.out.println("The mid did not reach the instance");
            throw new Exception("The mid did not reach the instance");
        } else {
            System.out.println("The mid is created/present in the instance");
        }
    }

    public static void createMidServerUser(InstanceModel instanceModel) throws UnirestException {
        System.out.println("CHECKING IF MIDSERVER USER AND ROLE IS PRESENT OR NOT");
        String sysid_user;
        String payload;
        Map<String, Object> query = new HashMap<>();
        if ((sysid_user = checkMidserverUser(instanceModel, instanceModel.getMidserverName())).isEmpty()) {
            query.put("sysparm_input_display_value", "true");
            query.put("sysparm_fields", "sys_id,user_password,user_name");
            payload = "{\"user_name\":\"" + instanceModel.getMidserverName() + "\",\"user_password\":\"" + instanceModel.getMidserverPassword() + "\"}";
            JSONArray jsonArray = HttpService.instancePostJsonCall(instanceModel, GLIDE_TABLE_API + SYSTEM_USER, HEADER, payload, query);
            JSONObject jsonObject = jsonArray.getJSONObject(0).getJSONObject("result");
            if (jsonObject.has("sys_id")) {
                sysid_user = jsonObject.getString("sys_id");
            }
            payload = "{\"role\":\"e76b74ba0ab3015700a3263b26e5e9d5\",\"user\":\"" + sysid_user + "\"}";
            HttpService.instancePostJsonCall(instanceModel, GLIDE_TABLE_API + SYSTEM_USER_ROLE, HEADER, payload, null);
            System.out.println("sys id of the the user" + instanceModel.getMidserverName() + " :- " + sysid_user);
        }

    }

    public static String checkMidserverUser(InstanceModel instanceModel, String midName) throws UnirestException {
        Map<String, Object> query = new HashMap<>();
        query.put("user_name", midName);
        query.put("sysparm_fields", "sys_id");
        JSONArray jsonArray = HttpService.instanceGetCall(instanceModel, GLIDE_TABLE_API + SYSTEM_USER, query);
        if (jsonArray.isNull(0)) return "";
        else return jsonArray.getJSONObject(0).getString("sys_id");
    }


    public static void main(String[] args) throws Exception {
        ServerModel serverModel = ServerModel.builder()
                .command("ls")
                .ipAddress("44.197.89.97")
                .serverUsername("ruty")
                .pemFilePath("/Users/subramanya.ganesh/Documents/OracleMid/rnd_ruty_id_rsa.pem")
                .build();
        new ServerUtilService().createServerSession(serverModel);

    }
}