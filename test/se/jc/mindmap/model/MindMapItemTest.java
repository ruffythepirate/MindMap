/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.model;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ruffy
 */
public class MindMapItemTest extends TestCase{
    
    public MindMapItemTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSwapNodes()
    {
        MindMapItem itemTwo = new MindMapItem("2");
        MindMapItem itemOne = new MindMapItem("1", itemTwo);
        
        MindMapItem.SwapItems(itemOne, itemTwo);
        
        Assert.assertEquals(itemOne.getParent(), itemTwo);
        Assert.assertNull(itemTwo.getParent());
    }
    
    @Test
    public void testAddChild_MindMapItem() {
        MindMapItem itemThree = new MindMapItem("3");
        MindMapItem itemTwo = new MindMapItem("2", itemThree);
        MindMapItem itemOne = new MindMapItem("1", itemTwo);
        
        itemOne.addChild(itemThree);
               
        Assert.assertEquals(2, itemOne.getChildren().size());
        Assert.assertEquals(0, itemTwo.getChildren().size());
        Assert.assertEquals(0, itemThree.getChildren().size());
        Assert.assertEquals(itemOne, itemTwo.getParent());
        Assert.assertEquals(itemOne, itemThree.getParent());
    }

    @Test
    public void testAddChild_ToParent() {
        MindMapItem itemTwo = new MindMapItem("2");
        MindMapItem itemOne = new MindMapItem("1", itemTwo);
        
        itemOne.addChild(itemTwo);
               
        Assert.assertEquals(1, itemOne.getChildren().size());
        Assert.assertEquals(0, itemTwo.getChildren().size());
        Assert.assertEquals(itemOne, itemTwo.getParent());
    }

    @Test
    public void testAddChild_RemovedFromParent() {
        MindMapItem itemTwo = new MindMapItem("2");
        MindMapItem itemOne = new MindMapItem("1", itemTwo);
        MindMapItem itemThree = new MindMapItem("3");

        itemThree.addChild(itemTwo);
               
        Assert.assertEquals(0, itemOne.getChildren().size());
        Assert.assertEquals(1, itemThree.getChildren().size());
        Assert.assertEquals(0, itemTwo.getChildren().size());
        Assert.assertEquals(itemThree, itemTwo.getParent());
    }


}
