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

    public CanvasCamera() {
        setTransformMatrix(new Matrix());

    }

    /**
     * @return the _scale
     */
    public float getScale() {
        return _scale;
    }

    /**
     * @param scale the _scale to set
     */
    public void setScale(float scale) {
        this._scale = scale;
        updateTransformMatrix();
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
    
    /**
     * @return the _translation
     */
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

    /**
     * @param translation the _translation to set
     */
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

    /**
     * @return the _transformMatrix
     */
    public Matrix getTransformMatrix() {
        return _transformMatrix;
    }

    /**
     * @param transformMatrix the _transformMatrix to set
     */
    public void setTransformMatrix(Matrix transformMatrix) {
        this._transformMatrix = transformMatrix;
    }
}
