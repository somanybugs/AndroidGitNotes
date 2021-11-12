package lhg.gitnotes.ui.view;

import android.content.Context;
import android.text.InputType;

import lhg.common.view.InputDialog;
import lhg.gitnotes.R;

public class NumberPasswordDialog extends InputDialog {

    public NumberPasswordDialog(Context context) {
        super(context);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        setTitle(R.string.input_password);
    }
}
