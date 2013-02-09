/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ruffy
 */
public class MindMapItem {

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

    public List<MindMapItem> getChildren()
    {
        return _children;
    }
    
    /**
     * @param parent the _parent to set
     */
    public void setParent(MindMapItem parent) {
        this._parent = parent;
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
    }
}
