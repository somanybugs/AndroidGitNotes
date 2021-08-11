package lhg.gitnotes.git.ui;

import android.app.Activity;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import lhg.gitnotes.app.App;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;


public class UsernamePasswordCredentialsProviderImpl extends org.eclipse.jgit.transport.CredentialsProvider {
    private String username;
    private String password;
    private final FragmentManager fm;

    public UsernamePasswordCredentialsProviderImpl(String username, String password) {
        this.username = username;
        this.password = password;
        Activity activity = App.instance().getTopActivity();
        if (activity instanceof FragmentActivity) {
            this.fm = ((FragmentActivity) activity).getSupportFragmentManager();
        } else {
            this.fm = null;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username)
                continue;

            else if (i instanceof CredentialItem.Password)
                continue;

            else
                return false;
        }
        return true;
    }


    private boolean hasUserpass() {
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        if (supports(items)) {
            if (fm != null && !hasUserpass()) {
                String json = new UICredentialsProvider(fm).call("userpass");
                UICredentialsProvider.Userpass userpass = new Gson().fromJson(json, UICredentialsProvider.Userpass.class);
                if (userpass != null) {
                    username = (userpass.username);
                    password = (userpass.password);
                }
            }
        }

        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                ((CredentialItem.Username) i).setValue(username);
                continue;
            }
            if (i instanceof CredentialItem.Password) {
                ((CredentialItem.Password) i).setValue(password==null ? new char[0] : password.toCharArray());
                continue;
            }
            if (i instanceof CredentialItem.StringType) {
                if (i.getPromptText().equals("Password: ")) { //$NON-NLS-1$
                    ((CredentialItem.StringType) i).setValue(password);
                    continue;
                }
            }
            throw new UnsupportedCredentialItem(uri, i.getClass().getName()
                    + ":" + i.getPromptText()); //$NON-NLS-1$
        }
        return true;
    }
}
