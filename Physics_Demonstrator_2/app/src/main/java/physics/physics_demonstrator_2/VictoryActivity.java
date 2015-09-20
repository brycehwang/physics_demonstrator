package physics.physics_demonstrator_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.bytedeco.javacpp.indexer.IntBufferIndexer;

import java.io.File;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_TREE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.Canny;
import static org.bytedeco.javacpp.opencv_imgproc.MORPH_ELLIPSE;
import static org.bytedeco.javacpp.opencv_imgproc.approxPolyDP;
import static org.bytedeco.javacpp.opencv_imgproc.blur;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.dilate;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;
import static org.bytedeco.javacpp.opencv_imgproc.getStructuringElement;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class VictoryActivity extends Activity {
    public class IPoint {
        public int x, y;

        public IPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ")";
        }
    };

    public class Shape {
        public boolean closed;
        public float[] vertices;

        public Shape(ArrayList<IPoint> pts, boolean closed ) {
            int npts = pts.size();
            this.vertices = new float[npts * 3];
            for (int i = 0; i < npts; i++) {
                this.vertices[i*3    ] = (float) pts.get(i).x;
                this.vertices[i*3 + 1] = (float) pts.get(i).y;
                this.vertices[i*3 + 2] = 0.f;
            }
            this.closed = closed;
        }
    }

    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String var = intent.getStringExtra("physics.physics_demonstrator2.PATH_MESSAGE");

        Mat img = imread(var);
        resize(img, img, new Size(img.cols() / 5, img.rows() / 5));

        ArrayList<Shape> objs = extract_objects(img);
        Log.d("Physics", "w " + img.cols() + " h " + img.rows());

        mGLView = new MyGLSurfaceView(this, objs, img.cols(), img.rows());
        setContentView(mGLView);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mGLView.onPause();
    }

    ArrayList<Shape> extract_objects(Mat img) {
        cvtColor(img, img, CV_RGB2GRAY);
        blur(img, img, new Size(6, 6));

        Canny(img, img, 20, 60);
        dilate(img, img, getStructuringElement(MORPH_ELLIPSE, new Size(10, 10)));

        MatVector contours = new MatVector();
        Mat hier = new Mat();
        findContours(img, contours, hier, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);

        IntBufferIndexer hierarchy = hier.createIndexer();

        int imgh = img.rows();

        ArrayList<Shape> objects = new ArrayList();
        for (int i = 0; i < contours.size(); i++) {
            // only want top-level
            if (hierarchy.get(0, i, 3) >= 0) {
                continue;
            }

            // closed if its got child contour
            boolean closed = hierarchy.get(0, i, 2) >= 0;

            Mat cont = contours.get(i);
            approxPolyDP(cont, cont, 2.0, false);

            IntBufferIndexer cidx = cont.createIndexer();
            ArrayList<IPoint> pts = new ArrayList<>();
            for (int j = 0; j < cidx.height(); j++) {
                pts.add(new IPoint(cidx.get(j, 0), imgh - cidx.get(j, 1)));
            }

            objects.add(new Shape(pts, closed));
        }

        return objects;
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private final PhysicsRenderer mRenderer;

        public MyGLSurfaceView(Context context, ArrayList<Shape> objs, int imgw, int imgh) {
            super(context);
            setPreserveEGLContextOnPause(true);
            setEGLContextClientVersion(2);
            mRenderer = new PhysicsRenderer(objs, imgw, imgh);
            setRenderer(mRenderer);
        }
    }
}
