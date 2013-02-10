package se.jc.mindmap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import se.jc.mindmap.view.MindMapView;

public class MainActivity extends Activity
{
    private MindMapView _mindMapView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        _mindMapView = (MindMapView)findViewById(R.id.mindMap); 
    }
    
    public void refreshView(View view)
    {
        _mindMapView.invalidate();
    }
}
