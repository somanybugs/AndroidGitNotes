package lhg.gitnotes.utils;

import com.google.gson.Gson;
import lhg.gitnotes.note.LockFileEntity;
import lhg.common.utils.FileUtils;
import lhg.common.utils.Utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptFolderUtils {
    private static final int KeySize = 32;

    public static byte[] createInputKey(String password, byte[] random) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] k1 = password.getBytes("utf-8");
        byte[] all = new byte[k1.length + random.length];
        System.arraycopy(k1, 0, all, 0, k1.length);
        System.arraycopy(random, 0, all, k1.length, random.length);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(all);
        return md5;
    }

    public static LockFileEntity createLockFileEntity(String password, Object[] out) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] key = Utils.randomBytes(KeySize);
        byte[] random = Utils.randomBytes(256);
        byte[] inputKey = createInputKey(password, random);
        LockFileEntity lockFileEntity = new LockFileEntity();
        lockFileEntity.key = AESUtls.encryptBytes2Hex(inputKey, key);
        lockFileEntity.random = Utils.bytesToHEX(random);
        lockFileEntity.signature = AESUtls.encryptBytes2Hex(key, random);
        if (out != null) {
            out[0] = key;
        }
        return lockFileEntity;
    }

    public static byte[] createLockFile(String password, File lockFile) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Object[] out = new Object[1];
        LockFileEntity lockFileEntity = createLockFileEntity(password, out);
        String json = new Gson().toJson(lockFileEntity);
        FileUtils.write(lockFile.getAbsolutePath(), json.getBytes("utf-8"));
        return (byte[]) out[0];
    }

    public static byte[] decryptKey(String password, File lockFile) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String json = FileUtils.readFile(lockFile, "utf-8");
        LockFileEntity lockFileEntity = new Gson().fromJson(json, LockFileEntity.class);
        byte[] random = Utils.hexToBytes(lockFileEntity.random);
        byte[] inputKey = createInputKey(password, random);
        byte[] key = AESUtls.decryptHex2bytes(inputKey, lockFileEntity.key);
        String signature = AESUtls.encryptBytes2Hex(key, random);
        if (!signature.equals(lockFileEntity.signature)) {
            throw new IllegalArgumentException("Password is error");
        }
        return key;
    }

}
