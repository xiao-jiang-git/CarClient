package com.xiaojiang.sbu.obu;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private Timer timer;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView showlocation, speedView;
    private ImageView wifiImage;
    private SocketHandler mHandler;
    private double latitude = 0.0;
    private double longtitude = 0.0;
    private int speed = 0;
    private Button start;
    Marker mCurrent;
    private Context context;
    private Boolean connected = false;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiImage = findViewById(R.id.Wifi);
        //SendUtils(context);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mHandler = new SocketHandler();

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(connected){

                }else{
                    Log.e("trying"," ");
                    wifiImage.setBackground(getResources().getDrawable(R.drawable.frame));
                    startFrameAnimation();
                    SendUtils(context);
                }
            }
        },0,3000);


        speedView = findViewById(R.id.speedView);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    private void displaylocation(){
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mCurrent = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).position(new LatLng(latitude, longtitude)).anchor(0.5f,0.5f).rotation(90).title("car"));
                //Move camera
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.0f));
            }
        });

    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        receiveMessage();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        //GoogleApiClient.connect();
    }


    private static StringBuffer mConsoleStr = new StringBuffer();
    //protected BufferedWriter mWriter;//BufferedReader
    private DatagramSocket socket, sendSocket;
    private DatagramPacket packet;


    public void SendUtils (final Context context) {
        final String content = getlocalip();
        //初始化socket
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        Log.e("zziafyc", ""+getlocalip());

        //创建线程发送信息
        new Thread() {
            private byte[] sendBuf;

            public void run() {
                try{
                    Thread.sleep(2000);
                }catch (Exception e ){

                }
                try {
                    sendBuf = content.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    Socket client=new Socket("192.168.137.202",8000);
                    OutputStream out = client.getOutputStream();
                    out.write(sendBuf);
                    out.flush();
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "成功连接服务器！", Toast.LENGTH_LONG);
                    Looper.loop();
                    connected = true;
                    Log.e("zziafyc", "已将内容发送给了：" + "192.168.137.202"+8000);

                } catch (ConnectException e){
                    connected = false;
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "请检查服务器是否开启！！", Toast.LENGTH_LONG).show();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wifiImage.setBackground(getResources().getDrawable(R.drawable.a0));

                        }
                    });
                    Looper.loop();


                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) return "Check the HotSpot";
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    private void receiveMessage() {
        Log.e("zziafyc", "开始receive");
        new Thread() {
            public void run() {
                try {
                    Log.e("zziafyc", "尝试连接");
                    socket = new DatagramSocket(8000);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                Log.e("zziafyc", "成功");

                byte[] receBuf = new byte[1024];
                packet = new DatagramPacket(receBuf, receBuf.length);
                while (true) {
                    try {
                        socket.receive(packet);
                        socket.setSoTimeout(6000);
                        final String receive = new String(packet.getData(), 0, packet.getLength(), "utf-8");
                        wifiImage.setBackground(getResources().getDrawable(R.drawable.a5));
                        mHandler.obtainMessage(0, receive).sendToTarget();
                        connected=true;
                    } catch (SocketTimeoutException e){
                        connected = false;
                        Looper.prepare();
                        Toast.makeText(MainActivity.this, "服务器连接断开请检查！", Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                wifiImage.setBackground(getResources().getDrawable(R.drawable.a0));
                                speedView.setText("m/h");

                            }
                        });

                        Looper.loop();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }





    private AnimationDrawable ad;
    //启动帧动画
    private void startFrameAnimation() {
        //配置文件方式
        ad = (AnimationDrawable) wifiImage.getBackground();


        // 动画是否正在运行
        if (ad.isRunning()) {
            //停止动画播放
            ad.stop();
        } else {
            //开始或者继续动画播放
            ad.start();
        }
    }

    class SocketHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    try {
                        //将handler中发送过来的消息创建json对象
                        JSONObject json = new JSONObject((String)msg.obj);
                        speed = json.getInt("speed");
                        latitude = json.getDouble("latitude");
                        longtitude = json.getDouble("longtitude");
                        mMap.clear();
                        displaylocation();
                        showSpeed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                default:
                    break;
            }
        }
    };


    public void showSpeed(){
        Log.e("speed", ""+speed);
        speedView.setText(""+speed);
        if(speed>=45 && speed<65){
            speedView.setBackground(getResources().getDrawable(R.drawable.warnspeed));
        }else if(speed>=65){
            speedView.setBackground(getResources().getDrawable(R.drawable.stopspeed));
        }else{
            speedView.setBackground(getResources().getDrawable(R.drawable.normalspeed));
        }
    }


        @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private static String getTime(long millTime) {
        Date d = new Date(millTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(d);
    }






}
