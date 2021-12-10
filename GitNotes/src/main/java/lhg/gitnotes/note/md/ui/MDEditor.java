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

import lhg.common.utils.ColorUtils;
import lhg.common.utils.DimenUtils;
import lhg.common.utils.DrawableUtils;
import lhg.common.utils.UndoRedo;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.FileEditor;

public class MDEditor extends FileEditor<String> {
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

        initBottomBar();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (editText.getSelectionStart() == -1) {
            editText.setSelection(0);
        }
        editText.requestFocus();
    }

    private void initRedoUndo() {
        undoRedo.init(editText);
        undoRedo.setCallback((i, j) -> {
            invalidateOptionsMenu();
            setContentChanged(hasChanged());
        });
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
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(DrawableUtils.colorStateList(Color.BLACK, ColorUtils.getAccentColor(this)));
        int p = DimenUtils.dip2px(parent.getContext(), 8);
        view.setPadding(p,p,p,p);
        view.setText(text);
        parent.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        view.setOnClickListener(v -> runnable.run());
    }

    @Override
    protected WriteCallback onCreateCallback() {
        return new WriteCallback<String>() {
            @Override
            public String onRead() throws Exception {
                return readFileText();
            }

            @Override
            public void onReadSuccess(String it) {
                editText.setText(it);
                initRedoUndo();
            }

            @Override
            public void onWrite() throws Exception {
                String text = editText.getText().toString();
                writeFileText(text);
            }
        };
    }
}
