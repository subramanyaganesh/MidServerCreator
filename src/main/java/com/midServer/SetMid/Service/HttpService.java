package com.midServer.SetMid.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.midServer.SetMid.Model.InstanceModel;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HttpService {
    public static JSONArray instanceGetCall(InstanceModel instanceModel, String urlPath, Map<String, Object> query) throws UnirestException {
        HttpResponse<JsonNode> response =
                Unirest.get(instanceModel.getInstanceUrl() + urlPath)
                        .basicAuth(instanceModel.getInstanceUsername(), instanceModel.getPassword())
                        .queryString(query)
                        .asJson();
        if (Integer.toString(response.getStatus()).startsWith("20"))
            return response.getBody().getObject().getJSONArray("result");
        else {
            if (response.getStatus() == 401) {
                throw new UnirestException(response.getStatusText());
            }
            if (Integer.toString(response.getStatus()).startsWith("40") || Integer.toString(response.getStatus()).startsWith("50")) {
                throw new UnirestException(response.getStatusText());
            }
            return response.getBody().getArray();

        }

    }

    public static String instancePostCall(InstanceModel instanceModel, String urlPath, Map<String, String> header,Map<String,Object> query, String body) throws UnirestException {
        HttpResponse<String> response =
                Unirest.post(instanceModel.getInstanceUrl() + urlPath)
                        .basicAuth(instanceModel.getInstanceUsername(), instanceModel.getPassword())
                        .headers(header)
                        .queryString(query)
                        .body(body)
                        .asString();
        return getString(response);
    }

    public static JSONArray instancePostJsonCall(InstanceModel instanceModel, String urlPath, Map<String, String> header, String body,Map<String,Object> query) throws UnirestException {
        HttpResponse<JsonNode> response =
                Unirest.post(instanceModel.getInstanceUrl() + urlPath)
                        .basicAuth(instanceModel.getInstanceUsername(), instanceModel.getPassword())
                        .queryString(query)
                        .headers(header)
                        .body(body)
                        .asJson();
        return response.getBody().getArray();
    }

    public static String instancePutCall(InstanceModel instanceModel, String urlPath, Map<String, String> header,Map<String,Object> query, String body) throws UnirestException {
        HttpResponse<String> response =
                Unirest.put(instanceModel.getInstanceUrl() + urlPath)
                        .basicAuth(instanceModel.getInstanceUsername(), instanceModel.getPassword())
                        .headers(header)
                        .queryString(query)
                        .body(body)
                        .asString();
        return getString(response);
    }

    public static String instanceDeleteCall(InstanceModel instanceModel, String urlPath, Map<String, String> header) throws UnirestException {
        HttpResponse<String> response =
                Unirest.delete(instanceModel.getInstanceUrl() + urlPath)
                        .basicAuth(instanceModel.getInstanceUsername(), instanceModel.getPassword())
                        .headers(header)
                        .asString();
        return getString(response);
    }

    private static String getString(HttpResponse<String> response) throws UnirestException {
        if (Integer.toString(response.getStatus()).startsWith("20"))
            return response.getBody();
        else {
            if (Integer.toString(response.getStatus()).startsWith("40") || Integer.toString(response.getStatus()).startsWith("50")) {
                throw new UnirestException(response.getStatusText());
            }
            return null;
        }
    }


}
