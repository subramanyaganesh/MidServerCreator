package com.midServer.SetMid.Service;

import com.midServer.SetMid.Model.SQLDBModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Map;

public class shellUtils {
    public static void executeShellCommand(String... s) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        String command = s[0];
        pb.command("sh", "-c", command);
        Process process = pb.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        System.out.println("The output of the process ::\n" + command + "\n is::");
        bufferedReader.lines().forEach(System.out::println);
        System.out.println("============Errors=========================");
        bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        bufferedReader.lines().forEach(System.out::println);
        while (process.isAlive()) {
            //empty body
        }
        System.out.println(process.exitValue());

        System.out.println("============================================");
    }

    public static StringBuilder connectDatabase(Map.Entry<String, String> entry, SQLDBModel dbModel) throws SQLException, ClassNotFoundException {
        try {
            StringBuilder sb=new StringBuilder();
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + entry.getKey(), dbModel.getDatabaseUsername(), dbModel.getDatabasePassword());
            Statement stmt = con.createStatement();
            System.out.println("Executing the command : " + entry.getValue());
            try (ResultSet rs = stmt.executeQuery(entry.getValue())) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    sb.append(rs.getString(1));
                    System.out.println(rs.getString(1));
                }
                System.out.println("\tSUCCESSFULLY EXECUTED THE COMMAND");
                System.out.println("\tThe total number of elements :: " + count);
                return sb;
            } catch (SQLException throwables) {
                System.out.println("The error is " + throwables.getLocalizedMessage() + " the sql code is " + throwables.getSQLState());
                System.out.println("Retry to Execute command : " + entry.getValue());
                stmt.execute(entry.getValue());
                System.out.println("\tSUCCESSFULLY EXECUTED THE COMMAND");
                System.out.println("---------------------------------------------------");
            }
            System.out.println("---------------------------------------------------");
            con.close();
        } catch (SQLException throwables) {
            System.out.println("The error is " + throwables.getLocalizedMessage() + " the sql code is " + throwables.getSQLState());
            throw throwables;
        }
        return new StringBuilder("NO RETURN VALUE");
    }
}
