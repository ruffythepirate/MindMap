/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view;

import android.graphics.PointF;
import se.jc.library.graphics.ZoomView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import se.jc.mindmap.model.MindMapItem;
import se.jc.mindmap.view.component.Bullet;
import se.jc.mindmap.view.component.DropItemAction;
import se.jc.mindmap.view.component.StyleSchemeType;

/**
 *
 * @author Ruffy
 */
public class MindMapView extends ZoomView {

    private MindMapItem _mindMapRoot;
    private Bullet _bulletRoot;
    private Bullet _selectedBullet;
    private Bullet _moveBullet;
    private PointF _moveBulletInitPosition;
    private Bullet _hoveredItem;
    //Listeners
    private OnSelectedBulletChangeListener _selectedBulletChangeListener;
    private MindMapItemActionRequestListener _mindMapActionListener;

    public MindMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        generateMap();
        setBulletRoot(Bullet.createBulletWithChildren(_mindMapRoot));
    }

    public MindMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MindMapView(Context context) {
        super(context);
        generateMap();
        setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return true;
            }
        });
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
        _bulletRoot.updateLayout();

        renderItemWithChildren(canvas, _bulletRoot);

        Bullet moveBullet = getMoveBullet();
        if (moveBullet != null) {
            moveBullet.renderThisItem(canvas);
        }

        super.onDraw(canvas);
    }

    private void renderItemWithChildren(Canvas canvasToRenderOn, Bullet itemToRender) {
        itemToRender.renderThisItem(canvasToRenderOn);
        itemToRender.renderChildConnections(canvasToRenderOn);
        for (Bullet child : itemToRender.getChildren()) {
            renderItemWithChildren(canvasToRenderOn, child);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClickEvent(MotionEvent event) {
        PointF transformedPoint = getAsAbsoluteCoordinate(event.getRawX(), event.getRawY());

        Bullet foundBullet = getBulletAtCoordinate(getBulletRoot(), transformedPoint);
        if (foundBullet != null
                && foundBullet.getWrappedContent() != getSelectedMindMapItem()) {
            setSelectedBullet(foundBullet);
        } else {
            setSelectedBullet(null);
        }
        super.onClickEvent(event);
    }

    @Override
    /**
     * Initializes dragging of a map point.
     */
    public void onLongClickEvent(PointF rawPoint) {
        //Initializes dragging of a map point.
        PointF transformedPoint = getAsAbsoluteCoordinate(rawPoint.x, rawPoint.y);
        Bullet foundBullet = getBulletAtCoordinate(getBulletRoot(), transformedPoint);
        if (foundBullet != null) {
            //This is where the dragging starts.
            setSelectedBullet(foundBullet);
            setActionMode(ZoomView.ACTION_MODE_PAN_IN_CORNERS);

            Bullet moveBullet = createMoveBullet(foundBullet);
            PointF moveBulletInitPosition = foundBullet.getRelativePosition(transformedPoint);
            setMoveBullet(moveBullet, moveBulletInitPosition);

            updateMoveBulletPosition(moveBullet, rawPoint);
        }
        super.onLongClickEvent(rawPoint);
    }

    private Bullet createMoveBullet(Bullet bulletToMove) {
        Bullet moveBullet = Bullet.createBulletOnlyForItem(bulletToMove.getWrappedContent());
        moveBullet.setStyleSchemeType(StyleSchemeType.Ghost);
        return moveBullet;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Bullet moveBullet = getMoveBullet();
        PointF transformedPoint = getAsAbsoluteCoordinate(event.getRawX(), event.getRawY());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                //See if we are dragging an item.
                if (moveBullet != null) {
                    updateMoveBulletPosition(moveBullet, new PointF(event.getRawX(), event.getRawY()));
                    handleMoveItemHoverMove(transformedPoint, moveBullet);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //See if we should place an object.
                if (moveBullet != null) {

                    Bullet hoveredBullet = getHoveredItem();
                    if (hoveredBullet != null) {
                        hoveredBullet.hoverItemLeave(moveBullet);
                        DropItemAction releaseAction = hoveredBullet.getDropItemAction(moveBullet, transformedPoint);
                        if (handleDropItemAction(hoveredBullet.getWrappedContent(), moveBullet.getWrappedContent(), releaseAction)) {
                            
                        }
                    }
                    setMoveBullet(null, null);
                    invalidate();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean handleDropItemAction(MindMapItem hoveredItem, MindMapItem droppedItem, DropItemAction action) {
        if (getMindMapActionListener() != null) {
            switch (action) {
                case Swap:
                    return getMindMapActionListener().requestSwap(hoveredItem, droppedItem);
                case AddChild:
                    return getMindMapActionListener().requestBecomeChild(hoveredItem, droppedItem, 0);
            }
        }
        return false;
    }

    private Bullet getBulletAtCoordinate(Bullet searchBullet, PointF coordinate) {
        if (searchBullet != null) {
            if (searchBullet.containsPoint(coordinate)) {
                return searchBullet;
            } else {
                for (Bullet childBullet : searchBullet.getChildren()) {
                    Bullet foundBullet = getBulletAtCoordinate(childBullet, coordinate);
                    if (foundBullet != null) {
                        return foundBullet;
                    }
                }
            }
        }
        return null;
    }

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

    /**
     * @return the _selectedBullet
     */
    public MindMapItem getSelectedMindMapItem() {
        if (_selectedBullet != null) {
            return _selectedBullet.getWrappedContent();
        }
        return null;
    }

    /**
     * @param selectedBullet the _selectedBullet to set
     */
    public void setSelectedBullet(Bullet selectedBullet) {
        if (_selectedBullet != selectedBullet) {
            if (_selectedBullet != null) {
                _selectedBullet.setSelected(false);
            }
            this._selectedBullet = selectedBullet;
            if (_selectedBullet != null) {
                _selectedBullet.setSelected(true);
            }
            invalidate();
            OnSelectedBulletChangeListener selectedBulletChanged = getOnSelectedBulletChangeListener();
            if (selectedBulletChanged != null) {
                selectedBulletChanged.onSelectedBulletChanged(this, _selectedBullet);
            }
        }
    }

    public OnSelectedBulletChangeListener getOnSelectedBulletChangeListener() {
        return getSelectedBulletChangeListener();
    }

    public void setOnSelectedBulletChangeListener(OnSelectedBulletChangeListener selectedBulletChangeListener) {
        this.setSelectedBulletChangeListener(selectedBulletChangeListener);
    }

    private Bullet getMoveBullet() {
        return _moveBullet;
    }

    private void setMoveBullet(Bullet bulletToMove, PointF moveBulletInitPosition) {
        _moveBulletInitPosition = moveBulletInitPosition;
        this._moveBullet = bulletToMove;
    }

    private Bullet getHoveredItem() {
        return _hoveredItem;
    }

    private void setHoveredItem(Bullet hoveredItem) {
        this._hoveredItem = hoveredItem;
    }

    private void handleMoveItemHoverMove(PointF transformedPoint, Bullet moveBullet) {
        Bullet foundBullet = getBulletAtCoordinate(getBulletRoot(), transformedPoint);
        boolean isHoveringOtherItem = foundBullet != null
                && foundBullet.getWrappedContent() != moveBullet.getWrappedContent()
                && foundBullet != getHoveredItem();
        if (isHoveringOtherItem) {
            setHoveredItem(foundBullet);
            foundBullet.hoverItem(moveBullet, transformedPoint);
        } else if (getHoveredItem() != null) {
            getHoveredItem().hoverItemLeave(moveBullet);
            setHoveredItem(null);
        }
    }

    private void updateMoveBulletPosition(Bullet moveBullet, PointF rawPoint) {
        if (moveBullet != null) {
            PointF moveBulletInitPosition = getMoveBulletInitPosition();
            if (moveBulletInitPosition != null) {
                moveBullet.setPosition((int) (rawPoint.x - moveBulletInitPosition.x), (int) (rawPoint.y - moveBulletInitPosition.y));
            } else {
                moveBullet.setPosition((int) (rawPoint.x), (int) (rawPoint.y));
            }
        }
    }

    private PointF getMoveBulletInitPosition() {
        return _moveBulletInitPosition;
    }

    public OnSelectedBulletChangeListener getSelectedBulletChangeListener() {
        return _selectedBulletChangeListener;
    }

    public void setSelectedBulletChangeListener(OnSelectedBulletChangeListener selectedBulletChangeListener) {
        this._selectedBulletChangeListener = selectedBulletChangeListener;
    }

    public MindMapItemActionRequestListener getMindMapActionListener() {
        return _mindMapActionListener;
    }

    public void setMindMapActionListener(MindMapItemActionRequestListener mindMapActionListener) {
        this._mindMapActionListener = mindMapActionListener;
    }
}
