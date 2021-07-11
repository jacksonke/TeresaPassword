package com.jacksonke.teresapassword;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class DBHelper {

    private static DBHelper sInstance = new DBHelper();

    private BoxStore boxStore;

    private boolean isInited = false;

    private DBHelper() {

    }

    public static DBHelper getInstance(){
        return sInstance;
    }

    public void init(Context context){
        if (isInited){
            throw new RuntimeException("boxStore has alwready been built");
        }

        boxStore = MyObjectBox.builder().androidContext(context).build();
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }

    public List<SiteEntity> queryAll(){
      Box<SiteEntity> box = DBHelper.getInstance().getBoxStore().boxFor(SiteEntity.class);
      return box.getAll();
    }

    public SiteEntity queryBySite(String site){
      Box<SiteEntity> box = DBHelper.getInstance().getBoxStore().boxFor(SiteEntity.class);
      return box.query().equal(SiteEntity_.name, site).build().findFirst();
    }

    public long insertSiteEntity(@NonNull SiteEntity entity){
      assert entity.id == 0;
      Box<SiteEntity> box = DBHelper.getInstance().getBoxStore().boxFor(SiteEntity.class);
      return box.put(entity);
    }

    public void updateSiteEntity(@NonNull SiteEntity entity){
      assert entity.id != 0;
      Box<SiteEntity> box = DBHelper.getInstance().getBoxStore().boxFor(SiteEntity.class);
      box.put(entity);
    }

    public void rmSiteEntity(String siteName){
      Box<SiteEntity> box = DBHelper.getInstance().getBoxStore().boxFor(SiteEntity.class);
      List<SiteEntity> siteEntities = box.query().equal(SiteEntity_.name, siteName).build().find();
      SiteEntity item = null;
      if (siteEntities.size() > 0){
        item = siteEntities.get(0);
      }

      if (item != null){
        box.remove(item);
      }
    }


}
