/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view.component;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import se.jc.mindmap.model.MindMapItem;

/**
 *
 * @author Ruffy
 */
public class Bullet {
    // Default Values

    public static final Integer AttributeDefaultPadding = 5;
    public static final Integer AttributeDefaultExternalPadding = 10;
    public static final Integer AttributeDefaultForeColor = Color.BLUE;
    //Attribute Keys
    //Color Keys
    public static final String AttributeForeColor = "color.fore";
    public static final String AttributeForeColorBorder = "color.fore.border";
    public static final String AttributeForeColorText = "color.fore.text";
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
    List<Bullet> _children;
    private Bullet _parent;
    //Different rendering properties.
    private Map<String, Object> _displayAttributes;
    private MindMapItem _contentToWrap;
    //Display data that is cached for efficiency.
    private Rect _textBounds;
    private Rect _itemBounds;
    private Point _position;
    private Paint _borderPaint;
    private Paint _textPaint;

    public Bullet(MindMapItem contentToWrap) {
        _displayAttributes = new Hashtable<String, Object>();
        _children = new ArrayList<Bullet>();
        _contentToWrap = contentToWrap;
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
                renderChildrenToLeft(canvasToRenderOn, 0, _children.size());
                break;
            case ToTheRight:
                renderChildrenToRight(canvasToRenderOn, 0, _children.size());
                break;
        }
    }

    public void renderChildrenCenter(Canvas canvasToRenderOn) {
        int itemsToTheRight = getOptimalNumberOfChildrenToRight();
        renderChildrenToRight(canvasToRenderOn, 0, itemsToTheRight);
        renderChildrenToLeft(canvasToRenderOn, itemsToTheRight, _children.size());
    }

    private int getChildItemsSize(int childStartIndex, int childStopIndex) {
        int itemExternalPadding = getRenderAttribute(AttributeExternalPaddingHeight, AttributeDefaultExternalPadding);
        int totalHeight = -itemExternalPadding;
        for (int i = childStartIndex; i < childStopIndex; i++) {
            totalHeight += _children.get(i).getDesiredHeightWithChildren();
            totalHeight += itemExternalPadding;
        }
        return totalHeight;
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
            Bullet currentBullet = _children.get(i);
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
            Bullet currentBullet = _children.get(i);
            int bulletDesiredHeight = currentBullet.getDesiredHeightWithChildren();
            int y = nextItemTop - bulletDesiredHeight / 2;
            currentBullet.setPosition(x, y);
            currentBullet.renderWithChildren(canvasToRenderOn, BulletRenderStyle.ToTheRight);
            nextItemTop -= bulletDesiredHeight + itemExternalPaddingHeight;
        }
    }

    public void renderThisItem(Canvas canvasToRenderOn) {
        renderBorder(canvasToRenderOn);
        renderText(canvasToRenderOn);
    }

    private void renderBorder(Canvas canvasToRenderOn) {
        Paint borderPaint = getBorderPaint();
        Rect itemBounds = getItemBounds();
        Point position = getPosition();

        canvasToRenderOn.drawRect(position.x,
                position.y - itemBounds.height() / 2,
                position.x + itemBounds.width(),
                position.y + itemBounds.height() / 2,
                borderPaint);
    }

    private void populateChildrenEntities() {
        _children.clear();
        for (MindMapItem childItem : _contentToWrap.getChildren()) {
            Bullet childBullet = new Bullet(childItem);
            _children.add(childBullet);
            childBullet.setParent(this);
        }
    }

    private void renderText(Canvas canvasToRenderOn) {
        String textToRender = getContentText();
        Paint textPaint = getTextPaint();
        Point position = getPosition();
        int leftPadding = getLeftPadding();
        int bottomPadding = getBottomPadding();

        canvasToRenderOn.drawText(textToRender,
                position.x + leftPadding,
                position.y + bottomPadding,
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
        return _contentToWrap;
    }

    /**
     * @param contentToWrap the _contentToWrap to set
     */
    protected void setContentToWrap(MindMapItem contentToWrap) {
        this._contentToWrap = contentToWrap;
    }

    public int getDesiredHeightWithChildren() {
        int betweenChildrenPadding = getRenderAttribute(AttributeExternalPaddingHeight, AttributeDefaultExternalPadding);

        int childrenSize = -betweenChildrenPadding;
        for (Bullet childBullet : _children) {
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
        this._position = position;
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
        for (Bullet child : _children) {
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
}
