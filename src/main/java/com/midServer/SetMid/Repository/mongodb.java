package com.midServer.SetMid.Repository;

import com.midServer.SetMid.Model.InstanceModel;
import com.midServer.SetMid.Model.ServerModel;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class mongodb {

    public static MongoDatabase openMongoDB() throws Exception {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        return mongoClient.getDatabase("midDatabase");
    }

    public static List<String> listMongoDB() throws Exception {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        List<String> dbName = new ArrayList<>();
        mongoClient.listDatabaseNames().forEach((Block<? super String>) dbName::add);
        return dbName;
    }

    public static MongoCollection<Document> openMongoCollection() throws Exception {
        return openMongoDB().getCollection("midCollection");
    }

    public static void insertData(InstanceModel instanceModel, ServerModel serverModel,String localPath,String midFileName, String midname, Date date) {
        try {
            Document doc = new Document();
            doc.append("InstanceUrl", instanceModel.getInstanceUrl());
            doc.append("InstanceName", instanceModel.getInstanceName());
            doc.append("InstanceUsername", instanceModel.getInstanceUsername());
            doc.append("Password", instanceModel.getPassword());
            doc.append("MidFileName", midFileName);
            doc.append("MidName", midname);
            doc.append("date", date);
            if (serverModel != null) {
                doc.append("IpAddress", serverModel.getIpAddress());
                doc.append("ServerUsername", serverModel.getServerUsername());
                doc.append("ServerPassword", serverModel.getServerPassword());
            }
            if (localPath!=null){
                doc.append("IpAddress",  Inet4Address.getLocalHost().getHostAddress());
                doc.append("ServerUsername", Inet4Address.getLocalHost().getHostName());
                doc.append("ServerPassword", "N/A");
            }

            openMongoCollection().insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteDB() throws Exception {
        openMongoDB().drop();
    }

    public static List<Document> listCollection() throws Exception {
        List<Document> list = new ArrayList<>();
        openMongoCollection().find().forEach((Consumer<? super Document>) list::add);
        return list;

    }

    public static void deleteCollection(InstanceModel instanceModel) throws Exception {
        listCollection().stream().parallel()
                .filter(document -> document.containsKey("InstanceName") && ((String) document.get("InstanceName")).equalsIgnoreCase(instanceModel.getInstanceName()))
                .forEach(document -> {
                    try {
                        openMongoCollection().deleteMany(document);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
