package lhg.gitnotes.app;

import android.content.Context;

import lhg.common.SimplePreference;

public class Paramters extends SimplePreference {
    public Entity_Boolean HasInitApp = new Entity_Boolean("HasInitApp");
    public Entity_String LastOpenGitRoot = new Entity_String("LastOpenGitRepo");

    static volatile Paramters sInstance;


    public static Paramters instance(Context context) {
        if (sInstance == null) {
            synchronized (Paramters.class) {
                if (sInstance == null) {
                    sInstance = new Paramters(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public Paramters(Context context) {
        super(context, "app_data");
    }
}
