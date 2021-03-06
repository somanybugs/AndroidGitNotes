package lhg.gitnotes.note.pwd.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lhg.common.utils.ColorUtils;
import lhg.common.utils.ToastUtil;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.note.pwd.PasswordEntity;
import lhg.gitnotes.ui.FileEditor;

public class PasswordEditor extends FileEditor<List<PasswordEntity>> {

    private static final int RequestCode_editItem = 111;
    RecyclerView recyclerView;
    PasswordItemAdapter adapter;
    final ArrayList<PasswordEntity> datas = new ArrayList<>();
    boolean hasChanged = false;
    private ActionMode selectActionMode;
    int accentColor;


    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, PasswordEditor.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }
        accentColor = ColorUtils.getAccentColor(this);

        setContentView(R.layout.activity_password_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();

        findViewById(R.id.btnAdd).setOnClickListener(v -> startActivityForResult(PasswordItemEditor.makeIntent(this, null), RequestCode_editItem));

        setTitleAndSubtitle();
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new PasswordItemAdapter();
        initListView();

        return true;
    }

    private void gotoView(PasswordEntity p) {
        ViewPasswordDialog dialog = new ViewPasswordDialog(getActivity(),p){
            @Override
            protected void clickOnEdit() {
                startActivityForResult(PasswordItemEditor.makeIntent(getActivity(), p), RequestCode_editItem);
            }
        };
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RequestCode_editItem == requestCode && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(PasswordItemEditor.IntentKey_Item)) {
                PasswordEntity p = (PasswordEntity) data.getSerializableExtra(PasswordItemEditor.IntentKey_Item);
                p.time = System.currentTimeMillis();
                int index = datas.indexOf(p);
                if (index >= 0) {
                    datas.set(index, p);
                    adapter.notifyItemChanged(index);
                } else {
                    datas.add(0, p);
                    adapter.notifyItemInserted(0);
                }
                saveLocalFile();
            }
        }
    }

    private void initListView() {
        adapter.setOnItemClickListener((adapter, data, holder) -> gotoView(data));
        adapter.setOnItemLongClickListener((adapter, data, holder) -> {
            startSelectMode(data);
            return true;
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(adapter);
    }



    /////////////////////////////////select mode//////////////////////////

    private PasswordItemAdapter.OnSelectListsner onSelectListsner = new PasswordItemAdapter.OnSelectListsner() {
        @Override
        public void onChanged(PasswordItemAdapter adapter) {
            if (selectActionMode != null) {
                selectActionMode.invalidate();
            }
        }
    };

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.pwd_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (adapter.selectedItems.size() > 0 && adapter.isSelectAll()) {
                menu.findItem(R.id.action_select_all).getIcon().setTint(accentColor);
            } else {
                menu.findItem(R.id.action_select_all).getIcon().setTintList(null);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_select_all) {
                if (adapter.isSelectAll()) {
                    adapter.selectNone();
                } else {
                    adapter.selectAll();
                }
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                if (adapter.selectedItems.isEmpty()) {
                    ToastUtil.show(getActivity(), R.string.none_selected);
                } else {
                    showDeleteDialog(adapter.selectedItems);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectActionMode = null;
            finishSelectMode();
        }
    };

    private void showDeleteDialog(List<PasswordEntity> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (PasswordEntity f : items) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(f.name);
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_delete_files)
                .setMessage(sb)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteItems(items))
                .show();
    }

    private void deleteItems(List<PasswordEntity> items) {
        datas.removeAll(items);
        saveLocalFile();
        adapter.notifyDataSetChanged();
        finishSelectMode();
    }


    private void finishSelectMode() {
        if (selectActionMode != null) {
            selectActionMode.finish();
            selectActionMode = null;
        }
        adapter.setOnSelectListsner(null);
        adapter.setSelecting(false, null);
    }

    private void startSelectMode(PasswordEntity item) {
        adapter.setSelecting(true, Arrays.asList(item));
        adapter.setOnSelectListsner(onSelectListsner);
        selectActionMode = getActivity().startActionMode(actionModeCallback);
    }

    /////////////////////////////////select mode//////////////////////////


    @Override
    protected void saveLocalFile() {
        hasChanged = true;
        super.saveLocalFile();
    }

    @Override
    protected WriteCallback<List<PasswordEntity>> onCreateCallback() {
        Gson gson = new Gson();
        return new WriteCallback<List<PasswordEntity>>() {
            @Override
            public void onWrite() throws Exception {
                String text = gson.toJson(datas);
                writeFileText(text);
            }

            @Override
            public List<PasswordEntity> onRead() throws Exception {
                String text = readFileText();
                ArrayList<PasswordEntity> datas =  gson.fromJson(text, new TypeToken<ArrayList<PasswordEntity>>(){}.getType());
                return datas;
            }

            @Override
            public void onReadSuccess(List<PasswordEntity> content) {
                datas.addAll(content);
                adapter.setItems(datas);
            }
        };
    }
}
