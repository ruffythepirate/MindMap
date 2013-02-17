/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view.component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import se.jc.library.util.PointEvaluator;
import se.jc.mindmap.model.MindMapItem;

/**
 *
 * @author Ruffy
 */
public class Bullet implements Observer {

    //Other values Mapping.
    private List<Bullet> _children;
    private Bullet _parent;
    //Different rendering properties.
    private MindMapItem _wrappedContent;
    private StyleScheme _currentStyleScheme;
    private StyleScheme _normalStyleScheme;
    private StyleScheme _ghostStyleScheme;
    //Display data that is cached for efficiency.
    private Rect _textBounds;
    private Rect _itemBounds;
    private Point _position;
    private Point _layoutPosition;
    private Map<Bullet, Path> _cachedPaths;
    //Bullet properties
    private boolean _selected;
    private BulletRenderStyle _childOrientation;
    private StyleSchemeType _styleSchemeType;
    //Bullet Display Properties
    private boolean _highlightLeft;
    private boolean _highlightRight;
    private boolean _runningMoveAnimation;

    private Bullet(MindMapItem contentToWrap) {
        _cachedPaths = new HashMap<Bullet, Path>();
        _children = new ArrayList<Bullet>();
        _normalStyleScheme = new StyleScheme();
        _ghostStyleScheme = new StyleScheme();
        _ghostStyleScheme.setColorAlpha(100);

        setChildOrientation(BulletRenderStyle.Center);
        setCurrentStyleScheme(_normalStyleScheme);

        setContentToWrap(contentToWrap);
        setPosition(0, 0);
        setLayoutPosition(0, 0);
    }

    public static Bullet createBullet(MindMapItem contentToWrap) {
        return new Bullet(contentToWrap);
    }

    public void updateLayout() {
        updateLayoutWithChildren(BulletRenderStyle.Center);
    }

    public void updateLayoutWithChildren(BulletRenderStyle renderStyle) {
        switch (renderStyle) {
            case Center:
                updateLayoutChildrenCenter();
                break;
            case ToTheLeft:
                updateLayoutChildrenToLeft(0, getChildren().size());
                break;
            case ToTheRight:
                updateLayoutChildrenToRight(0, getChildren().size());
                break;
        }
    }

    public void renderChildConnections(Canvas canvasToRenderOn) {
        for (Bullet child : _children) {
            renderChildConnection(child, canvasToRenderOn);
        }
    }

    public void updateLayoutChildrenCenter() {
        int itemsToTheRight = getOptimalNumberOfChildrenToRight();
        updateLayoutChildrenToRight(0, itemsToTheRight);
        updateLayoutChildrenToLeft(itemsToTheRight, getChildren().size());
    }

    public void updatePositionInstantlyRecursive() {
        setPosition(getLayoutPosition());
        for (Bullet child : getChildren()) {
            child.updatePositionInstantlyRecursive();
        }
    }

    public void populateMovementAnimators(List<ValueAnimator> allAnimators, int animationTime) {
        updatePositionPopulateAnimators(allAnimators, animationTime);
        for (Bullet child : getChildren()) {
            child.populateMovementAnimators(allAnimators, animationTime);
        }
    }

    public void updatePositionPopulateAnimators(List<ValueAnimator> allAnimators, int animationTime) {
        if (!isRunningMoveAnimation()
                && (getPosition().x != getLayoutPosition().x
                || getPosition().y != getLayoutPosition().y)) {
            ObjectAnimator animator = ObjectAnimator.ofObject(this, "position", new PointEvaluator(),
                    getPosition(), getLayoutPosition());
            animator.setDuration(animationTime);
            allAnimators.add(animator);

            animator.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    setRunningMoveAnimation(true);
                }

                public void onAnimationEnd(Animator animation) {
                    setRunningMoveAnimation(false);
                }

                public void onAnimationCancel(Animator animation) {
                    setRunningMoveAnimation(false);
                }

                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
    }

