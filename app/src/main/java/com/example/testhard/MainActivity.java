package com.example.testhard;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    static String TAG = "PhoneActivityTAG";
    Activity activity = MainActivity.this;
    TextView tv;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    ArrayList<String> _mst=new ArrayList<>();
    @SuppressLint({"SetTextI18n", "CheckResult"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         tv = (TextView)findViewById(R.id.tvcmd);

        tv.setText("Cores"+"  "+ Runtime.getRuntime().availableProcessors()+"\n\n");
        tv.setText(tv.getText()+"BogoMIPS"+"  "+ getBogoMipsFromCpuInfo()+"\n");
        tv.setText(tv.getText()+"ProcessorName"+"  "+ getCpuName()+"\n\n");
        long FreeSpaceInternal = new File(getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        long TotalSpaceInternal = (new File(getFilesDir().getAbsoluteFile().toString()).getTotalSpace());


        TotalSpaceInternal=TotalSpaceInternal / 1024 / 1024 ;
        FreeSpaceInternal=FreeSpaceInternal / 1024 / 1024 ;
        tv.setText(tv.getText()+"FreeSpaceInMega"+"  "+ FreeSpaceInternal+"\n\n");
        tv.setText(tv.getText()+"TotalSpaceInMega"+"  "+ TotalSpaceInternal+"\n\n");
        tv.setText(tv.getText()+"UsedSpaceInMega"+"  "+ (TotalSpaceInternal-FreeSpaceInternal)+"\n\n");


        // for Ram
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemory = memoryInfo.totalMem;
        Long availableMemory = memoryInfo.availMem;
        totalMemory=totalMemory / 1024 / 1024 ;
        availableMemory=availableMemory / 1024 / 1024 ;
        tv.setText(tv.getText()+"FreeMemorySpaceInMega"+"  "+ availableMemory+"\n\n");
        tv.setText(tv.getText()+"TotalMemorySpaceInMega"+"  "+ totalMemory+"\n\n");
        tv.setText(tv.getText()+"UsedMemorySpaceInMega"+"  "+ (totalMemory-availableMemory)+"\n\n");
        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            _mst = getPhone();

            for (String op: _mst) {
                Log.i("Device Information", String.valueOf(op));
            }
        }


        String state = Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state))
        {
            /*
            To get the external SD card's available "free" space to show a number which agrees with the
             Menu->Settings->SD card and phone storage's number, use the following code:
             */
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            double sdAvailSize = (double)stat.getAvailableBlocks()
                    * (double)stat.getBlockSize();
//One binary gigabyte equals 1,073,741,824 bytes.
            double gigaAvailable = sdAvailSize / 1073741824;

            //Here is how you get external storage sizes (SD card size):
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = statFs.getBlockSize();
            long totalSize = statFs.getBlockCount()*blockSize;
            long availableSize = statFs.getAvailableBlocks()*blockSize;
            long freeSize = statFs.getFreeBlocks()*blockSize;
        }
        else if (android.os.Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            // We can only read the media\
            tv.setText(tv.getText()+"UsedMemorySpaceInMega"+"  "+ (totalMemory-availableMemory)+"\n\n");

        }
        else
        {
            // No external media
            tv.setText(tv.getText()+"UsedMemorySpaceInMega"+"  "+ (totalMemory-availableMemory)+"\n\n");

        }

        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;

        int height = displayMetrics.heightPixels;
        float density=  getResources().getDisplayMetrics().density;
        tv.setText(tv.getText()+"density"+"  "+ density+"\n\n");
        tv.setText(tv.getText()+"densityDpi"+"  "+ getResources().getDisplayMetrics().densityDpi+"\n\n");
        tv.setText(tv.getText()+"scaledDensity"+"  "+ getResources().getDisplayMetrics().scaledDensity+"\n\n");
        tv.setText(tv.getText()+"widthPixels"+"  "+ getResources().getDisplayMetrics().widthPixels+"\n\n");
        tv.setText(tv.getText()+"heightPixels"+"  "+ getResources().getDisplayMetrics().heightPixels+"\n\n");

    }

    @SuppressLint({"SetTextI18n", "HardwareIds"})
    @TargetApi(Build.VERSION_CODES.O)
    private ArrayList<String> getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        ArrayList<String> _lst =new ArrayList<>();
        _lst.add(String.valueOf(phoneMgr.getCallState()));
        _lst.add("SIM OPERATOR NAME :-"+phoneMgr.getSimOperatorName()); //etisalat
        tv.setText(tv.getText()+"SIM OPERATOR NAME"+"  "+ phoneMgr.getSimOperatorName()+"\n\n");

        _lst.add("SIM STATE :-"+ phoneMgr.getSimState()); //5
        tv.setText(tv.getText()+"SIM STATE"+"  "+ phoneMgr.getSimState()+"\n\n");

        _lst.add("COUNTRY ISO :-"+phoneMgr.getSimCountryIso());//eg

        tv.setText(tv.getText()+"COUNTRY ISO "+"  "+ phoneMgr.getSimCountryIso()+"\n\n");

        String fingerPrint=Build.FINGERPRINT;
        tv.setText(tv.getText()+"fingerPrint "+"  "+fingerPrint+"\n\n");

        String deviceName=getDeviceName();

        tv.setText(tv.getText()+"deviceName "+"  "+deviceName+"\n\n");

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = "";
        if(wifiInfo!=null)
             macAddress = wifiInfo.getMacAddress();

        tv.setText(tv.getText()+"mac Address"+"  "+macAddress+"\n\n");

        String backCameraId = null;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId:manager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if(facing== CameraMetadata.LENS_FACING_BACK) {
                    backCameraId = cameraId;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        int numberOfCameras = Camera.getNumberOfCameras();

        Log.d(TAG, "back camera exists ? "+numberOfCameras);
        tv.setText(tv.getText()+"Number of Cameras "+"  "+numberOfCameras+"\n\n");

       boolean b=getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        tv.setText(tv.getText()+"Camera has flash"+"  "+b+"\n\n");

        Log.d(TAG, "back camera id  :"+backCameraId);

        return _lst;
    }

    @SuppressLint("SetTextI18n")
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        tv.setText(tv.getText()+"manufacturer"+"  "+manufacturer+"\n\n");

        String model = Build.MODEL;
        tv.setText(tv.getText()+"model"+"  "+model+"\n\n");

        int version = Build.VERSION.SDK_INT;
        tv.setText(tv.getText()+"android version "+"  "+version+"\n\n");
        String versionRelease = Build.VERSION.RELEASE;
        tv.setText(tv.getText()+"versionRelease "+"  "+versionRelease+"\n\n");
        tv.setText(tv.getText()+"Brand  "+"  "+Build.BRAND+"\n\n");
        tv.setText(tv.getText()+"Serial  "+"  "+Build.SERIAL+"\n\n");

        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Phone number: " + getPhone());
                } else {
                    Toast.makeText(activity,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void getCpuInfo() {
        try {
            Process proc = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            InputStream is = proc.getInputStream();
            TextView tv = (TextView)findViewById(R.id.tvcmd);
            tv.setText(getStringFromInputStream(is));

        }
        catch (IOException e) {
            Log.e(TAG, "------ getCpuInfo " + e.getMessage());
        }
    }


    @SuppressLint("SetTextI18n")
    public void getMemoryInfo() {
        try {
            Process proc = Runtime.getRuntime().exec("cat /proc/meminfo");
            InputStream is = proc.getInputStream();
            TextView tv = (TextView)findViewById(R.id.tvcmd);
            tv.setText(tv.getText()+"\n\n"+getStringFromInputStream(is));
        }
        catch (IOException e) {
            Log.e(TAG, "------ getMemoryInfo " + e.getMessage());
        }
    }

    private static String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try {
            while((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        }
        catch (IOException e) {
            Log.e(TAG, "------ getStringFromInputStream " + e.getMessage());
        }
        finally {
            if(br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "------ getStringFromInputStream " + e.getMessage());
                }
            }
        }


        return sb.toString();
    }

    public String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            text = br.readLine();
            br.close();
            String[] array = text.split(":\\s+", 2);
            if (array.length >= 2) {
                return array[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getBogoMipsFromCpuInfo(){
        String result = null;
        String cpuInfo = readCPUinfo();
        String[] cpuInfoArray =cpuInfo.split(":");
        for( int i = 0 ; i< cpuInfoArray.length;i++){
            if(cpuInfoArray[i].contains("BogoMIPS")){
                result = cpuInfoArray[i+1];
                break;
            }
        }
        if(result != null) result = result.trim().replace("Features","");
        return result;
    }
    public static String readCPUinfo()
    {
        ProcessBuilder cmd;
        String result="";
        InputStream in = null;
        try{
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            in = process.getInputStream();
            byte[] re = new byte[1024];
            while(in.read(re) != -1){
                System.out.println(new String(re));
                result = result + new String(re);
            }
        } catch(IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                if(in !=null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }





//
//        final Runtime runtime = Runtime.getRuntime();
//        final long usedMemInMB=((runtime.totalMemory() - runtime.freeMemory()) / 1048576L);
//        final long maxHeapSizeInMB=(runtime.maxMemory() / 1048576L);
//        final long availHeapSizeInMB = (maxHeapSizeInMB - usedMemInMB);
//
//        tv.setText(tv.getText()+"\n\n"+usedMemInMB);
//        tv.setText(tv.getText()+"\n\n"+maxHeapSizeInMB);
//        tv.setText(tv.getText()+"\n\n"+availHeapSizeInMB);

    // getCpuInfo();
//        getMemoryInfo();
//        getSTate();
    //tv.setText(getCpuNameFromCpuInfo());

//        Callable<String> getBogoMipsFromCpuInfo = MainActivity::getBogoMipsFromCpuInfo;
//        Observable<String> booleanObservable=Observable.create(emitter ->
//        {
//            emitter.onNext(getBogoMipsFromCpuInfo());
//            emitter.onComplete();
//        });
//        booleanObservable
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    tv.setText(tv.getText()+"BogoMIPS"+"  "+result+"\n\n");
//                }, error -> Log.e("error", Objects.requireNonNull(error.getMessage())));

//
//         Observable.fromCallable(getBogoMipsFromCpuInfo)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    tv.setText(tv.getText()+"BogoMIPS"+"  "+result+"\n\n");
//                }, error -> Log.e("error", Objects.requireNonNull(error.getMessage())));
}