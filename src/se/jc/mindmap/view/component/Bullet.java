/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view.component;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import se.jc.mindmap.model.MindMapItem;

/**
 *
 * @author Ruffy
 */
public class Bullet implements Observer {
    // Default Values

    public static final Integer AttributeDefaultPadding = 5;
    public static final Integer AttributeDefaultExternalPadding = 10;
    public static final Integer AttributeDefaultForeColor = Color.BLUE;
    public static final Integer AttributeDefaultBackgroundColor = Color.GRAY;
    public static final Integer AttributeDefaultBackgroundHighlightedColor = Color.MAGENTA;
    //Attribute Keys
    //Color Keys
    public static final String AttributeForeColor = "color.fore";
    public static final String AttributeForeColorBorder = "color.fore.border";
    public static final String AttributeForeColorText = "color.fore.text";
    public static final String AttributeBackgroundColor = "color.back";
    public static final String AttributeBackgroundColorHighlighted = "color.back.highlighted";
    //Padding Keys
    public static final String AttributePadding = "padding";
    public static final String AttributePaddingLeft = "padding.left";
    public static final String AttributePaddingRight = "padding.right";
    public static final String AttributePaddingTop = "padding.top";
    public static final String AttributePaddingBottom = "padding.bottom";
    public static final String AttributeExternalPaddingHeight = "externalpadding.height";
    public static final String AttributeExternalPaddingWidth = "externalpadding.width";
    public static final String AttributeExternalPadding = "externalpadding";
    //Other values Mapping.
    private List<Bullet> _children;
    private Bullet _parent;
    //Different rendering properties.
    private Map<String, Object> _displayAttributes;
    private MindMapItem _wrappedContent;
    //Display data that is cached for efficiency.
    private Rect _textBounds;
    private Rect _itemBounds;
    private Point _position;
    private Paint _borderPaint;
    private Paint _textPaint;
    private Paint _backgroundPaint;
    private Paint _backgroundHighlightedPaint;
    private Paint _pathPaint;
    private Map<Bullet, Path> _cachedPaths;
    //Bullet properties
    private boolean _selected;

    public Bullet(MindMapItem contentToWrap) {
        _displayAttributes = new Hashtable<String, Object>();
        _cachedPaths = new HashMap<Bullet, Path>();
        _children = new ArrayList<Bullet>();
        setContentToWrap(contentToWrap);
        setPosition(0, 0);

        populateChildrenEntities();
    }

    public void render(Canvas canvasToRenderOn) {
        renderWithChildren(canvasToRenderOn, BulletRenderStyle.Center);
    }

    public void renderWithChildren(Canvas canvasToRenderOn, BulletRenderStyle renderStyle) {
        renderThisItem(canvasToRenderOn);
        switch (renderStyle) {
            case Center:
                renderChildrenCenter(canvasToRenderOn);
                break;
            case ToTheLeft:
                renderChildrenToLeft(canvasToRenderOn, 0, getChildren().size());
                break;
            case ToTheRight:
                renderChildrenToRight(canvasToRenderOn, 0, getChildren().size());
                break;
        }
        renderChildConnections(canvasToRenderOn);
    }

    private void renderChildConnections(Canvas canvasToRenderOn) {
        for (Bullet child : _children) {
            renderChildConnection(child, canvasToRenderOn);
        }
    }

    public void renderChildrenCenter(Canvas canvasToRenderOn) {
        int itemsToTheRight = getOptimalNumberOfChildrenToRight();
        renderChildrenToRight(canvasToRenderOn, 0, itemsToTheRight);
        renderChildrenToLeft(canvasToRenderOn, itemsToTheRight, getChildren().size());
    }

    private int getChildItemsSize(int childStartIndex, int childStopIndex) {
        int itemExternalPadding = getRenderAttribute(AttributeExternalPaddingHeight, AttributeDefaultExternalPadding);
        int totalHeight = -itemExternalPadding;
        for (int i = childStartIndex; i < childStopIndex; i++) {
            totalHeight += getChildren().get(i).getDesiredHeightWithChildren();
            totalHeight += itemExternalPadding;
        }
        return totalHeight;
    }

