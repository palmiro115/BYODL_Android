package com.byodl.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;

@Entity
public class Labels {
    @Id
    private Long id;
    private String label;
    @Generated(hash = 834417431)
    public Labels(Long id, String label) {
        this.id = id;
        this.label = label;
    }
    @Generated(hash = 1048097501)
    public Labels() {
    }

    public Labels(String label) {
        this.label = label;
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public static void deleteAll(DaoSession session) {
        if (session==null)
            return;
        session.deleteAll(Labels.class);
    }

    public static void update(DaoSession session, List<String> labels) {
        if (session==null||labels==null)
            return;
        deleteAll(session);
        for (String label:labels){
            session.getLabelsDao().insert(new Labels(label));
        }
    }
    public static List<Labels> getLabels(DaoSession session){
        if (session==null)
            return null;
        return session.loadAll(Labels.class);
    }
}
