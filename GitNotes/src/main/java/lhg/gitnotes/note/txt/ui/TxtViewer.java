package lhg.gitnotes.note.txt.ui;

import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;

import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.FileViewer;
import lhg.common.utils.ToastUtil;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TxtViewer extends FileViewer {

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
        loadFile();
        return true;
    }


    private void loadFile() {
        Single.fromCallable(() -> readFileText())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(node -> textView.setText(node), throwable -> {
                    throwable.printStackTrace();
                    ToastUtil.show(getApplication(), "markdown parse error " + throwable.getMessage());
                });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode_Edit && resultCode == RESULT_OK) {
            loadFile();
        }
    }
}
