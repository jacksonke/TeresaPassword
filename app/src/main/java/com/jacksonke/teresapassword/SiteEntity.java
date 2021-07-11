package com.jacksonke.teresapassword;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by jacksonke on 30/08/2018.
 *
 *
 */
@Entity
public class SiteEntity {
    @Id
    long id;

    String name;
    int ver;
    int type = Constant.TYPE_NUMBER_ABC;

    public SiteEntity(String siteName){
        id = 0;
        name = siteName;
        ver = 0;
        type = Constant.TYPE_NUMBER_ABC;
    }


    public SiteEntity(){
        id = 0;
        name = "";
        ver = 0;
    }

    @Override
    public String toString() {
        return name;
    }

    String getDebugString(){
        String ret = "";
        if (BuildConfig.DEBUG){
            ret = ret + " id:" + id
                + " name:" + name
                + " ver:" + ver
                + " type:" + type;
        }

        return ret;
    }
}