    public PointF getRightConnectPoint() {
        Point position = getPosition();
        Rect itemBounds = getItemBounds();
        return new PointF(position.x + itemBounds.width(), position.y + itemBounds.height() / 2);
    }

    public PointF getLeftConnectPoint() {
        Point position = getPosition();
        Rect itemBounds = getItemBounds();
        return new PointF(position.x, position.y + itemBounds.height() / 2);
    }

    public void renderChildrenToLeft(Canvas canvasToRenderOn, int startIndex, int stopIndex) {
        int itemExternalPaddingWidth = getRenderAttribute(AttributeExternalPaddingWidth, AttributeDefaultExternalPadding);

        int childItemsSize = getChildItemsSize(startIndex, stopIndex);
        int endX = getPosition().x - itemExternalPaddingWidth;

        int itemTop = getPosition().y + getItemBounds().top;
        int itemExternalPaddingHeight = getRenderAttribute(AttributeExternalPaddingHeight, AttributeDefaultExternalPadding);
        int nextItemTop = itemTop + childItemsSize / 2;

        //render items 
        for (int i = startIndex; i < stopIndex; i++) {
            Bullet currentBullet = getChildren().get(i);
            int bulletDesiredHeight = currentBullet.getDesiredHeightWithChildren();
            int y = nextItemTop - bulletDesiredHeight / 2;
            currentBullet.setPosition(endX - currentBullet.getItemBounds().width(), y);
            currentBullet.renderWithChildren(canvasToRenderOn, BulletRenderStyle.ToTheLeft);
            nextItemTop -= bulletDesiredHeight + itemExternalPaddingHeight;
        }
    }

    public void renderChildrenToRight(Canvas canvasToRenderOn, int startIndex, int stopIndex) {
        int itemRight = getPosition().x + getItemBounds().right;
        int itemExternalPaddingWidth = getRenderAttribute(AttributeExternalPaddingWidth, AttributeDefaultExternalPadding);
        int itemTop = getPosition().y + getItemBounds().top;
        int itemExternalPaddingHeight = getRenderAttribute(AttributeExternalPaddingHeight, AttributeDefaultExternalPadding);

        int childItemsSize = getChildItemsSize(startIndex, stopIndex);

        int nextItemTop = itemTop + childItemsSize / 2;
        int x = itemRight + itemExternalPaddingWidth;

        //render items 
        for (int i = startIndex; i < stopIndex; i++) {
            Bullet currentBullet = getChildren().get(i);
            int bulletDesiredHeight = currentBullet.getDesiredHeightWithChildren();
            int y = nextItemTop - bulletDesiredHeight / 2;
            currentBullet.setPosition(x, y);
            currentBullet.renderWithChildren(canvasToRenderOn, BulletRenderStyle.ToTheRight);
            nextItemTop -= bulletDesiredHeight + itemExternalPaddingHeight;
        }
    }

    public void renderThisItem(Canvas canvasToRenderOn) {
        renderBackground(canvasToRenderOn);
        renderBorder(canvasToRenderOn);
        renderText(canvasToRenderOn);
    }

    private void renderBackground(Canvas canvasToRenderOn) {
        Paint backgroundPaint = isSelected() ? getBackgroundHighlightedPaint() : getBackgroundPaint();
        Rect itemBounds = getItemBounds();
        Point position = getPosition();


        canvasToRenderOn.drawRect(position.x,
                position.y,
                position.x + itemBounds.width(),
                position.y + itemBounds.height(),
                backgroundPaint);
    }

    private void renderBorder(Canvas canvasToRenderOn) {
        Paint borderPaint = getBorderPaint();
        Rect itemBounds = getItemBounds();
        Point position = getPosition();

        canvasToRenderOn.drawRect(position.x,
                position.y,
                position.x + itemBounds.width(),
                position.y + itemBounds.height(),
                borderPaint);
    }

    private void populateChildrenEntities() {
        getChildren().clear();
        for (MindMapItem childItem : getWrappedContent().getChildren()) {
            Bullet childBullet = new Bullet(childItem);
            getChildren().add(childBullet);
            childBullet.setParent(this);
        }
    }

