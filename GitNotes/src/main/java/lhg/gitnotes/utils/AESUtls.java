package lhg.gitnotes.utils;

import lhg.common.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

public class AESUtls {
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";


    public static String encryptHex2Hex(byte[] key, String hex) {
        return encryptBytes2Hex(key, Utils.hexToBytes(hex));
    }

    public static String encryptStr2Hex(byte[] key, String str) {
        try {
            return encryptBytes2Hex(key, str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encryptBytes2Hex(byte[] key, byte[] data) {
        try {
            return Utils.bytesToHEX(encryptBytes2Byte(key, data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptBytes2Byte(byte[] key, byte[] data) {
        try {
            final Key keySpec = createKey(key);
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            final byte[] encoded = cipher.doFinal(data);
            return encoded;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String decryptHex2Hex(byte[] key, String hex ) {
        return Utils.bytesToHEX(decryptHex2bytes(key, hex));
    }

    public static String decryptHex2Str(byte[] key, String hex ) {
        try {
            return new String(decryptHex2bytes(key, hex), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptHex2bytes(byte[] key, String hex) {
        try {
            final Key keySpec = createKey(key);
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            final byte[] bytes = Utils.hexToBytes(hex);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptFile2bytes(byte[] key, File file) throws Exception {
        return enOrDecryptFile2bytes(Cipher.DECRYPT_MODE, key, file);
    }
    public static void decryptIs2Os(byte[] key, InputStream is, OutputStream os) throws Exception {
        enOrDecryptIs2Os(Cipher.DECRYPT_MODE, key, is, os);
    }
    public static byte[] encryptFile2bytes(byte[] key, File file) throws Exception {
        return enOrDecryptFile2bytes(Cipher.ENCRYPT_MODE, key, file);
    }
    public static void encryptIs2Os(byte[] key, InputStream is, OutputStream os) throws Exception {
        enOrDecryptIs2Os(Cipher.ENCRYPT_MODE, key, is, os);
    }
    public static void encryptFile2File(byte[] key, File src, File dest) throws Exception {
        enOrDecryptFile2File(Cipher.ENCRYPT_MODE, key, src, dest);
    }
    public static void decryptFile2File(byte[] key, File src, File dest) throws Exception {
        enOrDecryptFile2File(Cipher.DECRYPT_MODE, key, src, dest);
    }

    public static void encryptIs2File(byte[] key, InputStream is, File dest) throws Exception {
        try(FileOutputStream fos = new FileOutputStream(dest)) {
            enOrDecryptIs2Os(Cipher.ENCRYPT_MODE, key, is, fos);
        }
    }

    public static void decryptIs2File(byte[] key, InputStream is, File dest) throws Exception {
        try(FileOutputStream fos = new FileOutputStream(dest)) {
            enOrDecryptIs2Os(Cipher.DECRYPT_MODE, key, is, fos);
        }
    }

    private static byte[] enOrDecryptFile2bytes(int opmode, byte[] key, File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            enOrDecryptIs2Os(opmode, key, fis, os);
            return os.toByteArray();
        }
    }


    private static void enOrDecryptFile2File(int opmode, byte[] key, File src, File dest) throws Exception {
        try(FileOutputStream fos = new FileOutputStream(dest)) {
            try (FileInputStream fis = new FileInputStream(src)) {
                enOrDecryptIs2Os(opmode, key, fis, fos);
            }
        }
    }

    private static void enOrDecryptIs2Os(int opmode, byte[] key, InputStream is, OutputStream os) throws Exception {
        CipherInputStream cis = null;
        try {
            final Key keySpec = createKey(key);
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(opmode, keySpec);
            cis = new CipherInputStream(is, cipher);
            byte[] buf = new byte[1024];
            int size = 0;
            while ((size = cis.read(buf)) > 0) {
                os.write(buf, 0, size);
            }

        } finally {
            if (cis != null) {
                try {
                    cis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Key createKey(byte[] keyByte) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return new SecretKeySpec(keyByte, KEY_ALGORITHM);
    }


}
