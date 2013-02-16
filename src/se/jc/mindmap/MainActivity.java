package se.jc.mindmap;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import se.jc.mindmap.model.MindMapItem;
import se.jc.mindmap.view.MindMapItemActionRequestListener;
import se.jc.mindmap.view.MindMapView;
import se.jc.mindmap.view.OnSelectedBulletChangeListener;
import se.jc.mindmap.view.component.Bullet;

public class MainActivity extends Activity implements TextWatcher {

    private MindMapView _mindMapView;
    private LinearLayout _actionPanel;
    private EditText _editTextView;
    private MindMapEditMode _currentEditMode;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        _mindMapView = (MindMapView) findViewById(R.id.mindMap);
        _mindMapView.setOnSelectedBulletChangeListener(_selectedBulletChangeListener);
        _mindMapView.setMindMapActionListener(_mindMapItemActionRequestListener);

        _actionPanel = (LinearLayout) findViewById(R.id.actionPanel);

        intializeEditTextView();

    }

    private void intializeEditTextView() {
        _editTextView = (EditText) findViewById(R.id.editText);
        _editTextView.addTextChangedListener(this);
        _editTextView.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH
                                || actionId == EditorInfo.IME_ACTION_DONE
                                || event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (!event.isShiftPressed()) {
                                // the user is done typing. 
                                MindMapItem selectedItem = _mindMapView.getSelectedMindMapItem();

                                if (_currentEditMode == MindMapEditMode.Edit) {
                                    _currentEditMode = MindMapEditMode.Undefined;
                                    _editTextView.setVisibility(View.GONE);
                                    return true; // consume.
                                } else if (_currentEditMode == MindMapEditMode.AddChild && selectedItem != null) {
                                    selectedItem.addChild(_editTextView.getText().toString());
                                    _editTextView.setText("");
                                    _mindMapView.invalidate();

                                } else if (_currentEditMode == MindMapEditMode.AddSibling && selectedItem != null) {
                                    selectedItem.addSibling(_editTextView.getText().toString());
                                    _editTextView.setText("");
                                    _mindMapView.invalidate();
                                }
                            }
                        }
                        return false; // pass on to other listeners. 
                    }
                });
    }

    public void refreshView(View view) {
        _mindMapView.invalidate();
    }

    public void onEditClicked(View v) {
        if (_mindMapView.getSelectedMindMapItem() != null) {
            _currentEditMode = MindMapEditMode.Edit;
            _editTextView.setText(_mindMapView.getSelectedMindMapItem().getText());
            _editTextView.setVisibility(View.VISIBLE);
            _editTextView.requestFocus();
        }
    }

    public void onAddSiblingClicked(View v) {
        if (_mindMapView.getSelectedMindMapItem() != null) {
            _currentEditMode = MindMapEditMode.AddSibling;
            _editTextView.setText("");
            _editTextView.setVisibility(View.VISIBLE);
            _editTextView.requestFocus();
        }
    }

    public void onAddChildClicked(View v) {
        if (_mindMapView.getSelectedMindMapItem() != null) {
            _currentEditMode = MindMapEditMode.AddChild;
            _editTextView.setText("");
            _editTextView.setVisibility(View.VISIBLE);
            _editTextView.requestFocus();
        }
    }

    public void onDeleteSelectedClicked(View v) {
        if (_mindMapView.getSelectedMindMapItem() != null) {
            _mindMapView.getSelectedMindMapItem().deleteOne();
            _mindMapView.setSelectedBullet(null);
        }
    }

    public void onDeleteBranchClicked(View v) {
        if (_mindMapView.getSelectedMindMapItem() != null) {
            _mindMapView.getSelectedMindMapItem().deleteBranch();
            _mindMapView.setSelectedBullet(null);
        }
    }
    private OnSelectedBulletChangeListener _selectedBulletChangeListener = new OnSelectedBulletChangeListener() {
        public void onSelectedBulletChanged(View v, Bullet selectedBullet) {
            if (selectedBullet != null) {
                _actionPanel.setVisibility(View.VISIBLE);
            } else {
                _actionPanel.setVisibility(View.GONE);
                _editTextView.setVisibility(View.GONE);
            }
        }
    };
    
    private MindMapItemActionRequestListener _mindMapItemActionRequestListener = new MindMapItemActionRequestListener() {

        public boolean requestBecomeChild(MindMapItem requestedParent, MindMapItem requestChild, int index) {
            requestedParent.addChild(requestChild);
            return true;
        }

        public boolean requestSwap(MindMapItem firstItem, MindMapItem secondItem) {
            if(firstItem == null || secondItem == null || firstItem == secondItem)
            {
                return false;
            }
            MindMapItem.SwapItems(firstItem, secondItem);
            return true;
        }
    };
            
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        if (_currentEditMode == MindMapEditMode.Edit) {
            String newText = _editTextView.getText().toString();
            _mindMapView.getSelectedMindMapItem().setText(newText);
            _mindMapView.invalidate();
        }
    }
}