    private void renderText(Canvas canvasToRenderOn) {
        String textToRender = getContentText();
        Paint textPaint = getTextPaint();
        Point position = getPosition();
        int leftPadding = getLeftPadding();
        int bottomPadding = getBottomPadding();
        Rect itemBounds = getItemBounds();

        canvasToRenderOn.drawText(textToRender,
                position.x + leftPadding,
                position.y + bottomPadding + itemBounds.height() / 2,
                textPaint);
    }

    public void setRenderAttribute(String key, Object value) {
        _displayAttributes.put(key, value);
    }

    public <T> T getRenderAttribute(String key, T defaultValue) {
        while (key != null && key.length() > 0) {
            if (_displayAttributes.containsKey(key)) {
                return (T) _displayAttributes.get(key);
            }
            key = key.lastIndexOf(".") > 0
                    ? key.substring(0, key.lastIndexOf("."))
                    : null;
        }
        return defaultValue;
    }

    /**
     * @return the _contentToWrap
     */
    protected MindMapItem getContentToWrap() {
        return getWrappedContent();
    }

    /**
     * @param contentToWrap the _contentToWrap to set
     */
    protected void setContentToWrap(MindMapItem contentToWrap) {
        this.setWrappedContent(contentToWrap);
    }

    public int getDesiredHeightWithChildren() {
        int betweenChildrenPadding = getRenderAttribute(AttributeExternalPaddingHeight, AttributeDefaultExternalPadding);

        int childrenSize = -betweenChildrenPadding;
        for (Bullet childBullet : getChildren()) {
            childrenSize += childBullet.getDesiredHeightWithChildren();
            childrenSize += betweenChildrenPadding;
        }

        int ownDesiredHeight = getItemDesiredHeight();

        return ownDesiredHeight > childrenSize ? ownDesiredHeight : childrenSize;
    }

    public int getItemDesiredHeight() {
        Rect itemBounds = getItemBounds();
        return itemBounds.height();
    }

    /**
     * @return the _textBounds
     */
    protected Rect getTextBounds() {
        if (_textBounds == null) {
            _textBounds = new Rect();
            String text = getContentText();
            getTextPaint().getTextBounds(text, 0, text.length(), _textBounds);
        }
        return _textBounds;
    }

    public Rect getItemBounds() {
        if (_itemBounds == null) {
            _itemBounds = new Rect();
            Rect textBounds = getTextBounds();
            int rightPadding = getRightPadding();
            int topPadding = getTopPadding();
            int leftPadding = getLeftPadding();
            int bottomPadding = getBottomPadding();

            _itemBounds.set(0, 0,
                    textBounds.width() + leftPadding + rightPadding,
                    textBounds.height() + topPadding + bottomPadding);
        }
        return _itemBounds;
    }

    private String getContentText() {
        MindMapItem item = getContentToWrap();
        if (item != null) {
            return item.getText();
        }
        return "";
    }

    public boolean containsPoint(PointF point) {
        Point position = getPosition();
        int relativeX = (int) point.x - position.x;
        int relativeY = (int) point.y - position.y;

        Rect itemBounds = getItemBounds();
        return itemBounds.contains(relativeX, relativeY);
    }

    /**
     * @return the _position
     */
    protected Point getPosition() {
        return _position;
    }

    /**
     * @param position the _position to set
     */
    public void setPosition(Point position) {
        if (_position == null || _position.x != position.x || _position.y != position.y) {
            this._position = position;
            clearCachedPaths();
        }
    }

    public void setPosition(int x, int y) {
        setPosition(new Point(x, y));
    }

    /**
     * @return the _borderPaint
     */
    protected Paint getBorderPaint() {
        if (_borderPaint == null) {
            int borderColor = getRenderAttribute(AttributeForeColorBorder, AttributeDefaultForeColor);
            _borderPaint = new Paint();
            _borderPaint.setStyle(Paint.Style.STROKE);
            _borderPaint.setColor(borderColor);
        }
        return _borderPaint;
    }

