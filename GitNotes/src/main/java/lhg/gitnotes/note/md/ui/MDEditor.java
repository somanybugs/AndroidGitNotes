package lhg.gitnotes.note.md.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lhg.common.utils.ColorUtils;
import lhg.common.utils.DimenUtils;
import lhg.common.utils.DrawableUtils;
import lhg.common.utils.UndoRedo;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.FileEditor;

public class MDEditor extends FileEditor {
    EditText editText;
    final UndoRedo undoRedo = new UndoRedo();
    TextView tvTitle;

    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, MDEditor.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }

        setContentView(R.layout.activity_md_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();
        setTitle(null);

        tvTitle = findViewById(R.id.tvTitle);
        editText = findViewById(R.id.editText);
        tvTitle.setText(file.getName());

        if (!isNewFile) {
            Single.fromCallable(() -> readFileText())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(text -> {
                        editText.setText(text);
                        initRedoUndo();
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        } else {
            initRedoUndo();
        }
        initBottomBar();
        return true;
    }

    private void initRedoUndo() {
        editText.requestFocus();
        editText.setSelection(0);
        undoRedo.init(editText);
        undoRedo.setCallback((i, j) -> invalidateOptionsMenu());
    }

    private void replaceText(String txt) {
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();
        editText.getText().replace(selectionStart, selectionEnd, txt);
        editText.setSelection(selectionStart + txt.length());
    }

    private void replaceText(String txt1, String txt2) {
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();
        editText.getText().replace(selectionStart, selectionEnd, txt1 + txt2);
        editText.setSelection(selectionStart + txt1.length());
    }

    private void initBottomBar() {
        LinearLayout ll = findViewById(R.id.llBottomBar);
        createEditIconView(ll, "#", () -> replaceText("#"));
        createEditIconView(ll, ">", () -> replaceText(">"));
        createEditIconView(ll, "*", () -> replaceText("*"));
        createEditIconView(ll, "-", () -> replaceText("-"));
        createEditIconView(ll, "~", () -> replaceText("~"));
    }

    protected boolean hasChanged() {
        return UndoRedo.EmptyCommandId != undoRedo.getLastCanUndoId();
    }

    protected void onSaveLocalFile() throws Throwable {
        String text = editText.getText().toString();
        writeFileText(text);
    }

    @Override
    public void onBackPressed() {
        if (hasChanged()) {
            gitSync(() -> super.onBackPressed());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mdediter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        enableMenuItem(menu.findItem(R.id.action_undo), undoRedo.canUndo());
        enableMenuItem(menu.findItem(R.id.action_redo), undoRedo.canRedo());
        enableMenuItem(menu.findItem(R.id.action_done), hasChanged());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_undo) {
            undoRedo.undo();
            return true;
        }
        if (item.getItemId() == R.id.action_redo) {
            undoRedo.redo();
            return true;
        }
        if (item.getItemId() == R.id.action_done) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableMenuItem(MenuItem item, boolean enable) {
        item.setEnabled(enable);
        item.getIcon().setAlpha(enable? 255 : 130);
    }

    private void createEditIconView(LinearLayout parent, String text, Runnable runnable) {
        TextView view = new TextView(parent.getContext());
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(DrawableUtils.colorStateList(Color.BLACK, ColorUtils.getAccentColor(this)));
        int p = DimenUtils.dip2px(parent.getContext(), 8);
        view.setPadding(p,p,p,p);
        view.setText(text);
        parent.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        view.setOnClickListener(v -> runnable.run());
    }
}