//Ryan Merrill 2015

package com.example.ryan.gldemo;


import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.math.*;
import java.util.List;
import java.util.Vector;

public class GLDemo extends Activity {

    private final int minTime = 100;
    private final int minDistance = 1;

    private View mView;
    PointView pView;
    TestRenderer tRender;
    Vector<Point> mPoints;

    TextView X;
    TextView Y;
    TextView Z;
    TextView W;

    private LocationManager mLocationManager;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    private float[] mOrientation;
    private float[] mRotationMatrix;

    private float mHeading;
    private float mPitch;
    private float mRoll;
    private Location mLocation;
    private boolean mHasInterference;
    private boolean first = true;
    private int mMode;

    private SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mHasInterference = (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // Get the current heading from the sensor, then notify the listeners of the
                // change.
                float[] prev = mRotationMatrix;
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, mRotationMatrix);
                SensorManager.getOrientation(mRotationMatrix, mOrientation);

                float prevH = mHeading;
                float prevP = mPitch;
                //converts orientation values to degrees
                mPitch = (float) Math.toDegrees(mOrientation[1]);
                mHeading = (float) Math.toDegrees(mOrientation[0]);
                mRoll = (float) Math.toDegrees(mOrientation[0]);

                float hDiff = prevH - mHeading;
                float pDiff = prevP - mPitch;
                boolean moved = (.01f < Math.abs(hDiff)) && (.01f < Math.abs(pDiff));
                //boolean jumped = (5f < Math.abs(hDiff)) && (5f < Math.abs(pDiff));
                if(!tRender.isReady()){
                    return;
                }
                if(first){
                    tRender.setLookAt(0,0,0,mHeading, -mPitch);//sets lookAt to current orientation
                    first = false;
                }
                else if(moved) {
                    //tRender.setLookAt(0,0,0,mHeading,mPitch);
                    tRender.setRot(mHeading, -mPitch);//sets lookAt to current orientation
                    int index = tRender.getLookAt();
                    if(index > -1) {
                        Point point = mPoints.get(index);
                        X.setText("" + point.mName);
                        Y.setText("" + point.mLat);
                        Z.setText("" + point.mLng);
                        W.setText("" + point.index);//gets the cube the user is looking at
                    }
                    else {
                        //X.setText("" + mHeading);
                        //Y.setText("" + hDiff);
                        //Z.setText("" + (prevH + hDiff));
                        //W.setText("" + tRender.getLookAt());//gets the cube the user is looking at
                        X.setText("");
                        Y.setText("");
                        Z.setText("");
                        W.setText("-1");//gets the cube the user is looking at
                    }
                }
            }
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Location prevLoc = mLocation;
            mLocation = location;
            if(!tRender.isReady()){
                return;
            }
            boolean moved = (.01f < Math.abs(mLocation.getLatitude() - prevLoc.getLatitude())) && (.01f < Math.abs(mLocation.getLongitude() - prevLoc.getLongitude()));
            boolean jumped = (5f < Math.abs(mLocation.getLatitude() - prevLoc.getLatitude())) && (5f <  Math.abs(mLocation.getLongitude() - prevLoc.getLongitude()));
            if(moved && !jumped) {
                tRender.setPos((float) mLocation.getLatitude(), 0, (float) mLocation.getLongitude());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Don't need to do anything here.
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Don't need to do anything here.
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Don't need to do anything here.
        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMode = 0;
        //initializes sensors and renderers
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPoints = Point.readFile(this);
        mRotationMatrix = new float[16];
        mOrientation = new float[9];

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_UI);

        // The rotation vector sensor doesn't give us accuracy updates, so we observe the
        // magnetic field sensor solely for those.
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);

        List<String> providers = mLocationManager.getProviders(
                criteria, true /* enabledOnly */);

        for (String provider : providers) {
         //   mLocationManager.requestLocationUpdates(provider, minTime,
           //         minDistance, mLocationListener);
        }

        mView = getLayoutInflater().inflate(R.layout.activity_gldemo, null);
        X = (TextView) mView.findViewById(R.id.text1);
        Y = (TextView) mView.findViewById(R.id.text2);
        Z = (TextView) mView.findViewById(R.id.text3);
        W = (TextView) mView.findViewById(R.id.text4);
        tRender = new TestRenderer(mPoints);
        pView = (PointView) mView.findViewById(R.id.point_view);
        pView.setEGLContextClientVersion(2);
        pView.setPreserveEGLContextOnPause(true);
        pView.setRenderer(tRender);
        //W.setText("" + tRender.getLookAt());
        this.setContentView(mView);
        //tRender.updateCubes(mPoints);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gldemo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.action_reset:
                tRender.reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
