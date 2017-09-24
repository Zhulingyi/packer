package com.test.project03;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.trace.TraceLocation;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.test.project03.OtherActivity.MallActivity;
import com.test.project03.OtherActivity.TaskActivity;
import com.test.project03.OtherActivity.ThemeActivity;
import com.test.project03.OtherActivity.WalletActivity;
import com.test.project03.QR_code.QRActivity;
import com.test.project03.recoder.PathRecord;
import com.test.project03.recoder.Util;
import com.test.project03.recoder.WalkRouteOverlay;
import com.test.project03.recoder.mapUtil;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;

public class MainActivity extends CheckPermissionsActivity implements CompoundButton.OnCheckedChangeListener,
        LocationSource, AMapLocationListener,RouteSearch.OnRouteSearchListener {
    private SlidingMenu slidingMenu;
    private MapView mapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption clientOption;
    private PolylineOptions mPolyoptions;
    private PathRecord record;
    private int tracesize = 30;
    private long mStartTime;
    private long mEndTime;
    private List<LatLng> mOriginLatLngList;
    private ToggleButton mcontrol;
    private ImageView img_msearch;
    private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();

    private ProgressDialog progDialog = null;

    private PolylineOptions mPolylineOptions;
    private RouteSearch mRouteSearch;
    private double StartLatitude;//开始位置纬度
    private double StartLongitude;//开始位置经度
    private double EndLatitude;//终点纬度
    private double EndLongitude;//终点经度
    private final int ROUTE_TYPE_WALK = 3;

    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
//    private LatLonPoint mStartPoint=new LatLonPoint(StartLatitude,StartLongitude);
//    private LatLonPoint mEndPoint = new LatLonPoint(EndLatitude, EndLongitude);

    mapUtil mapSetUp=new mapUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_map);
        getSupportActionBar().hide();
        Bmob.initialize(this,"b79f26077b57f9a2e2fd7a66c95603a4");

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        mcontrol= (ToggleButton) findViewById(R.id.control);
        initview(savedInstanceState);
        initpolyline();
        initRoute();
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
        mcontrol.setOnClickListener(mcontrolListener);

                //替换主界面内容
//        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new MapFragment()).commit();
        slidingMenu=new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);//菜单靠左
//        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//全屏支持触摸拖拉
        slidingMenu.setBehindOffset(150);//SlidingMenu划出时主页面显示的剩余宽度
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//不包含ActionBar
        slidingMenu.setMenu(R.layout.left_content);

        ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);//menu按钮
        TextView tx_theme= (TextView) findViewById(R.id.theme);//主题
        TextView tx_task= (TextView) findViewById(R.id.task);//任务
        TextView tx_wallet= (TextView) findViewById(R.id.wallet);//钱包
        TextView tx_Mall= (TextView) findViewById(R.id.Mall);//商城
//        TextView tx_map= (TextView) findViewById(R.id.map);//地图
        //搜索
        img_msearch= (ImageView) findViewById(R.id.scan);
        img_msearch.setOnClickListener(scanClick);

        menuImg.setOnClickListener(onclick);

        //菜单跳转
        /**
         * 查看当前主题
         */
        tx_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,ThemeActivity.class);
                startActivity(intent);
            }
        });
        /**
         * 任务
         */
        tx_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,TaskActivity.class);
                startActivity(intent);
//                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new TaskActivity()).commit();
//                slidingMenu.toggle();
            }
        });
        /**
         * 地图
         */
//        tx_map.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new MapFragment()).commit();
//                slidingMenu.toggle();
//            }
//        });
        /**
         * 钱包
         */
        tx_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,WalletActivity.class);
                startActivity(intent);
//                getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new WalletActivity()).commit();
//                slidingMenu.toggle();
            }
        });

        /**
         * 商城
         */
        tx_Mall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,MallActivity.class);
                startActivity(intent);
