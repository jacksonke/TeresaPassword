package com.jacksonke.teresapassword;

import android.support.annotation.NonNull;

import java.util.List;

import io.objectbox.Box;

public class Generator {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    static Generator _instance = new Generator();
    private String mSecret = "teresapassword";

    public static Generator instance(){
        return _instance;
    }

    private Generator(){
        // private constructor
    }

    public void setSecret(String secret){
        mSecret = secret;
    }

    public String generate(@NonNull SiteEntity entity){
        String siteName = entity.name;
        int type = entity.type;
        int ver = entity.ver;

        //
        if (siteName == null || siteName.trim().isEmpty()){
            throw new RuntimeException("site name is invalide");
        }

        return generate(siteName, mSecret, ver, type);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String generate(String sitename, String secret, int ver, int type);

}
