/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.library.util;

import android.graphics.PointF;

/**
 *
 * @author Ruffy
 */
public class MathHelper {
    public static double GetDistance(PointF pointOne, PointF pointTwo)
    {
        return Math.sqrt( Math.pow(pointOne.x - pointTwo.x, 2.0) +Math.pow(pointOne.y - pointTwo.y, 2.0) );
    }
}
