/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view.component;

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
                    createBullet(getRootBullet().getContentToWrap(), true);
                }
            });
            Bullet newBullet = createBullet(mindMapItem, true);
            _bulletStore.put(mindMapItem, newBullet);
        }
        return _bulletStore.get(mindMapItem);
    }

    public Bullet createBullet(MindMapItem mindMapItem, boolean withChildren) {
        Bullet newBullet = Bullet.createBullet(mindMapItem);
        
        if (withChildren) {
            for (MindMapItem childItem : mindMapItem.getChildren()) {
                Bullet childBullet = getOrCreateBullet(childItem);
                newBullet.getChildren().add(childBullet);
            }
        }
        return newBullet;
    }

    public Bullet createBulletWithChildren(MindMapItem contentToWrap) {
        return getOrCreateBullet(contentToWrap);
    }

    public Bullet createMoveBullet(MindMapItem contentToWrap) {
        return createBullet(contentToWrap, false);
    }

    public Bullet getRootBullet() {
        return _rootBullet;
    }

    public void setRootBullet(Bullet rootBullet) {
        this._rootBullet = rootBullet;
    }
}
