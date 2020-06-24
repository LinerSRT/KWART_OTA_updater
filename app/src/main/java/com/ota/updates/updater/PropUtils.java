package com.ota.updates.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PropUtils {
    public static String get(String name) {
        Process p;
        String result = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", name).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line=br.readLine()) != null) {
                result = line;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
