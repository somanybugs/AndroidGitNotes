package lhg.gitnotes.note.md.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lhg.canvasscrollview.CanvasScrollView;
import lhg.canvasscrollview.SelectableAdapter;
import lhg.common.utils.DimenUtils;
import lhg.common.utils.ImageUtils;
import lhg.common.utils.Utils;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.FileViewer;
import lhg.markdown.ImagePlugin;
import lhg.markdown.MarkDownParser;
import lhg.markdown.MarkDownTheme;
import lhg.markdown.TablePlugin;

public class MDViewer extends FileViewer<List<CanvasScrollView.CanvasBlock>> {

    private static final String TAG = "MDViewer";
    CanvasScrollView canvasScrollView;
    MyAdapter adapter;

    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, MDViewer.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }

        setContentView(R.layout.activity_md_viewer);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();
        canvasScrollView = findViewById(R.id.canvasScrollView);
        canvasScrollView.setAdapter(adapter = new MyAdapter(getActivity()));
        setTitleAndSubtitle();
        return true;
    }

    class MyImageSpan extends ImagePlugin.ImageSpan {

        @Override
        protected void loadBitmap(String path) {
            Single.fromCallable(() -> loadImage(path)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> updateBitmap(bitmap)) ;
        }
    }

    private Bitmap loadImage(String path) {
        try {
            File file = null;
            if (path.startsWith("./") || path.startsWith(".\\")) {
                file = new File(parent, path.substring(2));
            } else {
                file = Glide.with(this).asFile().load(path).submit().get();
            }
            Point size = Utils.screenSizeInPixel(this);
            Log.i(TAG, path);
            Bitmap bitmap = ImageUtils.loadScaleBitmap(file.getAbsolutePath(), size.x, size.x * size.y);

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
            }
            Log.i(TAG, " w=" + bitmap.getWidth() + ", " + bitmap.getHeight());
            return bitmap;
        } catch (Exception e) {
            return Bitmap.createBitmap(1,1, Bitmap.Config.RGB_565);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mdviewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            startActivityForResult(MDEditor.makeIntent(this, file.getAbsolutePath(), gitConfig, password), RequestCode_Edit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected ReadCallback<List<CanvasScrollView.CanvasBlock>> onCreateCallback() {
        return new ReadCallback<List<CanvasScrollView.CanvasBlock>>() {
            @Override
            public List<CanvasScrollView.CanvasBlock> onRead() throws Exception {
                String text = readFileText();
                TextPaint textpaint = new TextPaint();
                textpaint.setTextSize(DimenUtils.sp2px(getActivity(), 16));
                MarkDownTheme theme = MarkDownTheme.builderWithDefaults(getApplicationContext())
                        .textPaint(textpaint)
                        .build();
                return new MarkDownParser(theme)
                        .usePlugin(new ImagePlugin() {

                            @Override
                            public ImageSpan createImageSpan() {
                                return new MyImageSpan();
                            }
                        })
                        .usePlugin(new TablePlugin())
                        .render(text);
            }

            @Override
            public void onReadSuccess(List<CanvasScrollView.CanvasBlock> content) {
                adapter.setDatas(content);
            }
        };
    }

    static class MyAdapter extends SelectableAdapter {

        List<CanvasScrollView.CanvasBlock> datas;

        public MyAdapter(Context context) {
            super(context);
        }

        public void setDatas(List<CanvasScrollView.CanvasBlock> datas) {
            this.datas = datas;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return datas != null ? datas.size() : 0;
        }

        @Override
        public CanvasScrollView.CanvasBlock getItem(@NonNull @NotNull CanvasScrollView parent, int position) {
            return datas.get(position);
        }
    }
}