    /**
     * @return the _textPaint
     */
    protected Paint getTextPaint() {
        if (_textPaint == null) {
            _textPaint = new Paint();
            int textColor = getRenderAttribute(AttributeForeColorText, AttributeDefaultForeColor);
            _textPaint.setColor(textColor);
            _textPaint.setStyle(Paint.Style.STROKE);
            _textPaint.setAntiAlias(true);
        }
        return _textPaint;
    }

    protected int getLeftPadding() {
        int leftPadding = getRenderAttribute(AttributePaddingLeft, AttributeDefaultPadding);
        return leftPadding;
    }

    protected int getRightPadding() {
        int rightPadding = getRenderAttribute(AttributePaddingRight, AttributeDefaultPadding);
        return rightPadding;
    }

    protected int getTopPadding() {
        int topPadding = getRenderAttribute(AttributePaddingTop, AttributeDefaultPadding);
        return topPadding;
    }

    protected int getBottomPadding() {
        int bottomPadding = getRenderAttribute(AttributePaddingBottom, AttributeDefaultPadding);
        return bottomPadding;
    }

    /**
     * @return the _parent
     */
    public Bullet getParent() {
        return _parent;
    }

    /**
     * @param parent the _parent to set
     */
    public void setParent(Bullet parent) {
        this._parent = parent;
    }

    protected int getOptimalNumberOfChildrenToRight() {
        //1. We determine total height.
        int totalChildrenHeight = getDesiredHeightWithChildren();
        int desiredHeightOnEachSide = totalChildrenHeight / 2;
        //2. We determine how many items to render to the right.
        int accumulatedHeight = 0;
        int itemsToTheRight = 0;
        int closestMatch = Integer.MAX_VALUE;
        for (Bullet child : getChildren()) {
            accumulatedHeight += child.getDesiredHeightWithChildren();
            if (Math.abs(accumulatedHeight - desiredHeightOnEachSide) <= closestMatch) {
                itemsToTheRight++;
                closestMatch = Math.abs(accumulatedHeight - desiredHeightOnEachSide);
            } else {
                break;
            }
        }
        return itemsToTheRight;
    }

