package com.example.powervision;

import android.os.AsyncTask;
import android.util.Log;

public class CmdAsyncTask extends AsyncTask {

    public int hz;

    public CmdAsyncTask(int hz)
    {
        this.hz=hz;
    }

    @Override
    protected Object doInBackground(Object[] objects){
        SetCPU setCPU = SetCPU.getInstance();
        setCPU.runAllCores(hz);
        return 0;
    }
}