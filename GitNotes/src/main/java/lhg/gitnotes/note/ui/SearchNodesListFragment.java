package lhg.gitnotes.note.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Stack;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lhg.common.utils.ColorUtils;
import lhg.common.utils.ToastUtil;
import lhg.gitnotes.R;
import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.app.AppConstant;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.git.GitService;
import lhg.gitnotes.note.FolderEntity;
import lhg.gitnotes.note.NoteEntity;
import lhg.gitnotes.note.NoteFolderHelper;
import lhg.gitnotes.note.NoteFolderHelper.DatasHolder;
import lhg.gitnotes.note.bill.ui.BillEditor;
import lhg.gitnotes.note.md.ui.MDEditor;
import lhg.gitnotes.note.md.ui.MDViewer;
import lhg.gitnotes.note.pwd.ui.PasswordEditor;
import lhg.gitnotes.note.todo.ui.TodoEditor;
import lhg.gitnotes.note.txt.ui.TxtEditor;
import lhg.gitnotes.note.txt.ui.TxtViewer;


public class SearchNodesListFragment extends AppBaseFragment {
    GitConfig gitConfig;
    NoteFolderHelper folderHelper;
    SearchNoteListAdapter adapter;
    RecyclerView recyclerView;
    String repoDir;

    final NoteFolderHelper.OpenFolderCallback openFolderCallback = folder -> doOpenDir(folder, false);
    final NoteFolderHelper.OpenFolderCallback openFolderCallbackRefresh = folder -> doOpenDir(folder, true);
    int accentColor;
    String keyword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_notes_list, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gitConfig = (GitConfig) getArguments().getSerializable("gitConfig");
        keyword = getArguments().getString("keyword");

        repoDir = gitConfig.getRepoDir();
        folderHelper = new NoteFolderHelper(gitConfig);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new SearchNoteListAdapter(repoDir);
        initListView();

        accentColor = ColorUtils.getAccentColor(getContext());

        GitService.instance().getGitState(gitConfig.getUrl()).observe(getViewLifecycleOwner(), state -> updateState(state));
    }

    private void updateState(GitService.GitState state) {
        if (state.fileChanged) {
            folderHelper.deleteCache();
            refreshList();
        }
    }

    public void refreshList() {
        openDir(folderHelper.getCurrentDir(), true);
    }

    public synchronized void openDir(String path, boolean refresh) {
        folderHelper.openFolder(getActivity(), path, refresh ? openFolderCallbackRefresh : openFolderCallback);
    }

    private synchronized void doOpenDir(String folder, boolean refresh) {
        DatasHolder current = folderHelper.current();
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
        DatasHolder dest = folderHelper.createOrPopToHolder(folder);

        Single.fromCallable(() -> {
            if (dest.datas == null || refresh) {
                dest.loadDatas(gitConfig);
            }
            return dest;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(datasHolder -> {
                    if (recyclerView != null) {
                        adapter.setDatas(datasHolder.datas);
                        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(datasHolder.position, datasHolder.y);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    ToastUtil.show(getActivity(), throwable.getLocalizedMessage());
                });
    }

    public void openViewer(String path) {
        String password = folderHelper.getFilePassword(path);
        if (path.endsWith(AppConstant.FileSuffix.MD)) {
            startActivity(MDViewer.makeIntent(getContext(), path, gitConfig, password));
        } else if (path.endsWith(AppConstant.FileSuffix.PWD)) {
            startActivity(PasswordEditor.makeIntent(getContext(), path, gitConfig, password));
        } else if (path.endsWith(AppConstant.FileSuffix.TODO)) {
            startActivity(TodoEditor.makeIntent(getContext(), path, gitConfig, password));
        } else if (path.endsWith(AppConstant.FileSuffix.BILL)) {
            startActivity(BillEditor.makeIntent(getContext(), path, gitConfig, password));
        }  else {
            startActivity(TxtViewer.makeIntent(getContext(), path, gitConfig, password));
        }
    }

    public void openEditor(String path) {
        String password = folderHelper.getFilePassword(path);
        if (path.endsWith(AppConstant.FileSuffix.MD)) {
            startActivity(MDEditor.makeIntent(getContext(), path, gitConfig, password));
        } else if (path.endsWith(AppConstant.FileSuffix.PWD)) {
            startActivity(PasswordEditor.makeIntent(getContext(), path, gitConfig, password));
        } else if (path.endsWith(AppConstant.FileSuffix.TODO)) {
            startActivity(TodoEditor.makeIntent(getContext(), path, gitConfig, password));
        } else if (path.endsWith(AppConstant.FileSuffix.BILL)) {
            startActivity(BillEditor.makeIntent(getContext(), path, gitConfig, password));
        }  else {
            startActivity(TxtEditor.makeIntent(getContext(), path, gitConfig, password));
        }
    }

    @Override
    public synchronized boolean onBackPressed() {
        Stack<DatasHolder> stack = folderHelper.getFolderStack();
        if (stack.size() <= 1) {
            return super.onBackPressed();
        }
        DatasHolder datasHolder = stack.get(stack.size() - 2);
        openDir(datasHolder.folder, false);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void initListView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((adapter, data, holder) -> {
            if (data instanceof NoteEntity) {
                openViewer(data.file.getAbsolutePath());
            } else if (data instanceof FolderEntity) {
                openDir(data.file.getAbsolutePath(), false);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



}
