package lhg.gitnotes.note.ui;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.common.OnBackPressedCallback;

import lhg.gitnotes.note.NoteFolderHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class SelectFolderDialogFragment extends AppCompatDialogFragment implements OnBackPressedCallback {

    private OnSelectCallback callback;
    private RecyclerView recyclerView;
    private NoteListAdapter adapter;
    private NoteFolderHelper folderHelper;
    private TextView tvPath;
    private GitConfig gitConfig;
    private HorizontalScrollView pathScrollView;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private final NoteFolderHelper.OpenFolderCallback openFolderCallback = folder -> doOpenDir(folder);

    public SelectFolderDialogFragment(GitConfig gitConfig, OnSelectCallback callback) {
        this.callback = callback;
        this.gitConfig = gitConfig;
        setStyle(STYLE_NO_FRAME, R.style.AppDialogTheme);
    }

    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_folder, null);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.select_folder);
        toolbar.getMenu().add(getString(android.R.string.cancel)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        toolbar.getMenu().add(getString(android.R.string.ok)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        toolbar.setOnMenuItemClickListener(item -> {
            dismiss();
            if (item.getTitle().equals(getString(android.R.string.ok))) {
                if (callback != null) {
                    String folder = folderHelper.getCurrentDir();
                    callback.onSelectFolder(folder, folderHelper.getFilePassword(folder));
                }
            }
            return true;
        });
        recyclerView = findViewById(R.id.recyclerView);
        folderHelper = new NoteFolderHelper(gitConfig);
        tvPath = findViewById(R.id.tvPath);
        pathScrollView = findViewById(R.id.pathScrollView);
        initListView();
        openDir(null);
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_UP) {
                if (!onBackPressed()) {
                    dismiss();
                }
            }
            return true;
        });
        return dialog;
    }

        @Override
    public int show(@NonNull @NotNull FragmentTransaction transaction, @Nullable @org.jetbrains.annotations.Nullable String tag) {
        return super.show(transaction, tag);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public <T extends View> T findViewById(int id) {
        return getView().findViewById(id);
    }

    public boolean onBackPressed() {
        Stack<NoteFolderHelper.DatasHolder> stack = folderHelper.getFolderStack();
        if (stack.size() <= 1) {
            return false;
        }
        NoteFolderHelper.DatasHolder datasHolder = stack.get(stack.size() - 2);
        openDir(datasHolder.folder);
        return true;
    }

    public interface OnSelectCallback {
        void onSelectFolder(String folder, String base64Key);
    }

    private void initListView() {
        adapter = new NoteListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((adapter, data, holder) -> {
            openDir(data.file.getAbsolutePath());
        });
    }

    private void openDir(String path) {
        folderHelper.openFolder(getActivity(), path, openFolderCallback);
    }

    private synchronized void doOpenDir(String folder) {
        NoteFolderHelper.DatasHolder current = folderHelper.current();
        if (current != null) {
            View topView = recyclerView.getLayoutManager().getChildAt(0); //获取可视的第一个view
            if (topView != null) {
                current.y = topView.getTop(); //获取与该view的顶部的偏移量
                current.position = recyclerView.getLayoutManager().getPosition(topView);  //得到该View的数组位置
            } else {
                current.position = 0;
                current.y = 0;
            }
        }

        NoteFolderHelper.DatasHolder dest = folderHelper.createOrPopToHolder(folder);
        if (dest.datas == null) {
            dest.loadDatas(gitConfig, true);
        }

        if (recyclerView != null) {
            adapter.setDatas(dest.datas);
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(dest.position, dest.y);
        }

        tvPath.setText(folderHelper.createPathSpan(folder, path -> openDir(path)));
        uiHandler.postDelayed(() -> pathScrollView.fullScroll(View.FOCUS_RIGHT), 200);
    }

}
