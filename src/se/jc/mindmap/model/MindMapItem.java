/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import se.jc.library.util.SuspendableObservable;

/**
 *
 * @author Ruffy
 */
public class MindMapItem extends SuspendableObservable {

    private MindMapItem _parent;
    private String _text;
    private List<MindMapItem> _children;

    public MindMapItem(String title, MindMapItem... mindMapItems) {
        _children = new ArrayList<MindMapItem>();

        setText(title);
        if (mindMapItems != null) {
            for (MindMapItem item : mindMapItems) {
                item.setParent(this);
                _children.add(item);
            }
        }
    }

    /**
     * @return the _parent
     */
    public MindMapItem getParent() {
        return _parent;
    }

    public int getChildIndex(MindMapItem mindMapItem) {
        return _children.indexOf(mindMapItem);
    }

    public void removeChild(MindMapItem childToRemove) {
        _children.remove(childToRemove);
        setChangeAndNotifyObservers(this);
    }

    public void addChild(int index, MindMapItem childToAdd) {
        _children.add(index, childToAdd);
        childToAdd.setParent(this);
        setChangeAndNotifyObservers(this);
    }

    public void addChild(MindMapItem childToAdd) {

        _children.add(childToAdd);
        childToAdd.setParent(this);
        setChangeAndNotifyObservers(this);
    }

    public void addChild(String childText) {
        MindMapItem childToAdd = new MindMapItem(childText);
        addChild(childToAdd);
    }

    public void addSibling(String childText) {
        if (_parent != null) {
            MindMapItem childToAdd = new MindMapItem(childText);
            int currentIndex = _parent.getChildIndex(this);
            _parent.addChild(currentIndex + 1, childToAdd);
            setChangeAndNotifyObservers(this);
        }
    }

    public List<MindMapItem> getChildren() {
        return _children;
    }

    public void setChildren(List<MindMapItem> childrenToSet) {
        _children.clear();
        for (MindMapItem child : childrenToSet) {
            if (child != this) {
                child.setParent(this);
                _children.add(child);
            }
        }
        setChangeAndNotifyObservers(this);
    }

    public List<MindMapItem> copyChildren() {
        ArrayList<MindMapItem> childrenCopy = new ArrayList<MindMapItem>(_children.size());
        for (MindMapItem child : _children) {
            childrenCopy.add(child);
        }
        return childrenCopy;
    }

    /**
     * @param parent the _parent to set
     */
    private void setParent(MindMapItem parent) {
        if (_parent != null && _parent != parent) {
            _parent.removeChild(this);
        }
        this._parent = parent;
    }

    public void clearParent() {
        setParent(null);
    }

    /**
     * @return the _text
     */
    public String getText() {
        return _text;
    }

    /**
     * @param text the _text to set
     */
    public void setText(String text) {
        this._text = text;

        setChangeAndNotifyObservers(this);
    }

    public void deleteOne() {
        if (_parent != null) {
            _parent.suspendBinding();
            for (MindMapItem child : _children) {
                _parent.addChild(0, child);
            }
            _parent.removeChild(this);
            _parent.resumeBinding(_parent);
        }
    }

    public void deleteBranch() {
        if (_parent != null) {
            _parent.removeChild(this);
        }
    }

    public static void SwapItems(MindMapItem itemOne, MindMapItem itemTwo) {
        MindMapItem itemOneParent = itemOne.getParent();
        MindMapItem itemTwoParent = itemTwo.getParent();
        itemOneParent = itemOneParent == itemTwo ? itemOne : itemOneParent;
        itemTwoParent = itemTwoParent == itemOne ? itemTwo : itemTwoParent;

        List<MindMapItem> itemOneChildren = itemOne.copyChildren();
        List<MindMapItem> itemTwoChildren = itemTwo.copyChildren();

        itemOne.suspendBinding();
        itemTwo.suspendBinding();
        itemOne.setChildren(itemTwoChildren);
        itemTwo.setChildren(itemOneChildren);
        if (itemTwoParent != null) {
            itemTwoParent.addChild(itemOne);
        } else {
            itemOne.clearParent();
        }
        if (itemOneParent != null) {
            itemOneParent.addChild(itemTwo);
        } else {
            itemTwo.clearParent();
        }
        itemOne.resumeBinding(itemOne);
        itemTwo.resumeBinding(itemTwo);
    }

    @Override
    public String toString() {
        return getText() + " " + getChildren().size();
    }
    
    
}
