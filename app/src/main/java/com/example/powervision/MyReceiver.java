package com.example.powervision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class MyReceiver extends BroadcastReceiver {

    private static String TAG = "MyReceiver";

    private final String apiEndpoint = "https://vi-power.cognitiveservices.azure.cn/face/v1.0/"; //https://fer-service.cognitiveservices.azure.com/face/v1.0/
    private final String subscriptionKey = "a2f847cb765f44f592077fa31803f36d";
    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);
    private String name;
    private String path;

    @Override
    public void onReceive(Context context, Intent intent) {
        name=intent.getStringExtra("name_save");
        path=intent.getStringExtra("path_save");

        uploadPicture();
    }

    public void uploadPicture()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String file1=path+"/"+name;
                String jpgname=name;
                String jpgpath=path;

                //读取照片
                FileInputStream fs = null;
                ByteArrayInputStream inputStream=null;
                Bitmap bitmap=null;
                ByteArrayOutputStream outputStream=null;
                try {
                    fs = new FileInputStream(new File(file1));
                    bitmap  = BitmapFactory.decodeStream(fs);
                    outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                    inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //将照片上传
                if (fs != null) {
                    Face[] result = new Face[0];
                    try {
                        synchronized (faceServiceClient) {
                            result = faceServiceClient.detect(
                                    inputStream,
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    new FaceServiceClient.FaceAttributeType[]{
                                            FaceServiceClient.FaceAttributeType.Emotion,
                                    }
                            );
                        }
                        inputStream.close();
                        fs.close();
                    } catch (ClientException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(result.length==0) //识别不了情绪，将图片翻转再上传一次
                    {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(),  bitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream outputStream2= new ByteArrayOutputStream();
                        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, outputStream2);

                        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(outputStream2.toByteArray());
                        result = new Face[0];
                        try {
                            synchronized (faceServiceClient) {
                                result = faceServiceClient.detect(
                                        inputStream2,
                                        true,         // returnFaceId
                                        false,        // returnFaceLandmarks
                                        //null          // returnFaceAttributes:
                                        new FaceServiceClient.FaceAttributeType[]{
                                                FaceServiceClient.FaceAttributeType.Emotion,
                                        }
                                );
                            }
                            inputStream2.close();
                        } catch (ClientException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(result.length!=0) {
                        Emotion emotion = result[0].faceAttributes.emotion;
                        String s=jpgname
                                +" anger:"+emotion.anger
                                +" contempt:"+emotion.contempt
                                +" disgust:"+emotion.disgust
                                +" fear:"+emotion.fear
                                +" happiness:"+emotion.happiness
                                +" neutral:"+emotion.neutral
                                +" sadness:"+emotion.sadness
                                +" surprise:"+emotion.surprise
                                +"\n";
                        Log.d(TAG,s);

                        //保存情绪信息
                        saveEmotion(s,jpgpath);
                    }
                    else
                    {
                        saveEmotion(jpgname+" not find face\n",jpgpath);
                    }
                }
            }
        }).start();
    }

    public void saveEmotion(String s, String path)
    {
        //保存情绪信息
        FileWriter fileWritter = null;
        try {
            fileWritter = new FileWriter(path+".txt",true);
            fileWritter.write(s);
            fileWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
