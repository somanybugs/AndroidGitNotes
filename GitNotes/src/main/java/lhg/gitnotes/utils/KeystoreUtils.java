package lhg.gitnotes.utils;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeystoreUtils {
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
        SecretKey secretKey = KeystoreUtils.getSecretKey(context);
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
}
