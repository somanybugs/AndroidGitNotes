package lhg.gitnotes.note.todo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.note.todo.TodoEntity;
import lhg.gitnotes.ui.FileEditor;
import lhg.common.utils.ColorUtils;
import lhg.common.view.InputDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TodoEditor extends FileEditor {

    RecyclerView recyclerView;
    TodoItemAdapter adapter;
    Gson gson;
    ArrayList<TodoEntity> datas;
    boolean hasChanged = false;
    int accentColor;


    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, TodoEditor.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }
        accentColor = ColorUtils.getAccentColor(this);

        setContentView(R.layout.activity_todo_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();

        gson = new Gson();

        setTitleAndSubtitle();
        recyclerView = findViewById(R.id.recyclerView);

        initListView();

        if (!isNewFile) {
            Single.fromCallable(() -> {
                String text = readFileText();
                ArrayList<TodoEntity> datas =  gson.fromJson(text, new TypeToken<ArrayList<TodoEntity>>(){}.getType());
                return datas;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(datas -> {
                        this.datas = datas;
                        adapter.setItems(datas);
                    }, throwable -> {
                        this.datas = new ArrayList<>();
                        throwable.printStackTrace();
                    });
        } else {
            this.datas = new ArrayList<>();
            adapter.setItems(datas);
        }
        return true;
    }

    protected TodoItemAdapter createAdapter() {
        return new TodoItemAdapter(){
            @NonNull
            @NotNull
            @Override
            public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new VH(parent){
                    @Override
                    protected void onChecked(TodoEntity item) {
                        super.onChecked(item);
                        saveLocalFile();
                        hasChanged = true;
                    }

                    @Override
                    protected void onDelete(TodoEntity item) {
                        super.onDelete(item);
                        hasChanged = true;
                        datas.remove(item);
                        adapter.notifyDataSetChanged();
                    }
                };
            }
        };
    }


    @Override
    public void onBackPressed() {
        if (hasChanged) {
            gitSync(() -> super.onBackPressed());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveLocalFile() throws Throwable {
        String text = gson.toJson(datas);
        writeFileText(text);
    }

    private void gotoEditItem(TodoEntity p, boolean top) {
        if (p == null) {
            p = new TodoEntity();
        }
        if (TextUtils.isEmpty(p.uuid)) {
            p.uuid = UUID.randomUUID().toString();
        }
        InputDialog dialog = new InputDialog(this);
        dialog.setTitle("Edit Item Content");
        dialog.getEditText().setText(p.content);
        TodoEntity finalP = p;
        dialog.setOnInputListener(new InputDialog.SimpleOnInputListener(){
            @Override
            public void onInput(InputDialog dialog, String text) {
                int index = datas.indexOf(finalP);
                finalP.content = text;
                finalP.time = System.currentTimeMillis();
                if (index >= 0) {
                    datas.set(index, finalP);
                    adapter.notifyItemChanged(index);
                } else {
                    datas.add(top ? 0 : datas.size(), finalP);
                    adapter.notifyItemInserted(top ? 0 : datas.size());
                }
                hasChanged = true;
                saveLocalFile();
            }
        });
        dialog.show();
    }

    private void initListView() {
        adapter = createAdapter();
        adapter.setOnItemClickListener((adapter, data, holder) -> gotoEditItem(data, false));
        ConcatAdapter concatAdapter = new ConcatAdapter(new HeaderAdapter(true), adapter, new HeaderAdapter(false));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(concatAdapter);
    }

    private class HeaderAdapter extends RecyclerView.Adapter {

        boolean top;

        public HeaderAdapter(boolean top) {
            this.top = top;
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_add, parent, false)) {
            };
            vh.itemView.setOnClickListener(v -> gotoEditItem(null, top));
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }


}
