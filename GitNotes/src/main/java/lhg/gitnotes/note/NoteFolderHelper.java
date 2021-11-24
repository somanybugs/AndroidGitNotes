package lhg.gitnotes.note;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import lhg.gitnotes.R;
import lhg.gitnotes.app.FingerprintHelper;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.utils.EncryptFolderUtils;
import lhg.gitnotes.ui.view.NumberPasswordDialog;
import lhg.common.utils.FileUtils;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;
import lhg.common.view.InputDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class NoteFolderHelper {

    private final GitConfig gitConfig;
    private final Stack<DatasHolder> folderStack = new Stack<>();
    Pair<String, String> folderKey;
    FingerprintHelper.Login fingerLoginHelper = new FingerprintHelper.Login();
    private final CachedFolderKeys cachedFolderKeys;

    public NoteFolderHelper(GitConfig gitConfig) {
        this.gitConfig = gitConfig;
        this.cachedFolderKeys = new CachedFolderKeys(new File(gitConfig.getRootDir(), "cached_folder_keys.json"));
        this.cachedFolderKeys.load();
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
        if (!fingerprintToOpenEncryptFolder(context, lockFile, path, callback)) {
            inputKeyToOpenEncryptFolder(context, null, lockFile, finalDirPath, callback);
        }
    }

    private boolean fingerprintToOpenEncryptFolder(Context context, File lockFile, String path, OpenFolderCallback callback) {
        Activity activity = Utils.getActivityFromContext(context);
        if (activity == null || !(activity instanceof FragmentActivity)) {
            return false;
        }
        if (!fingerLoginHelper.init((FragmentActivity) activity)) {
            return false;
        }

        fingerLoginHelper.show(new FingerprintHelper.Login.Callback() {
            @Override
            public void onSuccess(byte[] fingerprintKey) {
                boolean needInputKey = true;
                CachedFolderKey cachedFolderKey = cachedFolderKeys.find(path);
                if (fingerprintKey != null && cachedFolderKey != null) {
                    byte[] plainKey = FingerprintHelper.decryptBytes2bytes(fingerprintKey, cachedFolderKey.encryptedKey);
                    if (plainKey != null && plainKey.length > 0) {
                        String plainKeyStr = new String(plainKey);
                        if (!openEncryptFolder(context, lockFile, path, plainKeyStr, callback)) {
                            cachedFolderKeys.remove(path);
                        } else {
                            needInputKey = false;
                        }
                    }
                }
                if (needInputKey) {
                    inputKeyToOpenEncryptFolder(context, fingerprintKey, lockFile, path, callback);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                inputKeyToOpenEncryptFolder(context, null, lockFile, path, callback);
            }
        });
        return true;
    }

    private void inputKeyToOpenEncryptFolder(Context context, byte[] fingerprintKey, File lockFile, String path, OpenFolderCallback callback) {
        NumberPasswordDialog dialog = new NumberPasswordDialog(context);
        dialog.setOnInputListener(new InputDialog.SimpleOnInputListener() {
            @Override
            public void onInput(InputDialog dialog, String text) {
                if (!TextUtils.isEmpty(text)) {
                    if (openEncryptFolder(context, lockFile, path, text, callback)) {
                        if (fingerprintKey != null) {
                            byte[] enctyptedKey = FingerprintHelper.encryptBytes2Byte(fingerprintKey, text.getBytes());
                            cachedFolderKeys.add(path, enctyptedKey);
                        }
                    }
                }
            }
        });
        dialog.show();
    }

    private boolean openEncryptFolder(Context context, File lockFile, String path, String pass, OpenFolderCallback callback) {
        try {
            byte[] key = EncryptFolderUtils.decryptKey(pass, lockFile);
            folderKey = new Pair<>(path, Utils.bytesToHEX(key));
            callback.openFolder(path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show(context, context.getString(R.string.passowrd_error));
            return false;
        }
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

    private static class CachedFolderKeys {
        final List<CachedFolderKey> keys = new ArrayList<>();
        final File file;

        private CachedFolderKeys(File file) {
            this.file = file;
        }

        void load() {
            try {
                String text = FileUtils.readFile(file, "utf-8");
                JSONArray arrays = new JSONArray(text);
                for (int i = arrays.length() - 1; i >= 0; i--) {
                    JSONObject json = arrays.getJSONObject(i);
                    CachedFolderKey key = new CachedFolderKey();
                    key.path = json.getString("path");
                    key.encryptedKey = Utils.hexToBytes(json.getString("key"));
                    keys.add(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void remove(String path) {
            boolean needSave = false;
            Iterator<CachedFolderKey> iter = keys.iterator();
            while (iter.hasNext()) {
                CachedFolderKey key = iter.next();
                if (key.path.equals(path)) {
                    iter.remove();
                    needSave = true;
                }
            }
            if (needSave) {
                save();
            }
        }

        void add(String path, byte[] key) {
            remove(path);
            keys.add(new CachedFolderKey(path, key));
            save();
        }

        CachedFolderKey find(String path) {
            for (CachedFolderKey key : keys) {
                if (key.path.equals(path)) {
                    return key;
                }
            }
            return null;
        }

        void save() {
            try {
                JSONArray arrays = new JSONArray();
                for (CachedFolderKey key : keys) {
                    JSONObject json = new JSONObject();
                    json.put("path", key.path);
                    json.put("key", Utils.bytesToHEX(key.encryptedKey));
                    arrays.put(json);
                }
                String text = arrays.toString();
                FileUtils.write(file, text.getBytes("utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class CachedFolderKey {
        String path;
        byte[] encryptedKey;

        public CachedFolderKey(String path, byte[] encryptedKey) {
            this.path = path;
            this.encryptedKey = encryptedKey;
        }

        public CachedFolderKey() {
        }
    }
}
