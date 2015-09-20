package physics.physics_demonstrator_2;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PhysicsRenderer implements GLSurfaceView.Renderer {

    private ArrayList<SimObject> objects = new ArrayList<>();
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final int imgw, imgh;

    public PhysicsRenderer(ArrayList<VictoryActivity.Shape> objs, int imgw, int imgh) {
        this.imgw = imgw;
        this.imgh = imgh;

        for (VictoryActivity.Shape shape : objs) {
            objects.add(new SimObject(shape));
        }
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(1.f, 1.f, 1.f, 1.0f);

        for (SimObject obj : objects) {
            obj.glInit();
        }
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        for (SimObject obj : objects) {
            obj.draw(mMVPMatrix);
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.w("Physics-GL", "w " + width + " h " + height);

        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        for(int i = 0; i < 16; i++) {
            mProjectionMatrix[i] = 0.0f;
            mViewMatrix[i] = 0.0f;
            mMVPMatrix[i] = 0.0f;
        }

        float cx = imgw/2.f;
        float cy = imgh/2.f;

        Matrix.orthoM(mProjectionMatrix, 0, 0, width, 0, height, 1, 300);
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 200f,
                0, 0, 0f,
                0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    public static int compileShader(int type, String glsl) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, glsl);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("PhysicsSim", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
