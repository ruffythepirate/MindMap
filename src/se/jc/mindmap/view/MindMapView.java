/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view;

import se.jc.library.graphics.ZoomView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import se.jc.mindmap.model.MindMapItem;
import se.jc.mindmap.view.component.Bullet;

/**
 *
 * @author Ruffy
 */
public class MindMapView extends ZoomView {

    private MindMapItem _mindMapRoot;
    private Bullet _bulletRoot;

    public MindMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        generateMap();
        setBulletRoot(new Bullet(_mindMapRoot));
    }

    public MindMapView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    public MindMapView(Context context) {
        super(context);
        generateMap();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        prepareCanvasZoom(canvas);
        
        int width = getWidth();
        int height = getHeight();
        Rect itemBounds = _bulletRoot.getItemBounds();
        int x = (width - itemBounds.width()) / 2;
        int y = (height - itemBounds.height()) / 2;

        _bulletRoot.setPosition(x, y);
        _bulletRoot.render(canvas);

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * @return the _bulletRoot
     */
    protected Bullet getBulletRoot() {
        return _bulletRoot;
    }

    /**
     * @param bulletRoot the _bulletRoot to set
     */
    protected void setBulletRoot(Bullet bulletRoot) {
        this._bulletRoot = bulletRoot;
    }

    private void generateMap() {
        _mindMapRoot = new MindMapItem("Main",
                new MindMapItem("1",
                new MindMapItem("1.1"),
                new MindMapItem("1.2")),
                new MindMapItem("WTF",
                new MindMapItem("This is pretty bloody genious"),
                new MindMapItem("Yeah, damn right!")),
                new MindMapItem("2",
                new MindMapItem("2.1"),
                new MindMapItem("2.2")));
    }
}
