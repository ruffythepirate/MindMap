/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view;

import android.view.View;
import se.jc.mindmap.view.component.Bullet;

/**
 *
 * @author Ruffy
 */
public interface OnSelectedBulletChangeListener {
    void onSelectedBulletChanged(View v, Bullet selectedBullet );
}
