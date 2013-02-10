/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.graphics;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 *
 * @author Ruffy
 */
public class CanvasCamera {

    private float _scale;
    private PointF _translation;
    private Matrix _transformMatrix;
    private Matrix _inverseMatrix;
    private boolean _inverseMatrixShouldBeRecalculated = true;

    public CanvasCamera() {
        setTransformMatrix(new Matrix());

    }

    public float getScale() {
        return _scale;
    }

    public void setScale(float scale) {
        this._scale = scale;
        updateTransformMatrix();
    }

    public PointF getAsUntransformedCoordinates(float x, float y)
    {
//        Matrix transformMatrix = getTransformMatrix();
//        float[] coordinateVector = {x, y};
//        Matrix inverseMatrix = getInverseMatrix();
//        inverseMatrix.mapPoints(coordinateVector);
//        return new PointF(coordinateVector[0], coordinateVector[1]);
        float transformedX = x / _scale - _translation.x;
        float transformedY = y / _scale - _translation.y;
        return new PointF(transformedX, transformedY);
    }

    public void setScaleAndRelativeTranslate(float scale, float relativeTranslateX, float relativeTranslateY)
    {
        _scale = scale;
        translate(relativeTranslateX, relativeTranslateY);
        updateTransformMatrix();
    }

    private void translate(float relativeTranslateX, float relativeTranslateY)
    {
        PointF currentTranslation = getTranslation();
        setTranslation(currentTranslation.x + relativeTranslateX, currentTranslation.y + relativeTranslateY);
    }
    
    public PointF getTranslation() {
        if (_translation == null) {
            _translation = new PointF();
        }
        return _translation;
    }

    public PointF copyTranslation() {
        PointF currentTranslation = getTranslation();
        return new PointF(currentTranslation.x, currentTranslation.y);
    }

    public void setTranslation(PointF translation) {
        this._translation = translation;

        updateTransformMatrix();
    }

    public void updateTransformMatrix() {
        Matrix transformMatrix = getTransformMatrix();
        transformMatrix.reset();

        PointF translation = getTranslation();
        transformMatrix.preTranslate(translation.x, translation.y);
        transformMatrix.postScale(_scale, _scale);
    }

    public void setTranslation(float x, float y) {
        if (_translation == null) {
            _translation = new PointF(x, y);
        } else {
            this._translation.set(x, y);
        }
        updateTransformMatrix();
    }

    public Matrix getTransformMatrix() {
        return _transformMatrix;
    }

    public void setTransformMatrix(Matrix transformMatrix) {
        this._transformMatrix = transformMatrix;
    }

    protected Matrix getInverseMatrix() {
        if(_inverseMatrix == null)
        {
            _inverseMatrixShouldBeRecalculated = true;
            _inverseMatrix = new Matrix();
        }
        if(_inverseMatrixShouldBeRecalculated)
        {
            Matrix transformMatrix = getTransformMatrix();
            if(!transformMatrix.invert(_inverseMatrix))
            {
                _inverseMatrix.reset();
            }
            _inverseMatrixShouldBeRecalculated = false;
        }
        return _inverseMatrix;
    }
}
