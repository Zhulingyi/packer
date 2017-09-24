package com.test.project03.recoder;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.test.project03.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.bmob.newim.core.BmobIMClient.getContext;
import static cn.bmob.v3.Bmob.getApplicationContext;

/**
 * Created by thinkpad on 2017/9/19.
 */

public class mapUtil {
    private Context mContext;
    private DbAdapter DbHepler;
    private Polyline mOriginPolyline;
    private Marker mOriginStartMarker,mOriginEndMarker;
    private Polyline mpolyline;

    public void trace(List<TraceLocation> mTracelocationlist) {
        List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
        mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, (TraceListener) this);
        TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
        mTracelocationlist.clear();
        mTracelocationlist.add(lastlocation);
    }
    /**
     * 实时轨迹画线
     */
    public void redrawline(AMap aMap,PolylineOptions mPolyoptions) {
        if (mPolyoptions.getPoints().size() > 1) {
            if (mpolyline != null) {
                mpolyline.setPoints(mPolyoptions.getPoints());
            } else {
                mpolyline = aMap.addPolyline(mPolyoptions);
            }
        }
    }
    /**
     * 显示原始轨迹
     * @param startPoint
     * @param endPoint
     * @param originList
     */
    public void showOriginTrace(AMap aMap,LatLng startPoint, LatLng endPoint,List<LatLng> originList){
        mOriginPolyline = aMap.addPolyline(new PolylineOptions().color(
                Color.BLUE).addAll(originList));//轨迹
        mOriginStartMarker = aMap.addMarker(new MarkerOptions().position(startPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.start)));//起点标记
        mOriginEndMarker = aMap.addMarker(new MarkerOptions().position(endPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.end)));//终点编标记

        try {
//            aMap.moveCamera(CameraUpdateFactory.zoomTo(17));//缩放级别
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(originList), 50));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOriginPolyline.setVisible(true);
        mOriginStartMarker.setVisible(true);
        mOriginEndMarker.setVisible(true);
    }
    /**
     * 保存记录路径
     * @param list
     * @param time
     */
    public void saveRecord(Context context,List<AMapLocation> list, String time, long mStartTime, long mEndTime ) {
        if (list != null && list.size() > 0) {
            DbHepler = new DbAdapter(context);
            DbHepler.open();
            String duration = getDuration(mStartTime,mEndTime);
            float distance = getDistance(list);
            String average = getAverage(distance,mStartTime,mEndTime);
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
    public String getcuDate(long time){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss ");
        Date curDate=new Date(time);
        String date=formatter.format(curDate);
        return date;
    }
    /**
     * 花费时间
     * @return
     */
    private String getDuration(long mStartTime,long mEndTime ) {
        return String.valueOf((mEndTime - mStartTime) / 1000f);
    }
    /**
     * 平均花费时间
     * @param distance
     * @return
     */
    private String getAverage(float distance,long mStartTime,long mEndTime) {
        return String.valueOf(distance / (float) (mEndTime - mStartTime));
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

    private LatLngBounds getBounds( List<LatLng> mOriginLatLngList) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (mOriginLatLngList == null) {
            return b.build();
        }
        for (int i = 0; i < mOriginLatLngList.size(); i++) {
            b.include(mOriginLatLngList.get(i));
        }
        return b.build();
    }



}
