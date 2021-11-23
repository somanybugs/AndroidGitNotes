package lhg.gitnotes.app;


import static androidx.biometric.BiometricConstants.ERROR_NEGATIVE_BUTTON;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lhg.common.utils.ByteUtils;
import lhg.common.utils.FileUtils;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;
import lhg.gitnotes.R;

public class FingerHelper {

    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";

    public static class Entity {
        public byte[] encryptedPassword;
        public byte[] iv;
        public byte[] random;
        public byte[] signature;

        private static final String FileName = "BiometricPasswordFile.json";
        static Entity instance;

        public static Entity get(Context context) {
            if (instance == null) {
                synchronized (Entity.class) {
                    if (instance == null) {
                        try {
                            String text = FileUtils.readFile(new File(context.getFilesDir(), FileName), "utf-8");
                            JSONObject json = new JSONObject(text);
                            Entity entity = new Entity();
                            entity.encryptedPassword = Utils.hexToBytes(json.getString("encryptedPassword"));
                            entity.iv = Utils.hexToBytes(json.getString("iv"));
                            entity.random = Utils.hexToBytes(json.getString("random"));
                            entity.signature = Utils.hexToBytes(json.getString("signature"));
                            instance = entity;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Entity entity = new Entity();
                            instance = entity;
                        }
                    }
                }
            }
            return instance;
        }

