//Ryan Merrill 2015

package com.example.ryan.gldemo;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

//GLSurfaceView container
public class PointView extends GLSurfaceView {

    public PointView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
    }
    public PointView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
    }
}
