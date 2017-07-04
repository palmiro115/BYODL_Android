package com.byodl.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
public class Annotations {
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
    @Generated(hash = 1462105703)
    private transient AnnotationsDao myDao;

    @Generated(hash = 860475110)
    public Annotations(Long id, @NotNull String uuid, Date date, String fileName) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.fileName = fileName;
    }

    @Generated(hash = 1906870294)
    public Annotations() {
    }

    public static Annotations fromLabels(DaoSession session, File f, Set<String> labels){
        if (f==null||labels==null)
            return null;
        Annotations a = new Annotations();
        a.setUuid(UUID.randomUUID().toString());
        a.setDate(new Date());
        a.setFileName(f.getAbsolutePath());
        for (String l:labels){
            Prediction pr = Prediction.createPrediction(a.getUuid(),l);
            session.getPredictionDao().insert(pr);
        }
        session.getAnnotationsDao().insert(a);
        return a;
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

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 272762071)
    public List<Prediction> getPredictions() {
        if (predictions == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PredictionDao targetDao = daoSession.getPredictionDao();
            List<Prediction> predictionsNew = targetDao._queryAnnotations_Predictions(uuid);
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

    public static Annotations getFirst(DaoSession session) {
        List<Annotations> pp = session.getAnnotationsDao().queryBuilder()
                .orderAsc(AnnotationsDao.Properties.Date)
                .limit(1)
                .list();
        if (pp!=null&&pp.size()>0)
            return pp.get(0);
        else
            return null;
    }

    public static void delete(DaoSession session, Annotations annotation) {
        session.getAnnotationsDao().delete(annotation);
    }

    public static long getAnnotationsCount(DaoSession session) {
        return session.getAnnotationsDao().count();
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1831060150)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAnnotationsDao() : null;
    }
}
