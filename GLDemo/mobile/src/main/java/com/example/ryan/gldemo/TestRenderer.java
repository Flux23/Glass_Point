//Ryan Merrill 2015

package com.example.ryan.gldemo;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


//rendering callback for OpenGL ES
public class TestRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    Vector<Cube> mCubes;
    float posX = 0;
    float posY = 0;
    float posZ = 0;
    float rX = 0;
    float rY = 0;


    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] mMVPMatrix = new float[16];
    private float[] mMVPTMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private boolean ready = false;

    public TestRenderer(Vector<Point> points)
    {
        setCubes(points);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.2f, 1.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        //mTriangle = new Triangle(this);
        //mCubes = new Vector<Cube>();
        //add cubes to vector
        //mCubes.add(new Cube(3, 0, 3));
        //mCubes.add(new Cube(-3, 0, 3));
        for (int i = 0; i < mCubes.size(); i++) {
            mCubes.get(i).init_cube();
        }
        ready = true;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if(!ready){
            return;
        }
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        //transforming the MVP matrix in draw is not ideal, but is slightly more effective than recreating the matrix every time the sensor is triggered
        mMVPTMatrix = mMVPMatrix.clone();
        Matrix.rotateM(mMVPTMatrix, 0, rX, 1f, 0f, 0f);
        Matrix.rotateM(mMVPTMatrix, 0, rY, 0f, 1f, 0f);
        Matrix.translateM(mMVPTMatrix, 0, -posX, -posY, -posZ);

        for(int i = 0; i < mCubes.size(); i++) {
            // update and draw the cubes
            mCubes.get(i).draw(mMVPTMatrix);
        }

    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 50);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        ready = true;

    }

    //rotates ModelViewPerspective matrix
    public void setLookAt(float[] rot_matrix){
        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, mMVPMatrix, 0,rot_matrix, 0);
        mMVPMatrix = temp;
    }

    public boolean isReady(){
        return ready;
    }

    //sets where the user is looking, and the users position
    public void setLookAt(float x, float y, float z, float ry, float rx){
        posX = x;
        posY = y;
        posZ = z;
        rX = rx;
        rY = ry;
    }

    //sets where the user is looking
    public void setRot(float ry, float rx){
        rX = rx;
        rY = ry;
    }

    //sets the users position
    public void setPos(float x, float y, float z){
        posX = x;
        posY = y;
        posZ = z;
    }

    //gets the cube the user is looking at
    public int getLookAt() {
        float best_dist = -1;
        int best = -1;
        if (mCubes != null){
            //transforms z unit vector in view space to model space
            float[] lookAt = new float[4];
            float[] zbasis = {0f, 0f, -1f, 1f};
            float[] temp = new float[16];
            Matrix.invertM(temp, 0, mMVPTMatrix, 0);
            Matrix.multiplyMV(lookAt, 0, temp, 0, zbasis, 0);

            //checks to see if the vector points at any of the cubes
            int i;
            for (i = 0; i < mCubes.size(); i++) {
                if (mCubes.get(i) != null) {
                    float dist;
                    dist = mCubes.get(i).isAt(lookAt);
                    if ( (best_dist < 0) && (dist > 0) ){
                        best_dist = dist;
                        best = i;
                    }
                    else if ((dist > 0) && (dist < best_dist)) {
                        best_dist = dist;
                        best = i;
                    }
                }
            }
        }
        if(best > 0) {
            return best;
            //return mCubes.get(best).index;
        }
        return best;
    }

    public void reset(){
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0f, 0f, 1f, 0f, 1.0f, 0.0f);
    }

    //adds a cube to a vector
    public int addCube(float x, float y, float z){
        Cube cube = new Cube(x,y,z);
        mCubes.add(cube);
        return mCubes.size()-1;
    }

    public void setCubes(Vector<Point> points){
        int j;
        int i;
        mCubes = new Vector<Cube>();
        for (i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            Cube cube = new Cube(point.mLat,0,point.mLng);
            mCubes.add(cube);
            point.index = mCubes.size();
            cube.index = i;
            if(ready){
                cube.init_cube();
            }
        }
    }

    public void updateCubes(Vector<Point> points){
        ready = false;
        int j;
        int i;
        for (j = 0; j < mCubes.size(); j++) {
            mCubes.get(j).index = -1;
        }
        for (i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if(point.index >= 0) {
                Cube cube = mCubes.get(point.index);
                if ((cube.lx == point.mLat) && (cube.lz == point.mLng)) {
                    cube.index = i;
                    continue;
                }
            }
            point.index = -1;
            for (j = 0; j < mCubes.size(); j++) {
                Cube cube = mCubes.get(j);
                if ((cube.lx == point.mLat) && (cube.lz == point.mLng) && (cube.index == -1)){
                    point.index = j;
                    cube.index = i;
                }
            }
            if( point.index == -1 ){
                Cube cube = new Cube(point.mLat,0,point.mLng);
                mCubes.add(cube);
                cube.init_cube();
                point.index = j;
                cube.index = i;
            }
        }
        j = 0;
        while(j < mCubes.size()) {
            if(mCubes.get(j).index == -1){
                mCubes.remove(j);
            }
            else {
                points.get(mCubes.get(j).index).index = j;
                j++;
            }
        }
        ready = true;
    }

    //clears cubes
    public void resetCubes(){
        mCubes.clear();
    }

    //removes cubes from vector
    public void removeCube(int index){
        mCubes.remove(index);
    }

    //gets the dot product of two vectors
    public static float dotProduct(float[] v1, float[] v2){
        float dot = 0f;
        if (v1.length <= v2.length) {
            int i;
            for (i = 0; i < v1.length; i++) {
                dot = dot + (v1[i] * v2[i]);
            }
        }
        return dot;
    }
}