    private int getChildItemsSize(int childStartIndex, int childStopIndex) {
        int itemExternalPadding = getStyleScheme().getRenderAttribute(StyleScheme.AttributeExternalPaddingHeight);
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

    public void hoverItem(Bullet hoverBullet, PointF hoverPosition) {
        PointF relativePosition = getRelativePosition(hoverPosition);
        float highlightRelativePlace = relativePosition.x / (float) getItemBounds().width();
        boolean highlightLeft = highlightRelativePlace < 0.5f;
        setHighlightLeft(highlightLeft);
        setHighlightRight(!highlightLeft);
    }

    public DropItemAction getDropItemAction(Bullet hoverBullet, PointF hoverPosition) {
        PointF relativePosition = getRelativePosition(hoverPosition);
        float highlightRelativePlace = relativePosition.x / (float) getItemBounds().width();
        DropItemAction action = DropItemAction.Undefined;
        switch (getChildOrientation()) {
            case Center:
                action = highlightRelativePlace < 0.25 || highlightRelativePlace > 0.75 ? DropItemAction.AddChild : DropItemAction.Swap;
                break;
            case ToTheLeft:
                action = highlightRelativePlace < 0.5 ? DropItemAction.AddChild : DropItemAction.Swap;
                break;
            case ToTheRight:
                action = highlightRelativePlace > 0.5 ? DropItemAction.AddChild : DropItemAction.Swap;
                break;
        }
        return action;
    }

    public void hoverItemLeave(Bullet hoverBullet) {
        setHighlightLeft(false);
        setHighlightRight(false);
    }

    public void updateLayoutChildrenToLeft(int startIndex, int stopIndex) {
        int itemExternalPaddingWidth = getStyleScheme().getRenderAttribute(StyleScheme.AttributeExternalPaddingWidth);

        int childItemsSize = getChildItemsSize(startIndex, stopIndex);
        int endX = getLayoutPosition().x - itemExternalPaddingWidth;

        int itemTop = getLayoutPosition().y + getItemBounds().top;
        int itemExternalPaddingHeight = getStyleScheme().getRenderAttribute(StyleScheme.AttributeExternalPaddingHeight);
        int nextItemTop = itemTop + childItemsSize / 2;

        //render items 
        for (int i = startIndex; i < stopIndex; i++) {
            Bullet currentBullet = getChildren().get(i);
            currentBullet.setChildOrientation(BulletRenderStyle.ToTheLeft);
            int bulletDesiredHeight = currentBullet.getDesiredHeightWithChildren();
            int y = nextItemTop - bulletDesiredHeight / 2;
            currentBullet.setLayoutPosition(endX - currentBullet.getItemBounds().width(), y);
            currentBullet.updateLayoutWithChildren(BulletRenderStyle.ToTheLeft);
            nextItemTop -= bulletDesiredHeight + itemExternalPaddingHeight;
        }
    }

    public void updateLayoutChildrenToRight(int startIndex, int stopIndex) {
        int itemRight = getLayoutPosition().x + getItemBounds().right;
        int itemExternalPaddingWidth = getStyleScheme().getRenderAttribute(StyleScheme.AttributeExternalPaddingWidth);
        int itemTop = getLayoutPosition().y + getItemBounds().top;
        int itemExternalPaddingHeight = getStyleScheme().getRenderAttribute(StyleScheme.AttributeExternalPaddingHeight);

        int childItemsSize = getChildItemsSize(startIndex, stopIndex);

        int nextItemTop = itemTop + childItemsSize / 2;
        int x = itemRight + itemExternalPaddingWidth;

        //render items 
        for (int i = startIndex; i < stopIndex; i++) {
            Bullet currentBullet = getChildren().get(i);
            currentBullet.setChildOrientation(BulletRenderStyle.ToTheRight);
            int bulletDesiredHeight = currentBullet.getDesiredHeightWithChildren();
            int y = nextItemTop - bulletDesiredHeight / 2;
            currentBullet.setLayoutPosition(x, y);
            currentBullet.updateLayoutWithChildren(BulletRenderStyle.ToTheRight);
            nextItemTop -= bulletDesiredHeight + itemExternalPaddingHeight;
        }
    }

    public void renderThisItem(Canvas canvasToRenderOn) {
        renderBackground(canvasToRenderOn);
        renderBorder(canvasToRenderOn);
        renderText(canvasToRenderOn);
    }

    @Override
    public String toString() {
        return getWrappedContent().toString();
    }

    private void renderBackground(Canvas canvasToRenderOn) {
        Paint backgroundPaint = isSelected() ? getStyleScheme().getBackgroundHighlightedPaint() : getStyleScheme().getBackgroundPaint();
        Rect itemBounds = getItemBounds();
        Point position = getPosition();
        canvasToRenderOn.drawRect(position.x,
                position.y,
                position.x + itemBounds.width(),
                position.y + itemBounds.height(),
                backgroundPaint);
        if (!isSelected()) {
            Paint highlightPaint = getStyleScheme().getBackgroundHighlightedPaint();
            if (isHighlightLeft()) {
                canvasToRenderOn.drawRect(position.x,
                        position.y,
                        position.x + itemBounds.width() / 2,
                        position.y + itemBounds.height(),
                        highlightPaint);
            }
            if (isHighlightRight()) {
                canvasToRenderOn.drawRect(position.x + itemBounds.width() / 2,
                        position.y,
                        position.x + itemBounds.width(),
                        position.y + itemBounds.height(),
                        highlightPaint);
            }
        }
    }

    private void renderBorder(Canvas canvasToRenderOn) {
        Paint borderPaint = getStyleScheme().getBorderPaint();
        Rect itemBounds = getItemBounds();
        Point position = getPosition();

        canvasToRenderOn.drawRect(position.x,
                position.y,
                position.x + itemBounds.width(),
                position.y + itemBounds.height(),
                borderPaint);
    }

    private void renderText(Canvas canvasToRenderOn) {
        String textToRender = getContentText();
        Paint textPaint = getStyleScheme().getTextPaint();
        Point position = getPosition();
        int leftPadding = getLeftPadding();
        int bottomPadding = getBottomPadding();
        Rect itemBounds = getItemBounds();

        canvasToRenderOn.drawText(textToRender,
                position.x + leftPadding,
                position.y + bottomPadding + itemBounds.height() / 2,
                textPaint);
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
        int betweenChildrenPadding = getCurrentStyleScheme().getRenderAttribute(StyleScheme.AttributeExternalPaddingHeight);

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
            getStyleScheme().getTextPaint().getTextBounds(text, 0, text.length(), _textBounds);
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

    protected Point getRelativePosition(Point absolutePosition) {
        int x = absolutePosition.x - getPosition().x;
        int y = absolutePosition.y - getPosition().y;
        return new Point(x, y);
    }

    public PointF getRelativePosition(PointF absolutePosition) {
        float x = absolutePosition.x - getPosition().x;
        float y = absolutePosition.y - getPosition().y;
        return new PointF(x, y);
    }

    /**
     * @param position the _position to set
     */
    public void setPosition(Point position) {
        if (_position == null || _position.x != position.x || _position.y != position.y) {
            this._position = position;
            clearCachedPaths();
            if (_parent != null) {
                _parent.clearCachedPaths();
            }
        }
    }

    public void setLayoutPosition(int x, int y) {
        setLayoutPosition(new Point(x, y));
    }

    public void setPosition(int x, int y) {
        setPosition(new Point(x, y));
    }

    protected int getLeftPadding() {
        int leftPadding = getStyleScheme().getRenderAttribute(StyleScheme.AttributePaddingLeft);
        return leftPadding;
    }

    protected int getRightPadding() {
        int rightPadding = getStyleScheme().getRenderAttribute(StyleScheme.AttributePaddingRight);
        return rightPadding;
    }

    protected int getTopPadding() {
        int topPadding = getStyleScheme().getRenderAttribute(StyleScheme.AttributePaddingTop);
        return topPadding;
    }

    protected int getBottomPadding() {
        int bottomPadding = getStyleScheme().getRenderAttribute(StyleScheme.AttributePaddingBottom);
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
        //2. We determine how many items to updateLayout to the right.
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

    private void renderChildConnection(Bullet child, Canvas canvasToRenderOn) {

        Path path = calculateBezierPath(child);
//        Path path = getChildPath(child);
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
        Paint pathPaint = getStyleScheme().getPathPaint();
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
        if (_cachedPaths.size() > 0) {
            _cachedPaths.clear();
        }
    }

    public void update(Observable observable, Object data) {
        MindMapItem updatedMindMapItem = (MindMapItem) observable;

        if (updatedMindMapItem == getWrappedContent()) {
            //We synchronize the children of the node.
            List<Bullet> bulletsToRemove = getBulletsToDelete();
            List<Bullet> bulletsToAdd = getBulletsToAdd();
            for (Bullet bulletToRemove : bulletsToRemove) {
                _children.remove(bulletToRemove);
            }
            for (Bullet bulletToAdd : bulletsToAdd) {
                _children.add(bulletToAdd);
            }
            sortBullets();
        }
        if (_parent != null) {
            _parent.clearCachedPaths();
        }
        clearCachedPaths();
        _textBounds = null;
        _itemBounds = null;

    }

    private void sortBullets() {
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
                bulletsToAdd.add(Bullet.createBullet(child));
            }
        }
        return bulletsToAdd;
    }

    private void setChildren(List<Bullet> children) {
        this._children = children;
    }

    protected StyleScheme getStyleScheme() {
        return getCurrentStyleScheme();
    }

    protected void setStyleScheme(StyleScheme styleScheme) {
        this.setCurrentStyleScheme(styleScheme);
    }

    protected StyleScheme getCurrentStyleScheme() {
        return _currentStyleScheme;
    }

    protected void setCurrentStyleScheme(StyleScheme currentStyleScheme) {
        this._currentStyleScheme = currentStyleScheme;
    }

    private BulletRenderStyle getChildOrientation() {
        return _childOrientation;
    }

    private void setChildOrientation(BulletRenderStyle childOrientation) {
        this._childOrientation = childOrientation;
    }

    public boolean isHighlightLeft() {
        return _highlightLeft;
    }

    public void setHighlightLeft(boolean highlightLeft) {
        this._highlightLeft = highlightLeft;
    }

    public boolean isHighlightRight() {
        return _highlightRight;
    }

    public void setHighlightRight(boolean highlightRight) {
        this._highlightRight = highlightRight;
    }

    public StyleSchemeType getStyleSchemeType() {
        return _styleSchemeType;
    }

    public void setStyleSchemeType(StyleSchemeType styleSchemeType) {
        this._styleSchemeType = styleSchemeType;
        switch (styleSchemeType) {
            case Normal:
                setStyleScheme(_normalStyleScheme);
                break;
            case Ghost:
                setStyleScheme(_ghostStyleScheme);
                break;
            default:
                break;
        }
    }

    public Point getLayoutPosition() {
        return _layoutPosition;
    }

    public void setLayoutPosition(Point layoutPosition) {
        this._layoutPosition = layoutPosition;
    }

    public boolean isRunningMoveAnimation() {
        return _runningMoveAnimation;
    }

    private void setRunningMoveAnimation(boolean runningMoveAnimation) {
        this._runningMoveAnimation = runningMoveAnimation;
    }
}
