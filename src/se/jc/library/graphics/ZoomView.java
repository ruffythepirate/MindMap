/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.Timer;
import java.util.TimerTask;
import se.jc.library.graphics.CanvasCamera;
import se.jc.library.util.MathHelper;

/**
 *
 * @author Ruffy
 */
public class ZoomView extends View {

    protected static int ACTION_MODE_NONE = 0;
    protected static int ACTION_MODE_DRAG = 1;
    protected static int ACTION_MODE_ZOOM = 2;
    protected static int ACTION_MODE_PAN_IN_CORNERS = 3;
    private static float MIN_ZOOM = 0.25f;
    private static float MAX_ZOOM = 4f;
    protected static int LONGPRESS_TIME = 500;
    
    private long _pressStartTimeInMilliSeconds;
    private boolean _startedDragging;
    private Timer _longpressTimer = new Timer();
    private ScaleGestureDetector _zoomDetector;
    private CanvasCamera _canvasCamera;
    private int _actionMode;
    private PointF _currentDragStart;
    private PointF _cameraDragStartTranslation;
    Handler _guiThreadHandler;

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeZoomComponents();
        _guiThreadHandler = new Handler();
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeZoomComponents();
    }

    public ZoomView(Context context) {
        super(context);
        initializeZoomComponents();

    }

    public float getDragDistanceFromStart(MotionEvent event) {
        PointF dragStart = getCurrentDragStart();
        if (dragStart != null) {
            return (float) MathHelper.GetDistance(dragStart, new PointF(event.getX(), event.getY()));
        }
        return 0.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleTouchStart(event);
                setActionMode(ACTION_MODE_DRAG);
                break;
            case MotionEvent.ACTION_MOVE:
                if (getActionMode() == ACTION_MODE_DRAG) {
                    float distanceFromStart = getDragDistanceFromStart(event);
                    if (distanceFromStart > 10.0f) {
                        stopLongpressTimer();
                        setStartedDragging(true);
                    }
                    if (isStartedDragging()) {
                        updateCameraTranslation(event);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                setActionMode(ACTION_MODE_ZOOM);
                break;

            case MotionEvent.ACTION_UP:
                stopLongpressTimer();
                boolean clickIsFasterThanLongpress = System.currentTimeMillis() - getPressStartTimeInMilliSeconds()
                        < LONGPRESS_TIME;
                if (!isStartedDragging() && clickIsFasterThanLongpress) {
                    onClickEvent(event);
                }
                setActionMode(ACTION_MODE_NONE);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                setActionMode(ACTION_MODE_NONE);
                break;
        }

        _zoomDetector.onTouchEvent(event);
        return true;
    }

    public void onClickEvent(MotionEvent event) {
    }

    private void internalOnLongClickEvent(final PointF rawPoint) {
        _guiThreadHandler.post(new Runnable() {
            public void run() {
                onLongClickEvent(rawPoint);
            }
        });
    }

    public void onLongClickEvent(PointF rawPoint) {
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

    public PointF getAsAbsoluteCoordinate(PointF rawPoint) {
        return getAsAbsoluteCoordinate(rawPoint.x, rawPoint.y);
    }

    public PointF getAsAbsoluteCoordinate(float x, float y) {
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

    private boolean isStartedDragging() {
        return _startedDragging;
    }

    private void setStartedDragging(boolean startedDragging) {
        this._startedDragging = startedDragging;
    }

    protected Timer getLongpressTimer() {
        return _longpressTimer;
    }

    protected void setLongpressTimer(Timer longpressTimer) {
        this._longpressTimer = longpressTimer;
    }

    private void startLongpressTimer(MotionEvent event) {
        stopLongpressTimer();
        Timer currentLongPressTimer = getLongpressTimer();
        final PointF location = new PointF(event.getRawX(), event.getRawY());
        currentLongPressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                internalOnLongClickEvent(location);
            }
        }, LONGPRESS_TIME);
    }

    private void stopLongpressTimer() {
        Timer currentLongPressTimer = getLongpressTimer();
        currentLongPressTimer.cancel();
        setLongpressTimer(new Timer());
    }

    private void handleTouchStart(MotionEvent event) {
        setCurrentDragStart(event.getX(), event.getY());
        initializeCameraDragStartTranslation();
        setStartedDragging(false);
        setPressStartTimeInMilliSeconds(System.currentTimeMillis());
        startLongpressTimer(event);
    }

    protected long getPressStartTimeInMilliSeconds() {
        return _pressStartTimeInMilliSeconds;
    }

    protected void setPressStartTimeInMilliSeconds(long pressStartTimeInMilliSeconds) {
        this._pressStartTimeInMilliSeconds = pressStartTimeInMilliSeconds;
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
