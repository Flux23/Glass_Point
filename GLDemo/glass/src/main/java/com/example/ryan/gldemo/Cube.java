//Ryan Merrill 2015

package com.example.ryan.gldemo;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Renders a 3D Cube using OpenGL ES 2.0.
 *
 * For more information on how to use OpenGL ES 2.0 on Android, see the
 * <a href="//developer.android.com/training/graphics/opengl/index.html">
 * Displaying Graphics with OpenGL ES</a> developer guide.
 */
public class Cube {

    public int index;

    private final float RADIUS = .5f;

    private final float RADIUS2 = RADIUS * RADIUS;
    /** Cube vertices */
    private static final float VERTICES[] = {
            -0.25f, -0.25f, -0.25f,
            0.25f, -0.25f, -0.25f,
            0.25f, 0.25f, -0.25f,
            -0.25f, 0.25f, -0.25f,
            -0.25f, -0.25f, 0.25f,
            0.25f, -0.25f, 0.25f,
            0.25f, 0.25f, 0.25f,
            -0.25f, 0.25f, 0.25f
    };

    /** Vertex colors. */
    private static final float COLORS[] = {
            0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
    };

    /** Order to draw vertices as triangles. */
    private static final byte INDICES[] = {
            0, 1, 3, 3, 1, 2, // Front face.
            0, 1, 4, 4, 5, 1, // Bottom face.
            1, 2, 5, 5, 6, 2, // Right face.
            2, 3, 6, 6, 7, 3, // Top face.
            3, 7, 4, 4, 3, 0, // Left face.
            4, 5, 7, 7, 6, 5, // Rear face.
    };


    private static final int COORDS_PER_VERTEX = 3;

    private static final int VALUES_PER_COLOR = 4;

    /** Vertex size in bytes. */
    private final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    /** Color size in bytes. */
    private final int COLOR_STRIDE = VALUES_PER_COLOR * 4;

    /** Shader code for the vertex. */
    private static final String VERTEX_SHADER_CODE =
            "uniform mat4 uMVPMatrix;" +
                    "uniform mat4 uLocMatrix;" +
                    "attribute vec4 vPosition;" +
                    //"attribute vec4 vColor;" +
                    //"varying vec4 _vColor;" +
                    "void main() {" +
                    //"  _vColor = vColor;" +
                    "  gl_Position = uMVPMatrix * uLocMatrix * vPosition;" +
                    "}";

    /** Shader code for the fragment. */
    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
                   // "varying vec4 _vColor;" +
                    "uniform vec4 _vColor;" +
                    "void main() {" +
                    "  gl_FragColor = _vColor;" +
                    "}";

    private final float[] mLocMatrix = new float[16];

    float COLOR[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
    private int mProgram;
    private int mLocHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    float lx = 0;
    float ly = 0;
    float lz = 0;

    public Cube() {
        Matrix.setIdentityM(mLocMatrix, 0);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4);

        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(VERTICES);
        mVertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(COLORS.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuffer.asFloatBuffer();
        mColorBuffer.put(COLORS);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(INDICES.length);
        mIndexBuffer.put(INDICES);
        mIndexBuffer.position(0);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE));
        GLES20.glAttachShader(
                mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE));
        GLES20.glLinkProgram(mProgram);

        mLocHandle = GLES20.glGetUniformLocation(mProgram, "uLocMatrix");//translates cube to specified location
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "_vColor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public Cube(float x, float y, float z){

        lx = x;
        ly = y;
        lz = z;

    }

    public void init_cube(){
        Matrix.setIdentityM(mLocMatrix, 0);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4);
        Matrix.translateM(mLocMatrix, 0, lx,ly,lz);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(VERTICES);
        mVertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(COLORS.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuffer.asFloatBuffer();
        mColorBuffer.put(COLORS);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(INDICES.length);
        mIndexBuffer.put(INDICES);
        mIndexBuffer.position(0);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE));
        GLES20.glAttachShader(
                mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE));
        GLES20.glLinkProgram(mProgram);

        mLocHandle = GLES20.glGetUniformLocation(mProgram, "uLocMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "_vColor");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix The Model View Project matrix in which to draw this shape
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment.
        GLES20.glUseProgram(mProgram);

        // Prepare the cube coordinate data.
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, 3, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

        // Prepare the cube color data.
        //GLES20.glEnableVertexAttribArray(mColorHandle);
        //GLES20.glVertexAttribPointer(
        //        mColorHandle, 4, GLES20.GL_FLOAT, false, COLOR_STRIDE, mColorBuffer);
        GLES20.glUniform4fv(mColorHandle, 1, COLOR, 0);

        // Apply the projection and view transformation.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(mLocHandle, 1, false, mLocMatrix, 0);

        // Draw the cube.
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, INDICES.length, GLES20.GL_UNSIGNED_BYTE, mIndexBuffer);

        // Disable vertex arrays.
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        //GLES20.glDisableVertexAttribArray(mColorHandle);
    }

    /** Loads the provided shader in the program. */
    private static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public float isAt(float[] lookAt){
        float[] pos = new float[3];
        pos[0] = lx; pos[1] = ly; pos[2] = lz;
        //float mag = (float)Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1] + pos[2] * pos[2]);
        //pos[0] = pos[0]/mag; pos[1] = pos[1]/mag; pos[2] = pos[2]/mag;
        //boolean x_diff = (Math.abs(pos[0] - lookAt[0]) < .1f);
        //boolean y_diff = (Math.abs(pos[1] - lookAt[1]) < .1f);
        //boolean z_diff = (Math.abs(pos[2] - lookAt[2]) < .1f);
        //return (x_diff && y_diff && z_diff);
        float mag2 = TestRenderer.dotProduct(pos,pos);
        float dot = TestRenderer.dotProduct(pos,lookAt);
        float offset = Math.abs(mag2-(dot * dot));
        boolean looking =  offset <= RADIUS2;
        if(looking) {
            return dot;
        }
        return -1f;
    }
}