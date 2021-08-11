package lhg.gitnotes.note.txt.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.note.md.ui.MDEditor;

public class TxtEditor extends MDEditor {

    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, TxtEditor.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }
        findViewById(R.id.llBottomBar).setVisibility(View.GONE);
        return true;
    }
}