        public void save(Context context) {
            try {
                JSONObject json = new JSONObject();
                json.put("encryptedPassword", Utils.bytesToHEX(encryptedPassword));
                json.put("iv", Utils.bytesToHEX(iv));
                json.put("random", Utils.bytesToHEX(random));
                json.put("signature", Utils.bytesToHEX(signature));
                String text = json.toString();
                FileUtils.write(new File(context.getFilesDir(), FileName), text.getBytes("utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void reset() {
            encryptedPassword = null;
            iv = null;
            random = null;
            signature = null;
        }

        public boolean isEnable() {
            return notEmpty(encryptedPassword) && notEmpty(random) && notEmpty(signature) && notEmpty(iv);
        }

        private boolean notEmpty(byte[] bytes) {
            return bytes != null && bytes.length > 0;
        }

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


    public static byte[] decryptBytes2bytes(byte[] key, byte[] data) {
        try {
            final Key keySpec = createKey(key);
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Key createKey(byte[] keyByte) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return new SecretKeySpec(keyByte, KEY_ALGORITHM);
    }


    private static final String Provider = "AndroidKeyStore";

    private static final String KEY_NAME(Context context) {
        return context.getPackageName() + ":login";
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void generateSecretKey(Context context) {
        if (getSecretKey(context) != null) {
            return;
        }
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_NAME(context),
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
        builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Invalidate the keys if the user has registered a new biometric
            // credential, such as a new fingerprint. Can call this method only
            // on Android 7.0 (API level 24) or higher. The variable
            // "invalidatedByBiometricEnrollment" is true by default.
            builder.setInvalidatedByBiometricEnrollment(false);
        }
        KeyGenParameterSpec keyGenParameterSpec = builder.build();
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Provider);
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public static SecretKey getSecretKey(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance(Provider);
            // Before the keystore can be accessed, it must be loaded.
            keyStore.load(null);
            return ((SecretKey) keyStore.getKey(KEY_NAME(context), null));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Cipher getCipher(Context context, int encryptMode, byte[] iv) {
        SecretKey secretKey = getSecretKey(context);
        if (secretKey == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            if (encryptMode == Cipher.ENCRYPT_MODE) {
                cipher.init(encryptMode, secretKey);
            } else {
                cipher.init(encryptMode, secretKey, new IvParameterSpec(iv));
            }
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static class Setup {

        private static void showeFingerprintVerificationErrorDialog(Context context, String message, Runnable fail) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setTitle(message)
                    .setNegativeButton(context.getText(android.R.string.ok), (dialog, which) -> {
                        if (fail != null) {
                            fail.run();
                        }
                    })
                    .show();
        }

        public static boolean enableFingerprintVerification(FragmentActivity context, String message, Runnable fail, Runnable succ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerHelper.generateSecretKey(context);
            }

            Cipher cipher = FingerHelper.getCipher(context, Cipher.ENCRYPT_MODE, null);
            if (cipher == null) {
                if (fail != null) {
                    fail.run();
                }
                return false;
            }

            Executor executor = ContextCompat.getMainExecutor(context);
            BiometricPrompt biometricPrompt = new BiometricPrompt(context, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            if (errorCode != ERROR_NEGATIVE_BUTTON) {
                                showeFingerprintVerificationErrorDialog(context, message, fail);
                            }
                            Toast.makeText(context, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            try {
                                Cipher cipher = result.getCryptoObject().getCipher();
                                FingerHelper.Entity entity = FingerHelper.Entity.get(context);
                                byte[] plainPass = Utils.randomBytes(32);
                                Log.i("TESTT", "混淆密码 "+ plainPass);
                                entity.encryptedPassword = cipher.doFinal(plainPass);
                                entity.iv = cipher.getIV();
                                entity.random = Utils.randomBytes(128);
                                entity.signature = FingerHelper.encryptBytes2Byte(plainPass, entity.random);
                                entity.save(context);
                                if (succ != null) {
                                    succ.run();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showeFingerprintVerificationErrorDialog(context, message, fail);
                            }

                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(Utils.getApplicationName(context))
                    .setSubtitle(message)
                    .setNegativeButtonText(context.getString(android.R.string.cancel))
                    .build();
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
            return true;
        }
    }


    public static class Login {

        private FragmentActivity context;
        private BiometricPrompt biometricPrompt;
        private Callback callback;
        private Error initError;
        private Cipher cipher = null;

        public boolean init(FragmentActivity context) {
            this.context = context;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                generateSecretKey(context);
            }
            BiometricManager biometricManager = BiometricManager.from(context);
            if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                initError = (new Error("不支持指纹验证"));
                return false;
            }

            FingerHelper.Entity entity = FingerHelper.Entity.get(context);
            if (entity.encryptedPassword == null || entity.encryptedPassword.length == 0) {
                initError = (new Error("指纹验证密码为空"));
                return false;
            }

            cipher = getCipher(context, Cipher.DECRYPT_MODE, entity.iv);
            if (cipher == null) {
                initError = (new Error("KeyStore读取失败,请使用密码键盘"));
                return false;
            }

            return true;
        }

        public boolean support() {
            return initError == null;
        }

        public boolean show(Callback callback) {
            if (initError != null) {
                callback.onFinish(null, initError);
                return false;
            }
            final FingerHelper.Entity entity = FingerHelper.Entity.get(context);
            this.callback = callback;
            Executor executor = ContextCompat.getMainExecutor(context);
            biometricPrompt = new BiometricPrompt(context, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    ToastUtil.show(context, "Authentication error: " + errString);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    try {
                        byte[] pass = result.getCryptoObject().getCipher().doFinal(entity.encryptedPassword);
                        byte[] ret = FingerHelper.decryptBytes2bytes(pass, entity.signature);
                        if (!ByteUtils.equals(entity.random, ret)) {
                            entity.reset();
                            entity.save(context);
                            showFingerErrorDialog();
                            return;
                        }
                        Log.i("TESTT", "指纹解密密码 " + Utils.bytesToHEX(pass));
                        callback.onFinish(pass, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showFingerErrorDialog();
                    }
//                ToastUtil.show(context, "Authentication succeeded!");
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
//                ToastUtil.show(context, "Authentication failed");
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(Utils.getApplicationName(context))
                    .setSubtitle("指纹验证")
                    .setNegativeButtonText("使用密码")
                    .build();
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
            return true;
        }


        private void showFingerErrorDialog() {
            new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("指纹验证失败")
                    .setNegativeButton("确定", null)
                    .setOnDismissListener(dialog -> callback.onFinish(null, new Error("指纹验证失败")))
                    .show();
        }

        public interface Callback {
            void onFinish(byte[] key, Error error);
        }


    }


}
