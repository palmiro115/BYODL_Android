package com.byodl.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;
import org.tensorflow.contrib.android.Classifier;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Predictions {
    @Id
    private Long id;

    @Unique
    @NotNull
    private String uuid;

    private Date date;

    private String fileName;

    @ToMany(joinProperties = {
            @JoinProperty(name = "uuid", referencedName = "parentUuid")
    })
    private List<Prediction> predictions;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 336565460)
    private transient PredictionsDao myDao;

    @Generated(hash = 1396714850)
    public Predictions(Long id, @NotNull String uuid, Date date, String fileName) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.fileName = fileName;
    }

    @Generated(hash = 47080558)
    public Predictions() {
    }


    public static Predictions fromPredictions(DaoSession session,File f, List<Classifier.Recognition> recognitions){
        if (f==null||recognitions==null)
            return null;
        Predictions p = new Predictions();
        p.setUuid(UUID.randomUUID().toString());
        p.setDate(new Date());
        p.setFileName(f.getAbsolutePath());
        for (Classifier.Recognition r:recognitions){
            Prediction pr = Prediction.createPrediction(p.getUuid(),r);
            session.getPredictionDao().insert(pr);
        }
        session.getPredictionsDao().insert(p);
        return p;
    }

    public static Predictions getLastPredictions(DaoSession session) {
        List<Predictions> pp = session.getPredictionsDao().queryBuilder()
                .orderDesc(PredictionsDao.Properties.Date)
                .limit(1)
                .list();
        if (pp!=null&&pp.size()>0)
            return pp.get(0);
        else
            return null;
    }
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 988878412)
    public List<Prediction> getPredictions() {
        if (predictions == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PredictionDao targetDao = daoSession.getPredictionDao();
            List<Prediction> predictionsNew = targetDao._queryPredictions_Predictions(uuid);
            synchronized (this) {
                if (predictions == null) {
                    predictions = predictionsNew;
                }
            }
        }
        return predictions;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 680166926)
    public synchronized void resetPredictions() {
        predictions = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 120611657)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPredictionsDao() : null;
    }
}