    public List<Bullet> getChildren() {
        return _children;
    }

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean selected) {
        this._selected = selected;
    }

    protected Paint getBackgroundPaint() {
        if (_backgroundPaint == null) {
            _backgroundPaint = new Paint();
            int color = getRenderAttribute(AttributeBackgroundColor, AttributeDefaultBackgroundColor);
            _backgroundPaint.setColor(color);
            _backgroundPaint.setStyle(Paint.Style.FILL);
        }
        return _backgroundPaint;
    }

    protected Paint getBackgroundHighlightedPaint() {
        if (_backgroundHighlightedPaint == null) {
            _backgroundHighlightedPaint = new Paint();
            int color = getRenderAttribute(AttributeBackgroundColorHighlighted, AttributeDefaultBackgroundHighlightedColor);
            _backgroundHighlightedPaint.setColor(color);
            _backgroundHighlightedPaint.setStyle(Paint.Style.FILL);
        }
        return _backgroundHighlightedPaint;
    }

    protected Paint getPathPaint() {
        if (_pathPaint == null) {
            _pathPaint = new Paint();
            int color = getRenderAttribute(AttributeForeColorBorder, AttributeDefaultForeColor);
            _pathPaint.setColor(color);
            _pathPaint.setStyle(Paint.Style.STROKE);
            _pathPaint.setAntiAlias(true);
        }
        return _pathPaint;
    }

    private void renderChildConnection(Bullet child, Canvas canvasToRenderOn) {

        Path path = getChildPath(child);
        renderConnectionPath(path, canvasToRenderOn);
    }

    private Path getChildPath(Bullet child) {
        if (!getCachedPaths().containsKey(child)) {
            Path calculatedPath = calculateBezierPath(child);
            getCachedPaths().put(child, calculatedPath);
        }
        return getCachedPaths().get(child);
    }

    private void renderConnectionPath(Path path, Canvas canvasToRenderOn) {
        Paint pathPaint = getPathPaint();
        canvasToRenderOn.drawPath(path, pathPaint);
    }

    private Path calculateBezierPath(PointF startPoint, PointF endPoint) {
        float offset = (startPoint.x - endPoint.x) / 2.0f;
        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        float x2 = (endPoint.x + startPoint.x) / 2;
        float y2 = (endPoint.y + startPoint.y) / 2;
        path.quadTo(startPoint.x - offset, startPoint.y, x2, y2);
        path.quadTo(endPoint.x + offset, endPoint.y, endPoint.x, endPoint.y);
        return path;
    }

    private Path calculateBezierPath(Bullet child) {
        int xDiff = getPosition().x - child.getPosition().x;
        PointF endPoint;
        PointF startPoint;
        if (xDiff < 0) {
            startPoint = getRightConnectPoint();
            endPoint = child.getLeftConnectPoint();
        } else {
            startPoint = child.getRightConnectPoint();
            endPoint = getLeftConnectPoint();
        }
        Path path = calculateBezierPath(startPoint, endPoint);
        return path;
    }

    protected Map<Bullet, Path> getCachedPaths() {
        if (_cachedPaths == null) {
            _cachedPaths = new Hashtable<Bullet, Path>();
        }
        return _cachedPaths;
    }

    public MindMapItem getWrappedContent() {
        return _wrappedContent;
    }

    public void setWrappedContent(MindMapItem wrappedContent) {
        if (_wrappedContent != null) {
            _wrappedContent.deleteObserver(this);
        }
        wrappedContent.addObserver(this);
        this._wrappedContent = wrappedContent;
    }

    private void clearCachedPaths() {
        _cachedPaths.clear();
    }

    public void update(Observable observable, Object data) {
        MindMapItem updatedMindMapItem = (MindMapItem) observable;

        if (updatedMindMapItem == getWrappedContent()) {
            //We synchronize the children of the node.
            List<Bullet> bulletsToRemove = getBulletsToDelete();
            List<Bullet> bulletsToAdd = getBulletsToAdd();
            for(Bullet bulletToRemove : bulletsToRemove)
            {
                _children.remove(bulletToRemove);
            }
            for(Bullet bulletToAdd : bulletsToAdd)
            {
                _children.remove(bulletToAdd);
            }
            sortBullets();
        }
        clearCachedPaths();
        _textBounds = null;
        _itemBounds = null;
        
    }

    private void sortBullets()
    {
        List<Bullet> newBulletsList = new ArrayList<Bullet>();
        for (MindMapItem child : getWrappedContent().getChildren()) {
            boolean bulletFound = false;
            for (Bullet currentChildBullet : getChildren()) {
                if (currentChildBullet.getWrappedContent() == child) {
                    newBulletsList.add(currentChildBullet);
                    break;
                }
            }
        }
        setChildren(newBulletsList);
    }
    
    private List<Bullet> getBulletsToDelete() {
        List<Bullet> bulletsToDelete = new ArrayList<Bullet>();
        for (Bullet currentChildBullet : getChildren()) {
            boolean bulletFound = false;
            for (MindMapItem child : getWrappedContent().getChildren()) {
                if (currentChildBullet.getWrappedContent() == child) {
                    bulletFound = true;
                    break;
                }
            }
            if (!bulletFound) {
                bulletsToDelete.add(currentChildBullet);
            }
        }
        return bulletsToDelete;
    }

    private List<Bullet> getBulletsToAdd() {
        List<Bullet> bulletsToAdd = new ArrayList<Bullet>();
        for (MindMapItem child : getWrappedContent().getChildren()) {
            boolean bulletFound = false;
            for (Bullet currentChildBullet : getChildren()) {
                if (currentChildBullet.getWrappedContent() == child) {
                    bulletFound = true;
                    break;
                }
            }
            if (!bulletFound) {
                bulletsToAdd.add(new Bullet(child));
            }
        }
        return bulletsToAdd;
    }

    private void setChildren(List<Bullet> children) {
        this._children = children;
    }
}
