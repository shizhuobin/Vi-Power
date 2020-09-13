package com.example.powervision;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.util.Log;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

import android.content.Intent;

import static java.lang.Thread.sleep;

public class MyWindow extends LinearLayout implements SurfaceTextureListener {

    private static String TAG = "MyWindow";
    private int delaytime=2000;
    private int pictureWidth=352;
    private int pictureHeight=288;

    private TextureView textureView;
    /**
     * 相机类
     */
    public Camera myCamera;
    private Context context;
    private WindowManager mWindowManager;
    private Bitmap bitmap_get = null;
    public int stoptime=0;


    public MyWindow(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.window, this);
        this.context = context;

        initView();
    }

    private void initView() {
        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (myCamera == null) {
            // 创建Camera实例
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        Log.d("Demo", "tryToOpenCamera"+cameraCount+" "+camIdx);
                        myCamera = Camera.open(camIdx);
                        Camera.Parameters params = myCamera.getParameters();
                        params.setPreviewSize(pictureWidth, pictureHeight);
                        myCamera.setParameters(params);
                    } catch (RuntimeException e) {
                            e.printStackTrace();
                    }
                }
            }

            try {
                // 设置预览在textureView上
                myCamera.setPreviewTexture(surface);
                myCamera.setDisplayOrientation(SetDegree(MyWindow.this));

                // 开始预览
                myCamera.startPreview();
                handler.sendEmptyMessage(BUFFERTAG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setStoptime()
    {
        stoptime=1;
        myCamera.stopPreview();
    }

    int tag;
    private void getPreViewImage() {
        tag=0;
        if (myCamera != null){
            myCamera.setPreviewCallback(new Camera.PreviewCallback(){

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null&&tag==0){
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
                            bitmap_get = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                            bitmap_get = rotateMyBitmap(bitmap_get);
                            stream.close();
                            tag=1;
                        }
                    }catch(Exception ex){
                        Log.e(TAG,"预览"+"Error:"+ex.getMessage());
                    }
                }
            });
        }
    }


    public Bitmap rotateMyBitmap(Bitmap mybmp){
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap nbmp2 = Bitmap.createBitmap(mybmp, 0,0, mybmp.getWidth(),  mybmp.getHeight(), matrix, true);
        saveImage(nbmp2); //保存图片
        return nbmp2;
    };

    public Bitmap bmp_save;
    public String name_save;
    public String path_save;

    public void saveImage(Bitmap bmp) {
        path_save =context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+"/"+MainActivity.getUserName()+"_"+MainActivity.getAppName()+"_"+MainActivity.getScenarioNumber(); //所创建文件目录
        File f = new File(path_save);
        if(!f.exists()){
            f.mkdirs(); //创建目录
        }
        Calendar Cld = Calendar.getInstance();
        int YY = Cld.get(Calendar.YEAR) ;
        int MM = Cld.get(Calendar.MONTH)+1;
        int DD = Cld.get(Calendar.DATE);
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);//毫秒

        name_save =YY+"-"+MM+"-"+DD+" "+HH+":"+mm+":"+SS+"."+MI+".jpg";
        bmp_save=bmp;
        Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fos = null;
                    try {
                        Log.d(TAG,"saveEmotion "+path_save +"/"+ name_save);
                        fos = new FileOutputStream(new File(path_save, name_save));
                        bmp_save.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent( "com.shi.zhu.bin");
                    intent.putExtra("name_save", name_save);
                    intent.putExtra("path_save", path_save);
                    context.sendBroadcast(intent);
                }
            };
        runnable.run();
    }

    public static final int BUFFERTAG = 100;
    private boolean isGetBuffer = true;

    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case BUFFERTAG:
                    if(isGetBuffer){
                        getPreViewImage();
                        handler.sendEmptyMessageDelayed(BUFFERTAG, delaytime);
                    }else{
                        myCamera.setPreviewCallback(null);
                        handler.sendEmptyMessageDelayed(BUFFERTAG, delaytime);
                    }
                    break;
            }
        };
    };


    private int SetDegree(MyWindow myWindow) {
        // 获得手机的方向
        int rotation = mWindowManager.getDefaultDisplay().getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        surface.release();

        myCamera.setPreviewCallback(null);

        myCamera.stopPreview(); //停止预览

        myCamera.lock();

        myCamera.release();     // 释放相机资源
        myCamera = null;

        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}

