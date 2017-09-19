package com.test.project03.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.test.project03.CheckPermissionsActivity;
import com.test.project03.R;
import com.test.project03.recoder.DbAdapter;
import com.test.project03.recoder.PathRecord;
import com.test.project03.recoder.Util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.bmob.v3.Bmob.getApplicationContext;


/**
 * 地图模块
 * Created by thinkpad on 2017/9/12.
 */

public  class MapFragment extends Fragment implements CompoundButton.OnCheckedChangeListener,
        LocationSource, AMapLocationListener{
    private MapView mapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption clientOption;

    private ToggleButton mcontrol;
    private PathRecord record;
    private long mStartTime;
    private long mEndTime;
    private DbAdapter DbHepler;
    private int tracesize = 30;
    private PolylineOptions mPolyoptions, tracePolytion;
    private Polyline mpolyline;
    private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();
    private TraceOverlay mTraceoverlay;

    private Polyline mOriginPolyline;
    private Marker mOriginStartMarker,mOriginEndMarker;
    private List<LatLng> mOriginLatLngList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_content, container, false);
        mcontrol= (ToggleButton) view.findViewById(R.id.control);
        initview(savedInstanceState,view);
        initpolyline();
        mcontrol.setOnClickListener(mcontrolListener);
        return view;
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
                record.setDate(getcuDate(mStartTime));
            }else{//停止运动
                mEndTime=System.currentTimeMillis();
                saveRecord(record.getPathline(), record.getDate());//保存轨迹
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
                    showOriginTrace(startLatLng,endLatLng,mOriginLatLngList);
                }


            }
        }
    };


    /**
     * 初始化视图
     * @param savedInstanceState
     * @param view
     */
    private void initview( Bundle savedInstanceState,View view){
        mapView= (MapView) view.findViewById(R.id.map_view);
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
        mcontrol.setOnClickListener(mcontrolListener);
        mTraceoverlay = new TraceOverlay(aMap);//纠偏
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        UiSettings settings =  aMap.getUiSettings();
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        settings.setLogoPosition(9000);//高德地图标志隐藏
    }

    private void initpolyline() {
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(15f);
        mPolyoptions.color(Color.GREEN);
    }

    /**
     * 保存记录路径
     * @param list
     * @param time
     */
    protected void saveRecord(List<AMapLocation> list, String time) {
        if (list != null && list.size() > 0) {
            DbHepler = new DbAdapter(getContext());
            DbHepler.open();
            String duration = getDuration();
            float distance = getDistance(list);
            String average = getAverage(distance);
            String pathlineSring = getPathLineString(list);
            AMapLocation firstLocaiton = list.get(0);
            AMapLocation lastLocaiton = list.get(list.size() - 1);
            String stratpoint = amapLocationToString(firstLocaiton);
            String endpoint = amapLocationToString(lastLocaiton);
            DbHepler.createrecord(String.valueOf(distance), duration, average, pathlineSring, stratpoint, endpoint, time);
            DbHepler.close();
        } else {
            Toast.makeText(getContext(), "没有记录到路径", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取路线字符串
     * @param list
     * @return
     */
    private String getPathLineString(List<AMapLocation> list) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuffer pathline = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            AMapLocation location = list.get(i);
            String locString = amapLocationToString(location);
            pathline.append(locString).append(";");
        }
        String pathLineString = pathline.toString();
        pathLineString = pathLineString.substring(0,pathLineString.length() - 1);
        return pathLineString;
    }

    /**
     * 地图位置字符串 （不显示）
     * @param location
     * @return
     */
    private String amapLocationToString(AMapLocation location) {
        StringBuffer locString = new StringBuffer();
        locString.append(location.getLatitude()).append(",");
        locString.append(location.getLongitude()).append(",");
        locString.append(location.getProvider()).append(",");
        locString.append(location.getTime()).append(",");
        locString.append(location.getSpeed()).append(",");
        locString.append(location.getBearing());
        return locString.toString();
    }
    /**
     * 获取时间
     */
    private String getcuDate(long time){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss ");
        Date curDate=new Date(time);
        String date=formatter.format(curDate);
        return date;
    }

    /**
     * 花费时间
     * @return
     */
    private String getDuration() {
        return String.valueOf((mEndTime - mStartTime) / 1000f);
    }
    /**
     * 经过距离
     * @param list
     * @return
     */
    private float getDistance(List<AMapLocation> list) {
        float distance = 0;
        if (list == null || list.size() == 0) {
            return distance;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            AMapLocation firstpoint = list.get(i);
            AMapLocation secondpoint = list.get(i + 1);
            LatLng firstLatLng = new LatLng(firstpoint.getLatitude(), firstpoint.getLongitude());
            LatLng secondLatLng = new LatLng(secondpoint.getLatitude(), secondpoint.getLongitude());
            double betweenDis = AMapUtils.calculateLineDistance(firstLatLng, secondLatLng);
            distance = (float) (distance + betweenDis);
        }
        return distance;
    }
    /**
     * 平均花费时间
     * @param distance
     * @return
     */
    private String getAverage(float distance) {
        return String.valueOf(distance / (float) (mEndTime - mStartTime));
    }



    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener=listener;
        if(locationClient==null){
            aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            locationClient=new AMapLocationClient(getActivity());
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

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null&&aMapLocation != null) {
            if (aMapLocation != null &&aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                LatLng mylocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                if(mcontrol.isChecked()){
                    record.addpoint(aMapLocation);
                    mPolyoptions.add(mylocation);
                    mTracelocationlist.add(Util.parseTraceLocation(aMapLocation));
                    redrawline();//实时轨迹
                    if (mTracelocationlist.size() > tracesize - 1) {
                        trace();
                    }
                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    /**
     * 实时轨迹画线
     */
    private void redrawline() {
        if (mPolyoptions.getPoints().size() > 1) {
            if (mpolyline != null) {
                mpolyline.setPoints(mPolyoptions.getPoints());
            } else {
                mpolyline = aMap.addPolyline(mPolyoptions);
            }
        }
    }

    private void trace() {
        List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
        mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, (TraceListener) this);
        TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
        mTracelocationlist.clear();
        mTracelocationlist.add(lastlocation);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        }
        else {
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
    }

    /**
     * 显示原始轨迹
     * @param startPoint
     * @param endPoint
     * @param originList
     */
    private void showOriginTrace(LatLng startPoint, LatLng endPoint,List<LatLng> originList){
        mOriginPolyline = aMap.addPolyline(new PolylineOptions().color(
                Color.BLUE).addAll(originList));//轨迹
        mOriginStartMarker = aMap.addMarker(new MarkerOptions().position(startPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.start)));//起点标记
        mOriginEndMarker = aMap.addMarker(new MarkerOptions().position(endPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.end)));//终点编标记

        try {
            aMap.moveCamera(CameraUpdateFactory.zoomTo(20));//缩放级别
//            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(), 50));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOriginPolyline.setVisible(true);
        mOriginStartMarker.setVisible(true);
        mOriginEndMarker.setVisible(true);
    }

    private LatLngBounds getBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (mOriginLatLngList == null) {
            return b.build();
        }
        for (int i = 0; i < mOriginLatLngList.size(); i++) {
            b.include(mOriginLatLngList.get(i));
        }
        return b.build();
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
}
