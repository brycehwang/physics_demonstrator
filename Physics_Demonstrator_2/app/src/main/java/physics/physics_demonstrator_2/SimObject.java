package physics.physics_demonstrator_2;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by jfuchs on 9/20/15.
 */
public class SimObject {
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private final int nverts;
    private int program;

    private final String vertexShader =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShader =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    static final int COORD_DIM = 3;
    static final int VERTEX_STRIDE = COORD_DIM * 4;

    public SimObject(VictoryActivity.Shape shape) {
        nverts = shape.vertices.length/3;

        ByteBuffer bb = ByteBuffer.allocateDirect(nverts * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(shape.vertices);
        vertexBuffer.position(0);

        short[] indices = new short[nverts];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
        }

        ByteBuffer ibb = ByteBuffer.allocateDirect(nverts * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public void glInit() {
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, PhysicsRenderer.compileShader(
                GLES20.GL_VERTEX_SHADER, vertexShader));
        GLES20.glAttachShader(program, PhysicsRenderer.compileShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShader));
        GLES20.glLinkProgram(program);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        int posHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(posHandle);

        GLES20.glVertexAttribPointer(
                posHandle, COORD_DIM,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, vertexBuffer);

        float color[] = {0.f, 0.f, 0.f, 1.0f};
        int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        int matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        PhysicsRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0);
        PhysicsRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(GLES20.GL_LINE_LOOP, nverts, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        GLES20.glDisableVertexAttribArray(posHandle);
    }
}

