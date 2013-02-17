/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view.component;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import se.jc.mindmap.model.MindMapItem;

/**
 *
 * @author Ruffy
 */
public class BulletManager {

    private Map<MindMapItem, Bullet> _bulletStore;
    private Bullet _rootBullet;

    public BulletManager() {
        _bulletStore = new HashMap<MindMapItem, Bullet>();
    }

    public Bullet getOrCreateBullet(MindMapItem mindMapItem) {
        if (!_bulletStore.containsKey(mindMapItem)) {
            mindMapItem.addObserver(new Observer() {
                public void update(Observable observable, Object data) {
                    BitSet updateMap = (BitSet) data;
                    if (updateMap != null) {
                        if (updateMap.get(MindMapItem.UPDATED_CHILDREN)) {
                            reconstructBulletTree();
                        }
                        if (updateMap.get(MindMapItem.UPDATED_DELETED)) {
                            _bulletStore.remove(observable);
                        }
                    }
                }
            });
            Bullet newBullet = Bullet.createBullet(mindMapItem);
            for (MindMapItem itemChild : mindMapItem.getChildren()) {
                Bullet childBullet = getOrCreateBullet(itemChild);
                newBullet.getChildren().add(childBullet);
            }
            _bulletStore.put(mindMapItem, newBullet);
        }
        return _bulletStore.get(mindMapItem);
    }

    public void reconstructBulletTree() {
        reconstructBulletTree(getRootBullet());
    }

    public void reconstructBulletTree(Bullet bulletToReconstruct) {
        if (bulletToReconstruct != null) {
            bulletToReconstruct.getChildren().clear();
            for (MindMapItem itemChild : bulletToReconstruct.getWrappedContent().getChildren()) {
                Bullet childBullet = getOrCreateBullet(itemChild);
                bulletToReconstruct.getChildren().add(childBullet);
                reconstructBulletTree(childBullet);
            }
        }
    }

    public Bullet createMoveBullet(MindMapItem contentToWrap) {
        return Bullet.createBullet(contentToWrap);
    }

    public Bullet getRootBullet() {
        return _rootBullet;
    }

    public void setRootBullet(Bullet rootBullet) {
        this._rootBullet = rootBullet;
    }
}
