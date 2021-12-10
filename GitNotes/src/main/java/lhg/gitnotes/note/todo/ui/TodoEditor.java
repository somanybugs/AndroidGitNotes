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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lhg.common.utils.ColorUtils;
import lhg.common.view.InputDialog;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.note.todo.TodoEntity;
import lhg.gitnotes.ui.FileEditor;
import lhg.gitnotes.ui.view.BaseItemMoveCallback;

public class TodoEditor extends FileEditor<List<TodoEntity>> {

    RecyclerView recyclerView;
    TodoItemAdapter adapter;

    int accentColor;
    final ArrayList<TodoEntity> content = new ArrayList<>();


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

        setTitleAndSubtitle();
        recyclerView = findViewById(R.id.recyclerView);

        initListView();

        return true;
    }



    protected TodoItemAdapter createAdapter() {
        return new TodoItemAdapter(){
            @NonNull
            @NotNull
            @Override
            public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new VH(parent){
                    {
                        itemView.setOnClickListener(v -> gotoEditItem(item, false));
                    }
                    @Override
                    protected void onChecked(TodoEntity item) {
                        super.onChecked(item);
                        saveLocalFile();
                    }

                    @Override
                    protected void onDelete(TodoEntity item) {
                        super.onDelete(item);
                        adapter.notifyItemRemoved(getBindingAdapterPosition());
                        content.remove(item);
                        saveLocalFile();
                    }
                };
            }
        };
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
                int index = content.indexOf(finalP);
                finalP.content = text;
                finalP.time = System.currentTimeMillis();
                if (index >= 0) {
                    content.set(index, finalP);
                    adapter.notifyItemChanged(index);
                } else {
                    content.add(top ? 0 : content.size(), finalP);
                    adapter.notifyItemInserted(top ? 0 : content.size());
                }
                saveLocalFile();
            }
        });
        dialog.show();
    }

    private void initListView() {
        adapter = createAdapter();
        ConcatAdapter concatAdapter = new ConcatAdapter(new HeaderAdapter(true), adapter, new HeaderAdapter(false));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(concatAdapter);

        new ItemTouchHelper(new BaseItemMoveCallback(getActivity()) {
            @Override
            protected void onItemMoved(int from, int to) {
                Collections.swap(content, from, to);//更换我们数据List的位置
                adapter.notifyItemMoved(from, to);
                saveLocalFile();
            }
        }).attachToRecyclerView(recyclerView);
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

    @Override
    protected WriteCallback<List<TodoEntity>> onCreateCallback() {
        return new WriteCallback<List<TodoEntity>>() {
            final Gson gson = new Gson();
            @Override
            public List<TodoEntity> onRead() throws Exception {
                String text = readFileText();
                return gson.fromJson(text, new TypeToken<ArrayList<TodoEntity>>(){}.getType());
            }

            @Override
            public void onReadSuccess(List<TodoEntity> it) {
                if (it != null) {
                    content.addAll(it);
                    adapter.setItems(content);
                }
            }

            @Override
            public void onWrite() throws Exception {
                String text = gson.toJson(content);
                writeFileText(text);
            }
        };
    }
}
