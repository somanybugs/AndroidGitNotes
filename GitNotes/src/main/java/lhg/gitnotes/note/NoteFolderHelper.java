package lhg.gitnotes.note;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;

import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.utils.EncryptFolderUtils;
import lhg.gitnotes.ui.view.NumberPasswordDialog;
import lhg.common.utils.FileUtils;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;
import lhg.common.view.InputDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NoteFolderHelper {

    private final GitConfig gitConfig;
    private final Stack<DatasHolder> folderStack = new Stack<>();
    Pair<String, String> folderKey;

    public NoteFolderHelper(GitConfig gitConfig) {
        this.gitConfig = gitConfig;
    }

    public String ensureFolder(String path) {
        path = TextUtils.isEmpty(path) ? gitConfig.getRepoDir() : FileUtils.removeLastSeparator(path);
        if (!path.startsWith(gitConfig.getRepoDir() + File.separator)) {
            path = gitConfig.getRepoDir();
        }
        return path;
    }

    public Stack<DatasHolder> getFolderStack() {
        return folderStack;
    }

    public DatasHolder current() {
        if (folderStack.isEmpty()) {
            return null;
        }
        return folderStack.peek();
    }

    public String getCurrentDir() {
        NoteFolderHelper.DatasHolder last = current();
        return last == null ? gitConfig.getRepoDir() : last.folder;
    }

    public String getFilePassword(String path) {
        String password = null;
        if (folderKey != null && (path.equals(folderKey.first) || path.startsWith(folderKey.first + File.separator))) {
            password = folderKey.second;
        }
        return password;
    }

    public void openFolder(Context context, String path, OpenFolderCallback callback) {
        path = ensureFolder(path);
        if (path.equals(gitConfig.getRepoDir())) {
            folderKey = null;
        }

        if (folderKey != null && folderKey.first.equals(path)) {
            callback.openFolder(path);
            return;
        }

        File lockFile = gitConfig.isRootEncryptFolder(path);
        if (lockFile == null) {
            callback.openFolder(path);
            return;
        }

        String finalDirPath = path;
        NumberPasswordDialog dialog = new NumberPasswordDialog(context);
        dialog.setOnInputListener(new InputDialog.SimpleOnInputListener() {
            @Override
            public void onInput(InputDialog dialog, String text) {
                if (!TextUtils.isEmpty(text)) {
                    try {
                        byte[] key = EncryptFolderUtils.decryptKey(text, lockFile);
                        folderKey = new Pair<>(finalDirPath, Utils.bytesToHEX(key));
                        callback.openFolder(finalDirPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.show(context, context.getString(R.string.passowrd_error));
                    }
                }
            }
        });
        dialog.show();
    }

    public void deleteCache(String path) {
        path = ensureFolder(path);
        for (DatasHolder datasHolder : folderStack) {
            if (path.equals(datasHolder.folder)) {
                datasHolder.datas = null;
                break;
            }
        }
    }

    public void deleteCache() {
        for (DatasHolder datasHolder : folderStack) {
            datasHolder.datas = null;
        }
    }

    public interface OpenFolderCallback {
        void openFolder(String folder);
    }


    public DatasHolder createOrPopToHolder(String dir) {
        dir = ensureFolder(dir);
        while (!folderStack.isEmpty()) {
            DatasHolder holder = folderStack.peek();
            if (dir.length() < holder.folder.length()) {
                folderStack.pop();
                continue;
            }

            if (dir.length() == holder.folder.length() && !dir.equals(holder.folder)) {
                folderStack.pop();
                continue;
            }

            if (dir.length() > holder.folder.length() && !dir.startsWith(holder.folder + File.separator)) {
                folderStack.pop();
                continue;
            }
            break;
        }

        DatasHolder datasHolder = null;
        if (folderStack.size() > 0) {
            DatasHolder last = folderStack.peek();
            if (last.folder.equals(dir)) {
                datasHolder = last;
            }
        }

        if (datasHolder == null) {
            datasHolder = new DatasHolder();
            datasHolder.folder = dir;
            folderStack.add(datasHolder);
        }
        return datasHolder;
    }


    public CharSequence createPathSpan(String dir, OpenFolderCallback callback) {
        dir = dir.substring(gitConfig.getRepoDir().length());
        List<Pair<String, String>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("root", gitConfig.getRepoDir()));

        if (dir.endsWith(File.separator)) {
            dir = dir.substring(0, dir.length() - 1);
        }
        String[] tokens = dir.split(File.separator);
        for (int i = 0; i < tokens.length; i++) {
            String s = tokens[i];
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            pairs.add(new Pair<>(s, s));
        }

        SpannableStringBuilder sb = new SpannableStringBuilder();
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < pairs.size(); i++) {
            String name = pairs.get(i).first;
            String value = pairs.get(i).second;
            path.append(value);
            final boolean isLastPath = i == tokens.length - 1;
            if (!isLastPath) {
                name += " > ";
            }
            String finalPath = path.toString();
            Utils.append(sb, name, new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    if (!isLastPath) {
                        if (callback != null) {
                            openFolder(view.getContext(), finalPath, callback);
                        }
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    if (!isLastPath) {
                        ds.setColor(ds.getColor());
                    } else {
                        ds.setColor(ds.linkColor);
                    }
                }
            });

            path.append(File.separator);
        }
        return sb;
    }

    public static class DatasHolder {
        public String folder;
        public List datas;
        public int position;
        public int y;

        public void loadDatas(GitConfig gitConfig) {
            loadDatas(gitConfig, false);
        }

        public void loadDatas(GitConfig gitConfig, boolean onlyFolder) {
            List items = new ArrayList<>();
            List notes = new ArrayList();
            File[] files = new File(folder).listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(".") || f.getName().startsWith("..")) {
                        continue;
                    }
                    if (f.isDirectory()) {
                        FolderEntity fe = new FolderEntity();
                        fe.isEncrypted = gitConfig.isRootEncryptFolder(f) != null;
                        fe.file = f;
                        items.add(fe);
                    } else if (!onlyFolder) {
                        NoteEntity ne = new NoteEntity();
                        ne.file = f;
                        ne.suffix = "." + FileUtils.getSuffix(f.getName());
                        ne.dir = f.getParent().substring(gitConfig.getRepoDir().length());
                        notes.add(ne);
                    }
                }
            }
            items.addAll(notes);
            datas = items;
        }
    }
}
