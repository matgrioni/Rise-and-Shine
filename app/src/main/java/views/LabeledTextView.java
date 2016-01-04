package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.grioni.app.screenwakecounter.R;

/**
 * @author Matias Grioni
 * @created 1/4/16
 */
public class LabeledTextView extends TextView {
    private String label;

    public LabeledTextView(Context context) {
        super(context);
    }

    public LabeledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupAttrs(attrs);
    }

    public LabeledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupAttrs(attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(label + text, type);
    }

    private void setupAttrs(AttributeSet attrs) {
        TypedArray arr = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.LabeledTextView, 0, 0);

        try {
            label = arr.getString(R.styleable.LabeledTextView_label);
        } finally {
            arr.recycle();
        }

        setText(label);
    }
}
