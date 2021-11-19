package lhg.gitnotes.note.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.git.GitService;
import lhg.gitnotes.git.action.GitDecryptFolder;
import lhg.gitnotes.git.action.GitDelete;
import lhg.gitnotes.git.action.GitEncryptFolder;
import lhg.gitnotes.git.action.GitMove;
import lhg.gitnotes.git.action.GitRename;
import lhg.gitnotes.git.action.GitSync;
import lhg.gitnotes.note.bill.ui.BillEditor;
import lhg.gitnotes.utils.AppUtils;
import lhg.gitnotes.note.NoteFolderHelper;
import lhg.gitnotes.note.NoteFolderHelper.DatasHolder;
import lhg.gitnotes.note.md.ui.MDEditor;
import lhg.gitnotes.note.md.ui.MDViewer;
import lhg.gitnotes.app.AppConstant;
import lhg.gitnotes.note.FolderEntity;
import lhg.gitnotes.note.NoteEntity;
import lhg.gitnotes.note.pwd.ui.PasswordEditor;
import lhg.gitnotes.note.todo.ui.TodoEditor;
import lhg.gitnotes.note.txt.ui.TxtEditor;
import lhg.gitnotes.note.txt.ui.TxtViewer;
import lhg.gitnotes.ui.view.NumberPasswordDialog;
import lhg.common.utils.ColorUtils;
import lhg.common.utils.FileUtils;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;
import lhg.common.view.InputDialog;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class NodesListFragment extends AppBaseFragment {
    GitConfig gitConfig;
    NoteFolderHelper folderHelper;
    NoteListAdapter adapter;
    RecyclerView recyclerView;
    TextView tvPath;
    String repoDir;
    PopupMenu createNoteMenu;

    HorizontalScrollView pathScrollView;
    ActionMode selectActionMode;
    final NoteFolderHelper.OpenFolderCallback openFolderCallback = folder -> doOpenDir(folder, false);
    final NoteFolderHelper.OpenFolderCallback openFolderCallbackRefresh = folder -> doOpenDir(folder, true);
    int accentColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes_list, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gitConfig = (GitConfig) getArguments().getSerializable("gitConfig");
        repoDir = gitConfig.getRepoDir();
        folderHelper = new NoteFolderHelper(gitConfig);

        pathScrollView = findViewById(R.id.pathScrollView);
        findViewById(R.id.btnAddNote).setOnClickListener(v -> clickOnAddNote(v));
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new NoteListAdapter();
        initListView();

        tvPath = findViewById(R.id.tvPath);
        tvPath.setMovementMethod(LinkMovementMethod.getInstance());
        accentColor = ColorUtils.getAccentColor(getContext());

        GitService.instance().getGitState(gitConfig.getUrl()).observe(getViewLifecycleOwner(), state -> updateState(state));
    }

    private void updateState(GitService.GitState state) {
        if (state.fileChanged) {
            folderHelper.deleteCache();
            refreshList();
        }
    }

    private void clickOnAddNote(View view) {
        if (createNoteMenu == null) {
            createNoteMenu = new CreateNotePopMenu(getContext(), view) {
                @Override
                protected void onCreateNote(int itemId, String name) {
                    File file = new File(folderHelper.getCurrentDir(), name);
                    if (file.exists()) {
                        showAlert(getString(R.string.error), name + " already exists");
                        return;
                    }
                    if (itemId == R.id.action_folder) {
                        file.mkdir();
                        refreshList();
                    } else {
                        openEditor(file.getAbsolutePath());
                    }
                }
            };
        }
        createNoteMenu.show();
    }

    public void refreshList() {
        finishSelectMode();
        openDir(folderHelper.getCurrentDir(), true);
    }

    public synchronized void openDir(String path, boolean refresh) {
        folderHelper.openFolder(getContext(), path, refresh ? openFolderCallbackRefresh : openFolderCallback);
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
                        finishSelectMode();
                        adapter.setDatas(datasHolder.datas);
                        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(datasHolder.position, datasHolder.y);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    ToastUtil.show(getActivity(), throwable.getLocalizedMessage());
                });

        tvPath.setText(folderHelper.createPathSpan(folder, openFolderCallback));
        uiHandler.postDelayed(() -> pathScrollView.fullScroll(View.FOCUS_RIGHT), 200);
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

        adapter.setOnItemLongClickListener((adapter, data, holder) -> {
            startSelectMode(data.file.getAbsolutePath());
            return true;
        });
    }

    private void showDecryptFolderDialog(File file) {
        NumberPasswordDialog dialog = new NumberPasswordDialog(getContext());
        dialog.setTitle(R.string.decrypt_folder);
        dialog.setMessage(getString(R.string.decrypt_folder_tips));
        dialog.getEditText().setHint(R.string.input_password);
        dialog.setOnInputListener(new InputDialog.SimpleOnInputListener() {
            @Override
            public void onInput(InputDialog dialog, String text) {
                if (!TextUtils.isEmpty(text)) {
                    decryptFolder(file, text);
                }
            }
        });
        dialog.show();
    }

    private void decryptFolder(File file, String password) {
        doGitSync(new GitDecryptFolder(gitConfig, file.getAbsolutePath(), password));
    }

    private void showEncryptFolderDialog(File file) {
        NumberPasswordDialog dialog = new NumberPasswordDialog(getContext());
        dialog.setTitle(R.string.encrypt_folder);
        dialog.setMessage(getString(R.string.encrypt_folder_tips));
        dialog.getEditText().setHint(R.string.input_password);
        dialog.setOnInputListener(new InputDialog.SimpleOnInputListener() {
            @Override
            public void onInput(InputDialog dialog, String text) {
                if (!TextUtils.isEmpty(text)) {
                    encryptFolder(file, text);
                }
            }
        });
        dialog.show();
    }

    private void encryptFolder(File file, String password) {
        doGitSync(new GitEncryptFolder(gitConfig, file.getAbsolutePath(), password));
    }

    private void showDeleteDialog(File... files) {
        if (files == null || files.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(f.getName());
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm_delete_files)
                .setMessage(sb)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteFiles(files))
                .show();
    }

    private void deleteFiles(File[] files) {
        doGitSync(new GitDelete(gitConfig, files));
    }

    private void showMoveToDialog(File[] files) {
        String srcPassword = folderHelper.getFilePassword(files[0].getAbsolutePath());
        new SelectFolderDialogFragment(gitConfig, (folder, destPassword) -> moveFiles(files, srcPassword, new File(folder), destPassword))
                .show(getChildFragmentManager(), SelectFolderDialogFragment.class.getName());
    }

    private void moveFiles(File[] files, String srcPassword, File destFolder, String destPassword) {
        byte[] srcKey = null;
        if (!TextUtils.isEmpty(srcPassword)) {
            srcKey = Utils.hexToBytes(srcPassword);
        }
        byte[] desKey = null;
        if (!TextUtils.isEmpty(destPassword)) {
            desKey = Utils.hexToBytes(destPassword);
        }
        doGitSync(new GitMove(gitConfig, files, srcKey, destFolder, desKey));
    }

    private void showRenameDialog(File file) {
        InputDialog dialog = new InputDialog(getContext());
        dialog.setTitle(R.string.rename);
        dialog.getEditText().setText(file.getName());
        dialog.setOnInputListener(new InputDialog.SimpleOnInputListener() {
            @Override
            public void onInput(InputDialog dialog, String text) {
                doGitSync(new GitRename(gitConfig, file, text));
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void doGitSync(GitSync gitSync) {
        GitService.instance().submit(gitSync);
    }

    /////////////////////////////////select mode//////////////////////////

    private NoteListAdapter.OnSelectListsner onSelectListsner = new NoteListAdapter.OnSelectListsner() {
        @Override
        public void onChanged(NoteListAdapter adapter) {
            if (selectActionMode != null) {
                selectActionMode.invalidate();
            }
        }
    };

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.main_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (adapter.selectPaths.size() > 0 && adapter.isSelectAll()) {
                menu.findItem(R.id.action_select_all).getIcon().setTint(accentColor);
            } else {
                menu.findItem(R.id.action_select_all).getIcon().setTintList(null);
            }
            boolean showLockItem = false;
            boolean showUnLockItem = false;
            boolean showRename = false;
            if (adapter.selectPaths.size() == 1) {
                showRename = true;
                File file = new File(adapter.selectPaths.get(0));
                if (file.isDirectory() && FileUtils.removeLastSeparator(file.getParent()).equals(gitConfig.getRepoDir())) {
                    File lockFile = new File(file, GitConfig.FolderLockFile);
                    if (lockFile.exists()) {
                        showUnLockItem = true;
                    } else {
                        showLockItem = true;
                    }
                }
            }
            menu.findItem(R.id.action_rename).setVisible(showRename);
            menu.findItem(R.id.action_lock).setVisible(showLockItem);
            menu.findItem(R.id.action_unlock).setVisible(showUnLockItem);
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
                if (adapter.selectPaths.isEmpty()) {
                    ToastUtil.show(getContext(), R.string.none_selected);
                } else {
                    showDeleteDialog(AppUtils.pathToFiles(adapter.selectPaths));
                }
                return true;
            } else if (item.getItemId() == R.id.action_move) {
                if (adapter.selectPaths.isEmpty()) {
                    ToastUtil.show(getContext(), R.string.none_selected);
                } else {
                    showMoveToDialog(AppUtils.pathToFiles(adapter.selectPaths));
                }
                return true;
            } else if (item.getItemId() == R.id.action_lock) {
                showEncryptFolderDialog(new File(adapter.selectPaths.get(0)));
                return true;
            } else if (item.getItemId() == R.id.action_unlock) {
                showDecryptFolderDialog(new File(adapter.selectPaths.get(0)));
                return true;
            } else if (item.getItemId() == R.id.action_rename) {
                showRenameDialog(new File(adapter.selectPaths.get(0)));
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


    private void finishSelectMode() {
        if (selectActionMode != null) {
            selectActionMode.finish();
            selectActionMode = null;
        }
        adapter.setOnSelectListsner(null);
        adapter.setSelecting(false, null);
    }

    private void startSelectMode(String selectFile) {
        adapter.setSelecting(true, Arrays.asList(selectFile));
        adapter.setOnSelectListsner(onSelectListsner);
        selectActionMode = getActivity().startActionMode(actionModeCallback);
    }

    /////////////////////////////////select mode//////////////////////////
}
