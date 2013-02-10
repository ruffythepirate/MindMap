/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import se.jc.library.graphics.CanvasCamera;

/**
 *
 * @author Ruffy
 */
public class ZoomView extends View {

    protected static int ACTION_MODE_NONE = 0;
    protected static int ACTION_MODE_DRAG = 1;
    protected static int ACTION_MODE_ZOOM = 2;
    private static float MIN_ZOOM = 0.25f;
    private static float MAX_ZOOM = 4f;
    private ScaleGestureDetector _zoomDetector;
    private CanvasCamera _canvasCamera;
    private int _actionMode;
    private PointF _currentDragStart;
    private PointF _cameraDragStartTranslation;

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeZoomComponents();

    }

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeZoomComponents();
    }

    public ZoomView(Context context) {
        super(context);
        initializeZoomComponents();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                setCurrentDragStart(event.getX(), event.getY());
                initializeCameraDragStartTranslation();
                setActionMode(ACTION_MODE_DRAG);
                break;

            case MotionEvent.ACTION_MOVE:
                if (getActionMode() == ACTION_MODE_DRAG) {
                    updateCameraTranslation(event);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                setActionMode(ACTION_MODE_ZOOM);
                break;

            case MotionEvent.ACTION_UP:
                setActionMode(ACTION_MODE_NONE);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                setActionMode(ACTION_MODE_NONE);
                break;
        }

        _zoomDetector.onTouchEvent(event);
        return true;
    }

    protected void prepareCanvasZoom(Canvas canvasToPrepare) {
        Matrix zoomMatrix = getCanvasCamera().getTransformMatrix();
        canvasToPrepare.setMatrix(zoomMatrix);
    }

    private void initializeZoomComponents() {
        _zoomDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        setCanvasCamera(new CanvasCamera());
        getCanvasCamera().setScale(1.f);
        setCurrentDragStart(new PointF());
    }

    protected int getActionMode() {
        return _actionMode;
    }

    protected void setActionMode(int actionMode) {
        this._actionMode = actionMode;
    }

    protected PointF getCurrentDragStart() {
        return _currentDragStart;
    }
    
    public PointF getAsAbsoluteCoordinate(float x, float y)
    {
        CanvasCamera camera = getCanvasCamera();
        return camera.getAsUntransformedCoordinates(x, y);
    }
    
    protected void setCurrentDragStart(PointF currentDragStart) {
        this._currentDragStart = currentDragStart;
    }

    protected void setCurrentDragStart(float x, float y) {
        this._currentDragStart.set(x, y);
    }

    protected CanvasCamera getCanvasCamera() {
        return _canvasCamera;
    }

    protected void setCanvasCamera(CanvasCamera canvasCamera) {
        this._canvasCamera = canvasCamera;
    }

    protected PointF getCameraDragStartTranslation() {
        return _cameraDragStartTranslation;
    }

    protected void initializeCameraDragStartTranslation() {
        this._cameraDragStartTranslation = getCanvasCamera().copyTranslation();
    }

    private void updateCameraTranslation(MotionEvent event) {
        PointF dragTranslation = new PointF();
        dragTranslation.set(event.getX() - getCurrentDragStart().x,
                event.getY() - getCurrentDragStart().y);
        CanvasCamera camera = getCanvasCamera();
        dragTranslation.x /= camera.getScale();
        dragTranslation.y /= camera.getScale();
        PointF startTranslation = getCameraDragStartTranslation();
        camera.setTranslation(dragTranslation.x + startTranslation.x,
                dragTranslation.y + startTranslation.y);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScaleFactor = getCanvasCamera().getScale();
            float scaleFactor = oldScaleFactor * detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            if (scaleFactor != oldScaleFactor) {
                float scaleDifference = scaleFactor / oldScaleFactor;
                float zoomTranslationX = (1 - scaleDifference) * detector.getFocusX() / scaleFactor;
                float zoomTranslationY = (1 - scaleDifference) * detector.getFocusY() / scaleFactor;
                getCanvasCamera().setScaleAndRelativeTranslate(scaleFactor,
                        zoomTranslationX,
                        zoomTranslationY);
                invalidate();
            }
            return true;
        }
    }
}
