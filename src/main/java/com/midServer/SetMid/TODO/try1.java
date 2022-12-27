package com.midServer.SetMid.TODO;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class try1 {

}

    /*public static void main(String[] args) throws Exception {
        String command="mysql -uroot -pAdmin@123456";
        ProcessBuilder pb=new ProcessBuilder();
        pb.command("sh","-c",command);
        Process process=pb.start();
       *//* BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        bufferedWriter.write("");
        bufferedWriter.flush();
        bufferedWriter.close();*//*
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        System.out.println("The command executed here is ::\n" + command );
        bufferedReader.lines().forEach(System.out::println);
        System.out.println("============Errors=========================");
        bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        bufferedReader.lines().forEach(System.out::println);
        System.out.println("============================================");


    }*/
    /*public static void main(String[] args) throws UnirestException {
      *//*  Map<String, String> HEADER = new HashMap<String, String>() {{
            put("Content-Type", "multipart/form-data");
            put("Accept", "application/json");
        }};
        Map<String, Object> fields = new HashMap<>();
        fields.put("table_name", "ecc_agent_jar");
        fields.put("table_sys_id", "ab3b89421b52b010e15eeb90604bcb98");

        InstanceModel instanceModel = InstanceModel.builder()
                .instanceName("test91")
                .instanceUrl("https://test91.service-now.com/")
                .instanceUsername("admin")
                .password("Admin@123")
                .build();*//*
        Unirest.setTimeouts(0, 0);
        Future<HttpResponse<String>> response =
                Unirest.post("https://test91.service-now.com/api/now/attachment/upload")
                        .header("Authorization", "Basic YWRtaW46QWRtaW5AMTIz")
                        .header("Content-Type", "multipart/form-data")
                        .header("Accept", "application/json")
                        .field("table_name", "ecc_agent_jar")
                        .field("table_sys_id", "ab3b89421b52b010e15eeb90604bcb98")
                        .field("file", new File("/Users/subramanya.ganesh/Downloads/Core.dll"))
                        .mode("RFC6532")
                        .asStringAsync();
        System.out.println(response);
    }*/

/*    interface  o{
        void hi();
    }
    abstract class a implements o{
        @Override
        public void hi() {
            System.out.println("bi");
        }
    }
    class k extends a{

    }*/

 /*   public static void main(String[] args) {
        Function<List<Integer>,Integer> f= integers -> {
            AtomicInteger sum= new AtomicInteger();
            integers.forEach(sum::addAndGet);
            return sum.get();
        };
        System.out.println(f.apply(Arrays.asList(1,2,3)));
    }
   }*/
