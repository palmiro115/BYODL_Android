package com.byodl.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.tensorflow.contrib.android.Classifier;

import java.util.UUID;

@Entity
public class Prediction {
    @Id
    private Long id;

    @Unique
    @NotNull
    private String uuid;

    @NotNull
    private String parentUuid;

    private String label;

    private float value;

    private boolean isManual;

    public static Prediction createPrediction(String parentUuid,Classifier.Recognition recognition){
        if (parentUuid==null||recognition==null)
            return null;
        Prediction p = new Prediction();
        p.setUuid(UUID.randomUUID().toString());
        p.setParentUuid(parentUuid);
        p.setLabel(recognition.getTitle());
        p.setValue(recognition.getConfidence());
        p.setIsManual(false);
        return p;
    }
    public static Prediction createPrediction(String parentUuid,String label){
        if (parentUuid==null||label==null)
            return null;
        Prediction p = new Prediction();
        p.setUuid(UUID.randomUUID().toString());
	    p.setParentUuid(parentUuid);
        p.setLabel(label);
        p.setIsManual(true);
        return p;
    }
    @Generated(hash = 57186713)
    public Prediction(Long id, @NotNull String uuid, @NotNull String parentUuid, String label,
            float value, boolean isManual) {
        this.id = id;
        this.uuid = uuid;
        this.parentUuid = parentUuid;
        this.label = label;
        this.value = value;
        this.isManual = isManual;
    }
    @Generated(hash = 619682788)
    public Prediction() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParentUuid() {
        return this.parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean getIsManual() {
        return this.isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }
    public String getUuid() {
        return this.uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
