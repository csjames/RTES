package com.example.james.avrdude;

import android.content.Context;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by james on 03/11/16.
 */

public class DudeHandler {

    private Context c;

    public String programmer, chip;
    private String pathToAvrdude = "/data/data/com.example.james.avrdude/lib/libavrdude.so";
    private String fuseString = "%s:w:0x%s:m";
    interface USBCallback {
        void outputOnly(boolean success, String output);
        void fuses(boolean success, String output, String fuses);
    }

    public DudeHandler(Context c){

    }

    public synchronized void setFuses(String hfuse, String lfuse, String efuse, USBCallback c){
        ArrayList<String> commands = new ArrayList<>();
        commands.add("su"); commands.add("&&"); commands.add(pathToAvrdude);
        commands.add("-C"); commands.add("/sdcard/avrdude.conf");
        commands.add("-p"); commands.add(chip);
        commands.add("-c"); commands.add(programmer);

        if(hfuse != null){
            commands.add("-U");
            commands.add(String.format(fuseString,"hfuse",hfuse));
        }

        if(lfuse != null){
            commands.add("-U");
            commands.add(String.format(fuseString,"lfuse",lfuse));
        }

        if(efuse != null){
            commands.add("-U");
            commands.add(String.format(fuseString,"efuse",efuse));
        }

        String[] cmdArr = new String[commands.size()];

        try {
            Process process = null;
            process = Runtime.getRuntime().exec(
                   commands.toArray(cmdArr));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[200];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            process.waitFor();

            System.out.println("OUTPUT " + output.toString());

            c.outputOnly((process.exitValue() == 0) && (output.toString().contains("signature"))
                    ,output.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public synchronized void readFuses(USBCallback c){
        ArrayList<String> commands = new ArrayList<>();

        commands.add("su"); commands.add("&&"); commands.add(pathToAvrdude);
        commands.add("-C"); commands.add("/sdcard/avrdude.conf");
        commands.add("-p"); commands.add(chip);
        commands.add("-c"); commands.add(programmer);
        commands.add("-U"); commands.add("lfuse:r:/sdcard/lf.txt:h");
        commands.add("-U"); commands.add("hfuse:r:/sdcard/hf.txt:h");
        commands.add("-U"); commands.add("efuse:r:/sdcard/ef.txt:h");

        try {
            String[] s = new String[commands.size()];
            Process process = Runtime.getRuntime().exec(
                   commands.toArray(s));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[200];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }

            reader.close();

            process.waitFor();

            String lfuse = readFuseFile("/sdcard/lf.txt");
            String hfuse = readFuseFile("/sdcard/hf.txt");
            String efuse = readFuseFile("/sdcard/ef.txt");

            c.fuses((process.exitValue() == 0) && (output.toString().contains("signature")),
                    output.toString(), lfuse + ":" + hfuse + ":" + efuse);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String flashString = "flash:w:%s";

    public synchronized void flashHex(String pathToHex,USBCallback c){
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[]{
                            "su","&&",
                            pathToAvrdude,"-C", "/sdcard/avrdude.conf"
                            ,"-p",chip
                            ,"-c",programmer
                            ,"-U", String.format(flashString, pathToHex)
                    });

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[200];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            process.waitFor();

            System.out.println("OUTPUT " + output.toString());

            c.outputOnly((process.exitValue() == 0) && (output.toString().contains("signature"))
                    ,output.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void testConnection(USBCallback c){
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[]{
                            "su","&&",
                            pathToAvrdude,"-C", "/sdcard/avrdude.conf"
                            ,"-p",chip
                            ,"-c",programmer
                            ,"-v"
                    });

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[200];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            process.waitFor();

            System.out.println("OUTPUT " + output.toString());

            c.outputOnly((process.exitValue() == 0) && (output.toString().contains("signature"))
                    ,output.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String readFuseFile(String path){
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String fuse = sb.toString();
            return fuse;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
