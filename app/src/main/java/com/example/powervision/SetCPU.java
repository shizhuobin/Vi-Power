package com.example.powervision;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class SetCPU {
    private static String TAG = "SetCPU";

    public static Integer one = 1;
    public static Integer zero = 0;
    public static Integer sync= one;

    private RootStream stream;

    private static class SetCPUHolder{
        private static SetCPU setCPU = new SetCPU();
    }

    public static SetCPU getInstance(){
        return SetCPUHolder.setCPU;
    }

    private SetCPU(){
        try{
            stream = new RootStream();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void runAllCores(int hz) {
        int cores = 4;
        try {
            this.stream.writeLine("stop mpdecision\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < cores; i++) {
            try {
                this.stream.writeLine("chmod 777 /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_max_freq"+ "\n");
                this.stream.writeLine("chmod 777 /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_min_freq"+ "\n");
                this.stream.writeLine("echo 300000 > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_min_freq"+ "\n");
                this.stream.writeLine("echo "+hz+" > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_max_freq"+ "\n");

                int result = 0;
                int tag=0;
                for(;;) {
                    try {
                        String line;
                        BufferedReader br = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_max_freq"));
                        if ((line = br.readLine()) != null) {
                            result = Integer.parseInt(line);
                        }
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result==hz||result==0)
                        break;
                    else
                        this.stream.writeLine("echo "+hz+" > /sys/devices/system/cpu/cpu"+i+"/cpufreq/scaling_max_freq"+ "\n");
                    tag++;
                }
                Log.d(TAG,"cpu"+i+" "+result+" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class RootStream{
        Process process;

        DataInputStream in;
        DataOutputStream os;
        BufferedOutputStream bos;

        public RootStream() throws IOException{
            this.process = Runtime.getRuntime().exec("su");
            Log.i(TAG, "RootStream: p:"+process);
            this.os = new DataOutputStream(this.process.getOutputStream());
            this.bos = new BufferedOutputStream(this.os);
        }

        public synchronized void writeLine(String command) throws IOException {
            this.bos.write(command.getBytes());
            this.bos.flush();
        }

        public synchronized void flush() throws IOException {
            this.bos.flush();
        }

    }
}


