package com.example.ryan.gldemo;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

//import com.google.android.glass.timeline.GlRenderer;

import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Ryan on 3/6/2015.
 */
public class PointRenderer implements GLSurfaceView.Renderer {

    /** Rotation increment per frame. */
    private static final float CUBE_ROTATION_INCREMENT = 0.6f;

    /** The refresh rate, in frames per second. */
    private static final int REFRESH_RATE_FPS = 60;

    /** The duration, in milliseconds, of one frame. */
    private static final float FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;

    private final float[] mMVPMatrix;
    private final float[] mProjectionMatrix;
    private final float[] mViewMatrix;
    private final float[] mRotationMatrix;
    private final float[] mFinalMVPMatrix;
    float vx;
    float vy;
    float vz;

    private Cube mCube;
    private float mCubeRotation;
    private long mLastUpdateMillis;

    public PointRenderer() {
        mMVPMatrix = new float[16];
        mProjectionMatrix = new float[16];
        mViewMatrix = new float[16];
        mRotationMatrix = new float[16];
        mFinalMVPMatrix = new float[16];

        // Set the fixed camera position (View matrix).
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4.0f, 0.0f, 1.0f, 0.0f);
    }

    public void onSurfaceCreated(EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        mCube = new Cube();//new Cube(0f,0f,4f);
    }

    public void onSurfaceChanged(int width, int height) {
        float ratio = (float) width / height;

        GLES20.glViewport(0, 0, width, height);
        // This projection matrix is applied to object coordinates in the onDrawFrame() method.
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 3.0f, 7.0f);
        // modelView = projection x view
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the rotation.
        Matrix.setRotateM(mRotationMatrix, 0, mCubeRotation, 1.0f, 1.0f, 1.0f);
        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(mFinalMVPMatrix, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw cube.
        mCube.draw(mFinalMVPMatrix);
    }

    public void setLookAt(float[] rot_matrix){
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0,rot_matrix, 0);
    }

    public int selected(){

        return -1;
    }
    /** Updates the cube rotation. */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        onSurfaceCreated(config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        onSurfaceChanged(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        onDrawFrame();
    }
}
