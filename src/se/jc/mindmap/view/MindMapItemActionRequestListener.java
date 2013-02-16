/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view;

import se.jc.mindmap.model.MindMapItem;

/**
 *
 * @author Ruffy
 */
public interface MindMapItemActionRequestListener {
   boolean requestBecomeChild(MindMapItem requestedParent, MindMapItem requestChild, int index);
   boolean requestSwap(MindMapItem firstItem, MindMapItem secondItem);
}
