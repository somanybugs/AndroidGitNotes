package lhg.gitnotes.note.txt.ui;

import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.FileViewer;

public class TxtViewer extends FileViewer<String> {

    private static final String TAG = "MDViewer";
    TextView textView;

    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, TxtViewer.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }

        setContentView(R.layout.activity_txt_viewer);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        setTitleAndSubtitle();
        return true;
    }

    @Override
    protected ReadCallback<String> onCreateCallback() {
        return new ReadCallback<String>() {
            @Override
            public String onRead() throws Exception {
                return readFileText();
            }

            @Override
            public void onReadSuccess(String content) {
                textView.setText(content);
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mdviewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            startActivityForResult(TxtEditor.makeIntent(this, file.getAbsolutePath(), gitConfig, password), RequestCode_Edit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
