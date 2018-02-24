package aka.capstonedesign;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;

public class WalkActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.CurrentLocationEventListener {

    private final static String DAUM_API_KEY = "f3b18f2e7e9ca12e193a59af6decb76c"; //RestApI

    ImageButton close;
    Button btnStart, btnStop,btnSave;
    TextView txtLong;
    private Chronometer mChronometer;

    static LinearLayout container;

    //Intent
    String getTime;
    String WalkTime;
    String saveDate;

    //LocationManager manager;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mFirebaseUser;


    private static final String TAG = WalkActivity.class.getName();
    private static final int REQUEST_CODE = 100;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_walk);

            //파이어베이스 유저인증,데이터베이스 인스턴스 생성
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            //아이디 찾기
            btnStart = (Button) findViewById(R.id.btn_start);
            btnStop = (Button) findViewById(R.id.btn_stop);
            txtLong = (TextView) findViewById(R.id.txt_Long);
            close = (ImageButton) findViewById(R.id.btn_close);
            btnSave = (Button) findViewById(R.id.btnSave);

            mChronometer = (Chronometer) findViewById(R.id.chronometer);
            container = (LinearLayout) findViewById(R.id.map_capture);

            //manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);// 시스템 서비스 참조
            final MapView mapView = new MapView(this);
            mapView.setDaumMapApiKey(DAUM_API_KEY);
            mapView.setMapViewEventListener(this);
            mapView.setCurrentLocationEventListener(this); // 추가
            ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
            mapViewContainer.addView(mapView);

            //-------------------------------------------------------------------sdcard screenshot
        // call for the projection manager
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            // start capture handling thread
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    mHandler = new Handler();
                    Looper.loop();
                }
            }.start();
            //----------------------------end

             btnSave.setEnabled(false); // 안눌림

            //엑스버튼 클릭시
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert_close = new AlertDialog.Builder(WalkActivity.this);
                    alert_close.setMessage("기록은 저장되지 않습니다. \n화면을 종료하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish(); }
                            }).setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return; }
                            });
                    AlertDialog alert = alert_close.create();
                    alert.show();
                }
            });

            //-------------------------------------------------------------------버튼 클릭시
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == btnStart) {

                            //트래킹 시작
                            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                            // 실시간 위치 좌표를 받음
                            startLocationService();
                            //스톱워치
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.start();
                            btnStart.setEnabled(false); // 안눌림
                            btnStart.setFocusable(false);

                    }

                    else if (v == btnStop) {
                        //트래킹모드 종료
                        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);

                        //polyline test
                        mapView.addPolyline(polyline);

                        //스톱워치 종료
                        mChronometer.stop();
                        WalkTime = mChronometer.getText().toString();

                        //날짜 전달 ( 년 - 월 - 일 )
                        long now = System.currentTimeMillis();
                        final Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        saveDate = simpleDateFormat.format(date);
                        getTime = sdf.format(date);

                        //-------------------------------------------------------------------sdcard screenshot
                        startProjection();

                        btnSave.setEnabled(true);
                    }
                    else if(v==btnSave){
                        //팝업창 띄우기
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(WalkActivity.this);
                        alert_confirm.setMessage("저장 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {// 'YES' 저장
                                        AlertDialog.Builder alertmemo = new AlertDialog.Builder(WalkActivity.this);
                                        alertmemo.setTitle("오늘의 산책은 어땠나요?");
                                        final EditText memo = new EditText(WalkActivity.this);
                                        alertmemo.setView(memo);
                                        alertmemo.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                final String Memo = memo.getText().toString();
                                                //map객체에 정보 저장
                                                Map map = new Map();
                                                map.setWalkdate(getTime);
                                                map.setWalktime(WalkTime);
                                                map.setDistance(Math.floor(mergeDistan));
                                                map.setWalkday(saveDate);
                                                map.setMemo(Memo);
                                                java.util.Map<String, Object> mapValues = map.toMap();
                                                mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("Map").child(getTime).setValue(mapValues).addOnSuccessListener(
                                                        WalkActivity.this, new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                finish();
                                                            }
                                                        });
                                            }
                                        });
                                        alertmemo.show();
                                    }
                                }).setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 처음 상태로 돌아감
                                        calDistan = 0.0;
                                        mergeDistan = 0.0;
                                        mChronometer.setBase(SystemClock.elapsedRealtime());
                                        btnStart.setEnabled(true);
                                        return;
                                    }
                                });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                    }
                }//onClick

            }; // OnClickListener

            btnStart.setOnClickListener(clickListener);
            btnStop.setOnClickListener(clickListener);
            btnSave.setOnClickListener(clickListener);


        } // onCreate


        final MapPolyline polyline = new MapPolyline();

        Double latitude, longitude;
        Double lat1, lon1, lat2, lon2;
        Double calDistan = 0.0;
        Double mergeDistan = 0.0;

        //원래는 void 리턴
    public void startLocationService() {
        long minTime = 30000; // 갱신 주기 (1000 = 1초)
        float minDistance = 1; // 위치가 얼마 벗어날때마다 갱신되는지 ..

        //polyline test
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 시스템 서비스 참조
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) { // 위치가 바뀔때

                        if(location!=null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                        if (lat1 == null && lon1 == null && lat2 == null && lon2 == null)  // 둘다 널 일때
                        {
                            lat1 = latitude;
                            lon1 = longitude;
                        } else if (lat1 != null && lon1 != null && lat2 == null && lon2 == null)  // lat2,lon2 가 널일때
                        {
                            lat2 = latitude;
                            lon2 = longitude;
                        } else { // 둘다 널이 아닐때
                            lat1 = lat2;
                            lon1 = lon2;
                            lat2 = latitude;
                            lon2 = longitude;
                            calDistan = distance(lat1, lon1, lat2, lon2, "meter");

                            polylineTest(lat1, lon1); //polyline test

                            if (mergeDistan == null)
                                mergeDistan = calDistan;
                            else
                                mergeDistan = mergeDistan + calDistan;
                        }
// 실시간 거리 표시
                        txtLong.setText(" " + String.valueOf(Math.floor(mergeDistan)) + " m" );
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
                }); // LocationListener

    } // StartLocationService


    public void polylineTest( double lat1 , double lon2) {

        List<Double> latArray = new ArrayList<Double>();
        List<Double> lonArray = new ArrayList<Double>();

        latArray.add(lat1);
        lonArray.add(lon2);

        for ( int i =0; i <latArray.size(); i++) {
            polyline.addPoint(MapPoint.mapPointWithGeoCoord(latArray.get(i),lonArray.get(i)));

        }

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        LocationManager locManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);

        if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //위치권한요청
            chkGpsService();
        }
        else {
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); //트래킹시작
        }
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    // 다음 지도 트래킹모드
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();

        //현재위치값 받기
        double latitude = mapPointGeo.latitude;
        double longitude = mapPointGeo.longitude;

        // 마커 꾸미기
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Start Marker");
        marker.setTag(0);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker);

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    //GPS 설정 체크
    private boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        Log.d(gps, "aaaa");

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }

    //두 좌표값 사이 위치 거리 계산
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }

        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    //------------------------------------------------------------------------------------------------sdcard screenshot
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (sMediaProjection != null) {
                File externalFilesDir = getExternalFilesDir(null);
                if (externalFilesDir != null) {
                    STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
                    File storeDirectory = new File(STORE_DIRECTORY);
                    if (!storeDirectory.exists()) {
                        boolean success = storeDirectory.mkdirs();
                        if (!success) {
                            Log.e(TAG, "failed to create file storage directory.");
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
                    return;
                }

                // display metrics
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDensity = metrics.densityDpi;
                mDisplay = getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
//                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, 2000, Bitmap.Config.ARGB_8888);
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    fos = new FileOutputStream(STORE_DIRECTORY + "/walk_" + getTime + "_"+IMAGES_PRODUCED + ".png"); //myscreen 이름으로 저장됨. STORE_DIRECTORY에
                    //fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png"); //myscreen 이름으로 저장됨. STORE_DIRECTORY에
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    stopProjection();
                    IMAGES_PRODUCED++;
                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);

                    Toast.makeText(WalkActivity.this, "경로 저장", Toast.LENGTH_SHORT).show();

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    } // end ImageAvailableListener

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    /****************************************** Factoring Virtual Display creation ****************/
    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }


} // end class