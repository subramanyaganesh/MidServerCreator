package com.midServer.SetMid.Service;

public class bbbb {

     public static void main(String[] args) {
         String a="india is my country";
         String[] p=a.split(" ");
         a=a.replace(" ","");
         int prevLength=a.length();
         StringBuilder output= new StringBuilder();
         for (int i = 0; i < p.length; i++) {
             int b=p[i].length();
             output.append(new StringBuilder(a.substring(prevLength - b, prevLength)).reverse()).append(" ");
             prevLength=prevLength-b;

         }
         System.out.println(output);

     }
}
