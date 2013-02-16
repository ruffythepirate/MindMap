/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.jc.mindmap.view.component;

import android.graphics.Color;
import android.graphics.Paint;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author Ruffy
 */
class StyleScheme {

    // Default Values
    public static final Integer AttributeDefaultPadding = 5;
    public static final Integer AttributeDefaultExternalPadding = 10;
    //Attribute Keys
    //Padding Keys
    public static final String AttributePadding = "padding";
    public static final String AttributePaddingLeft = "padding.left";
    public static final String AttributePaddingRight = "padding.right";
    public static final String AttributePaddingTop = "padding.top";
    public static final String AttributePaddingBottom = "padding.bottom";
    public static final String AttributeExternalPaddingHeight = "externalpadding.height";
    public static final String AttributeExternalPaddingWidth = "externalpadding.width";
    public static final String AttributeExternalPadding = "externalpadding";
    public static final Integer AttributeDefaultForeColor = Color.BLUE;
    public static final Integer AttributeDefaultBackgroundColor = Color.GRAY;
    public static final Integer AttributeDefaultBackgroundHighlightedColor = Color.MAGENTA;
    //Color Keys
    public static final String AttributeForeColor = "color.fore";
    public static final String AttributeForeColorBorder = "color.fore.border";
    public static final String AttributeForeColorText = "color.fore.text";
    public static final String AttributeBackgroundColor = "color.back";
    public static final String AttributeBackgroundColorHighlighted = "color.back.highlighted";
    private Paint _borderPaint;
    private Paint _textPaint;
    private Paint _backgroundPaint;
    private Paint _backgroundHighlightedPaint;
    private Paint _pathPaint;
    private Map<String, Object> _displayAttributes;
    
    private int _colorAlpha;

    public StyleScheme() {
        _displayAttributes = new Hashtable<String, Object>();
        initDefaultValues();
        setColorAlpha(255);
    }

    private void initDefaultValues() {
        setRenderAttribute(AttributeForeColor, AttributeDefaultForeColor);
        setRenderAttribute(AttributeBackgroundColor, AttributeDefaultBackgroundColor);
        setRenderAttribute(AttributeBackgroundColorHighlighted, AttributeDefaultBackgroundHighlightedColor);
        setRenderAttribute(AttributeExternalPadding, AttributeDefaultExternalPadding);
        setRenderAttribute(AttributePadding, AttributeDefaultPadding);
    }

    /**
     * @return the _borderPaint
     */
    protected Paint getBorderPaint() {
        if (_borderPaint == null) {
            int borderColor = getRenderAttribute(AttributeForeColorBorder);
            _borderPaint = new Paint();
            _borderPaint.setStyle(Paint.Style.STROKE);
            _borderPaint.setColor(borderColor);
            _borderPaint.setAlpha(_colorAlpha);
        }
        return _borderPaint;
    }

    /**
     * @return the _textPaint
     */
    protected Paint getTextPaint() {
        if (_textPaint == null) {
            _textPaint = new Paint();
            int textColor = getRenderAttribute(AttributeForeColorText);
            _textPaint.setColor(textColor);
            _textPaint.setAlpha(_colorAlpha);
            _textPaint.setStyle(Paint.Style.STROKE);
            _textPaint.setAntiAlias(true);
        }
        return _textPaint;
    }

    protected Paint getBackgroundPaint() {
        if (_backgroundPaint == null) {
            _backgroundPaint = new Paint();
            int color = getRenderAttribute(AttributeBackgroundColor);
            _backgroundPaint.setColor(color);
            _backgroundPaint.setAlpha(_colorAlpha);
            _backgroundPaint.setStyle(Paint.Style.FILL);
        }
        return _backgroundPaint;
    }

    protected Paint getBackgroundHighlightedPaint() {
        if (_backgroundHighlightedPaint == null) {
            _backgroundHighlightedPaint = new Paint();
            int color = getRenderAttribute(AttributeBackgroundColorHighlighted);
            _backgroundHighlightedPaint.setColor(color);
            _backgroundHighlightedPaint.setAlpha(_colorAlpha);
            
            _backgroundHighlightedPaint.setStyle(Paint.Style.FILL);
        }
        return _backgroundHighlightedPaint;
    }

    protected Paint getPathPaint() {
        if (_pathPaint == null) {
            _pathPaint = new Paint();
            int color = getRenderAttribute(AttributeForeColorBorder);
            _pathPaint.setColor(color);
            _pathPaint.setAlpha(_colorAlpha);
            _pathPaint.setStyle(Paint.Style.STROKE);
            _pathPaint.setAntiAlias(true);
        }
        return _pathPaint;
    }

    public <T> T getRenderAttribute(String key) {
        while (key != null && key.length() > 0) {
            if (_displayAttributes.containsKey(key)) {
                return (T) _displayAttributes.get(key);
            }
            key = key.lastIndexOf(".") > 0
                    ? key.substring(0, key.lastIndexOf("."))
                    : null;
        }
        return null;
    }

    public void setRenderAttribute(String key, Object value) {
        _displayAttributes.put(key, value);
    }

    public int getColorAlpha() {
        return _colorAlpha;
    }

    /**
     * Sets the alpha values of all colors in the style scheme.
     * @param colorAlpha The alpha value, must be in the bounds [0, 255].
     */
    public void setColorAlpha(int colorAlpha) {
        this._colorAlpha = colorAlpha;
        clearCachedPaints();
    }

    private void clearCachedPaints() {
        _backgroundHighlightedPaint = null;
        _backgroundPaint = null;
        _borderPaint = null;
        _pathPaint = null;
        _textPaint = null;
    }
}
