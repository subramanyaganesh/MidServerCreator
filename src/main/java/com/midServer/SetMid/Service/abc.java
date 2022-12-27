package com.midServer.SetMid.Service;

public class abc {
    public static void main(String[] args) {

        int[] a = {-9, 3, 4, 8, -1, 0, 5, 0, -3, -7, -1};
        int temp;
        boolean f = false;
        for (int i = 0; i < a.length; i++) {
            for (int j = i + 1; j < a.length; j++) {
                if (a[i] > a[j]) {
                    temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }

        }
        for (int j : a) {
            System.out.print(j + "\t");
        }

    }
}
