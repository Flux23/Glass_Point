package com.example.ryan.gldemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.List;
import java.util.Vector;


/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class GLDemo extends Activity {

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;

    private final int minTime = 100;
    private final int minDistance = 1;
    /**
     */
    private View mView;
    PointView pView;
    TestRenderer tRender;
    Vector<Point> mPoints;
    Vector<View> mCards;

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
    private float prevH = 0f;
    private float prevP = 0f;
    private float mRoll;
    private Location mLocation;
    private boolean mHasInterference;
    private boolean first = true;
    private int mMode;
    private int lookAt;


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

                //float prevH = mHeading;
                //float prevP = mPitch;
                //converts orientation values to degrees
                mPitch = (float) Math.toDegrees(mOrientation[1]);
                mHeading = (float) Math.toDegrees(mOrientation[0]);
                mRoll = (float) Math.toDegrees(mOrientation[0]);

                float hDiff = prevH - mHeading;
                float pDiff = prevP - mPitch;
                prevH = prevH - hDiff;
                prevP = prevP - pDiff;
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
                    tRender.setRot(mHeading, -mPitch);//sets lookAt to current orientation
                    if(mCardScroller.getSelectedItemPosition() == 0) {
                        lookAt = tRender.getLookAt();
                        if (lookAt > -1) {
                            Point point = mPoints.get(lookAt);
                            X.setText("" + point.mName);
                            Y.setText("" + point.mLat);
                            Z.setText("" + point.mLng);
                            //W.setText("" + point.index);//gets the cube the user is looking at
                        } else {
                            //X.setText("" + mHeading);
                            //Y.setText("" + hDiff);
                            //Z.setText("" + (prevH + hDiff));
                            //W.setText("" + tRender.getLookAt());//gets the cube the user is looking at
                            X.setText("");
                            Y.setText("");
                            Z.setText("");
                            //W.setText("-1");//gets the cube the user is looking at
                        }
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
            //boolean moved = (.01f < Math.abs(mLocation.getLatitude() - prevLoc.getLatitude())) && (.01f < Math.abs(mLocation.getLongitude() - prevLoc.getLongitude()));
            //boolean jumped = (5f < Math.abs(mLocation.getLatitude() - prevLoc.getLatitude())) && (5f <  Math.abs(mLocation.getLongitude() - prevLoc.getLongitude()));
            //if(moved && !jumped) {
                //tRender.setPos((float) mLocation.getLatitude(), 0, (float) mLocation.getLongitude());
            //}
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
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        //initializes sensors and renderers
        mCards = new Vector<View>();
        mMode = 0;
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
            mLocationManager.requestLocationUpdates(provider, minTime,
                    minDistance, mLocationListener);
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
        //get sensorManager and initialise sensor listeners


        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return mCards.size() + 1;
            }

            @Override
            public Object getItem(int position) {
                if(position < 1 || position > mCards.size()){
                    return mView;
                }
                return mCards.get(position - 1);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(position < 1 || position > mCards.size()){
                    return mView;
                }
                return mCards.get(position - 1);
            }

            @Override
            public int getPosition(Object item) {
                if(mView.equals(item)){
                    return 0;
                }
                for(int i = 0; i < mCards.size(); i++) {
                    if (mCards.get(i).equals(item)) {
                        return i + 1;
                    }
                }
                return AdapterView.INVALID_POSITION;
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                //AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                //am.playSoundEffect(Sounds.DISALLOWED);
                openOptionsMenu();
            }
        });
        setContentView(mCardScroller);
        //tRender.updateCubes(mPoints);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu){
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId ==  Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.menu_gldemo, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
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
        Point point = null;
        if(lookAt >= 0) {
            point = mPoints.get(lookAt);
            buildDetailView(point);
        }
        switch (item.getItemId()) {
            case R.id.action_reset:
                reset();
                return true;
            case R.id.edit:
                editView(point, false);
                return true;
            case R.id.edit_all:
                editView(point, true);
                return true;
            case R.id.select:
                updateDetailView(point);
                mCardScroller.setSelection(1);
                return true;
            case R.id.update:
                update();
                return true;
            case R.id.add:
                addView();
                return true;
            case R.id.next:
                mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() + 1);
                return true;
            case R.id.prev:
                mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() - 1);
                return true;
            case R.id.home:
                mCardScroller.setSelection(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId ==  Window.FEATURE_OPTIONS_PANEL) {
            Point point = null;
            if(lookAt >= 0) {
                point = mPoints.get(lookAt);
                buildDetailView(point);
            }
            switch (item.getItemId()) {
                case R.id.action_reset:
                    reset();
                    return true;
                case R.id.edit:
                    editView(point, false);
                    return true;
                case R.id.edit_all:
                    editView(point, true);
                    return true;
                case R.id.select:
                    updateDetailView(point);
                    mCardScroller.setSelection(1);
                    return true;
                case R.id.update:
                    update();
                    return true;
                case R.id.add:
                    addView();
                    return true;
                case R.id.next:
                    mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() + 1);
                    return true;
                case R.id.prev:
                    mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() - 1);
                    return true;
                case R.id.home:
                    mCardScroller.setSelection(0);
                    return true;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    private final static int SPEECH_REQUEST = 0;
    private String voiceInput = null;
    boolean finished;
    Point cur_point;

    public void getVoiceInput(String text, int position) {
        // Start the intent to ask the user for voice input
        finished = false;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, text);
        startActivityForResult(intent, position);
        if(!finished) {
            android.util.Log.d("VoiceInputActivity", "customization text: waiting");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // When the voice input intent returns and is ok
        if (resultCode == RESULT_OK) {
            // Get the text spoken
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            android.util.Log.d("VoiceInputActivity", "customization text: " + spokenText);


            if(requestCode == 1){
                cur_point.mName = spokenText;
            }
            else if(requestCode > 2){
                cur_point.mData.set(requestCode - 3, spokenText);
            }
            updateDetailView(cur_point);
            finished = true;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addView() {
        String name = "";
        float lat = 1f;
        float lng = 1f;
        Point point = new Point(name,lat,lng);
        for( int i = 0; i < mPoints.get(0).mData.size(); i++){
            point.mData.add("");
        }
        mPoints.add(point);
        editView(point,true);
    }

    private void editView(Point point, boolean editall) {
        if(point == null){
            return;
        }
        cur_point = point;
        if(editall) {

            getGPS(point);
            for (int i = point.mData.size() - 1; i >= 0 ; i--) {
                getVoiceInput(String.format("Enter field %d", i), i + 3);
            }
            getVoiceInput("Enter Name", 1);
        }
        else {
            int index = mCardScroller.getSelectedItemPosition();
            if(index == 1){
                getVoiceInput("Enter Name", 1);
            }
            else if(index == 2){
                getGPS(point);
            }
            else if(index > 2){
                getVoiceInput(String.format("Enter field %d", index - 3), index);
            }
        }
        tRender.updateCubes(mPoints);
        updateDetailView(point);
    }

    private void getGPS(Point point){
        point.mLat = point.mLat;
        point.mLng = point.mLng;
    }

    private void updateDetailView(Point point) {
        if(point == null){
            return;
        }
        setText(mCards.get(0), point.mName);
        setText(mCards.get(1), String.format("%02f\n%02f", point.mLat, point.mLng));
        for( int i = 0; i < point.mData.size(); i++){
            setText(mCards.get(i + 2), point.mData.get(i));
        }
    }

    private void buildDetailView(Point point) {
        if(point == null){
            return;
        }
        for( int i = mCards.size(); i < point.mData.size() + 2; i++){
            mCards.add(buildTextCard());
        }
        //updateDetailView(point);
    }

    private View buildTextCard(){
        View view = getLayoutInflater().inflate(R.layout.text_view, null);
        return view;
    }

    private View buildImageCard(){
        return null;
    }

    private View buildNum(float num) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.setText(String.format("%02f", num));
        return card.getView();
    }

    private View buildText(String text) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.setText(text);
        return card.getView();
    }

    private View buildImage(Bitmap image) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.addImage(image);
        return card.getView();
    }

    private View buildVideo() {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        return card.getView();
    }

    private void setText(View view, String string){
        TextView text = (TextView) view.findViewById(R.id.text);
        text.setText(string);
    }

    private void setImage(View view, Bitmap bitmap){
        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setImageBitmap(bitmap);
    }

    private void reset(){
        tRender.reset();
        mPoints = Point.readFile(this);
        tRender.setCubes(mPoints);
    }

    private void update(){
        tRender.reset();
        mPoints = Point.readFile(this);
        tRender.setCubes(mPoints);
    }
}
