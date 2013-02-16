/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.util;

import android.animation.TypeEvaluator;
import android.graphics.Point;

/**
 *
 * @author Ruffy
 */
public class PointEvaluator implements TypeEvaluator {

    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Point startPoint = (Point) startValue;
        Point endPoint = (Point) endValue;
        return new Point((int) (startPoint.x + fraction * (endPoint.x - startPoint.x)),
                (int) (startPoint.y + fraction * (endPoint.y - startPoint.y)));
    }
}