//                getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new MallActivity()).commit();
//                slidingMenu.toggle();
            }
        });

    }
    /**
     * 初始化视图
     * @param savedInstanceState
     */
    private void initview( Bundle savedInstanceState ){
        mapView= (MapView)findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        if (aMap==null)
        {
            aMap=mapView.getMap();
            setUpMap();
        }
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
    }

    /**
     * 初始化路径规划
     */
    private void initRoute(){
        mRouteSearch=new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        //初始化路径规划线段属性
        mPolylineOptions = null;
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.parseColor("#6db74d")).width(18f);
    }
    /**
     * 初始化轨迹线
     */
    private void initpolyline() {
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(15f);
        mPolyoptions.color(Color.GREEN);
    }
    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        UiSettings settings =  aMap.getUiSettings();
        //设置定位监听
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        settings.setLogoPosition(9000);//高德地图标志隐藏
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }
    /**
     * 开始运动按钮
     * 记录运动路线
     */
    View.OnClickListener mcontrolListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mcontrol.isChecked()){//开始运动
                aMap.clear(true);
                if(record!=null){
                    record=null;
                }
                record=new PathRecord();
                mStartTime= System.currentTimeMillis();
                record.setDate(mapSetUp.getcuDate(mStartTime));
            }else{//停止运动
                mEndTime=System.currentTimeMillis();
                mapSetUp.saveRecord(MainActivity.this,record.getPathline(), record.getDate(),mStartTime,mEndTime);//保存轨迹
                /*显示原始轨迹*/
                AMapLocation startLoc = record.getStartpoint();
                AMapLocation endLoc = record.getEndpoint();
                Log.e("getPathline", String.valueOf(record.getPathline()));
                Log.e("startLoc",String.valueOf(startLoc));
                Log.e("endloc",String.valueOf(endLoc));
                if (record.getPathline() == null || startLoc == null || endLoc == null) {
//                    Toast.makeText(getContext(), "没有记录到轨迹", Toast.LENGTH_SHORT).show();
//                    return;
                }else{
                    LatLng startLatLng = new LatLng(startLoc.getLatitude(),startLoc.getLongitude());
                    LatLng endLatLng = new LatLng(endLoc.getLatitude(), endLoc.getLongitude());
                    mOriginLatLngList = Util.parseLatLngList(record.getPathline());
                   mapSetUp.showOriginTrace(aMap,startLatLng,endLatLng,mOriginLatLngList);
                }
            }
        }
    };

    /**
     * 侧边菜单栏
     */
    View.OnClickListener onclick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.title_bar_menu_btn:
                    slidingMenu.toggle();
                    break;
            }
        }
    };
    /**
     * 进入二维码扫描部分
     */
    View.OnClickListener scanClick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(MainActivity.this, QRActivity.class);
            startActivity(intent);
        }
    };


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" );
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }
    /**
     * 菜单键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //实现按下手机Menu键弹出和关闭侧滑菜单
        if(keyCode==KeyEvent.KEYCODE_MENU){
            slidingMenu.toggle();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if(isChecked){
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        }
        else {
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
    }


    /**
     * 位置变化
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (mListener != null&&aMapLocation != null) {
            if (aMapLocation != null &&aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                LatLng mylocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
//                city=aMapLocation.getCity().substring(0,2);//株洲市
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                StartLatitude=aMapLocation.getLatitude();//纬度
                StartLongitude=aMapLocation.getLongitude();//经度
                /*开始运动按钮*/
                if(mcontrol.isChecked()){
                    record.addpoint(aMapLocation);
                    mPolyoptions.add(mylocation);
                    mTracelocationlist.add(Util.parseTraceLocation(aMapLocation));
                    mapSetUp.redrawline(aMap,mPolyoptions);//实时轨迹
                    if (mTracelocationlist.size() > tracesize - 1) {
                    mapSetUp.trace(mTracelocationlist);
                    }
                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    /**
     * 激活定位
     * @param listener
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener=listener;
        if(locationClient==null){
            aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            locationClient=new AMapLocationClient(this);
            clientOption=new AMapLocationClientOption();
            locationClient.setLocationListener(this);//定位监听
            clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//高精度定位
            clientOption.setInterval(2000);
            // 设置定位参数
            locationClient.setLocationOption(clientOption);
            clientOption.setOnceLocationLatest(true);//设置单次精确定位
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            locationClient.startLocation();
        }

    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener=null;
        if(locationClient!=null){
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient=null;
    }

    /**
     * 必须重写以下方法
     */
    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(locationClient!=null){
            locationClient.onDestroy();
        }
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            Toast.makeText(this, "定位中，稍后再试...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEndPoint == null) {
            Toast.makeText(this, "终点未设置", Toast.LENGTH_SHORT).show();
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }
    /**
     * 步行路线规划
     * @param busRouteResult
     * @param i
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }


    /**
     * 获取算路结果
     * @param result
     * @param errorCode
     */
    private WalkRouteResult mWalkRouteResult;
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
//                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
//                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
//                    String des = mapUtil.getFriendlyTime(dur)+"("+mapUtil.getFriendlyLength(dis)+")";
//                    mRotueTimeDes.setText(des);
//                    mRouteDetailDes.setVisibility(View.GONE);
                } else if (result != null && result.getPaths() == null) {
                    Toast.makeText(this, "没有搜索到相关数据", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "没有搜索到相关数据", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, errorCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

}
