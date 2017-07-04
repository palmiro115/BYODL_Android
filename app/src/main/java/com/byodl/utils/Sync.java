package com.byodl.utils;

import android.os.Handler;

/**
 * Class to call runnable in defined interval
 */
public class Sync{
    //Handler to call runnable thread
    private Handler handler = new Handler();

    //Runnable to call
    private Runnable task;

    /**
     * Create an instance of sync class
     * @param task task to run
     */
    public Sync(Runnable task){
        this.task = task;
        handler.removeCallbacks(task);
    }

    /**
     * Start task just now
     */
    @SuppressWarnings("unused")
    public void now(){
        task.run();
    }

    /**
     * Start task in period ms
     * @param period period to run task
     */
    public void next(long period){
        handler.postDelayed(task, period);
    }

    /**
     * Cancel running task
     */
    public void stop(){
        handler.removeCallbacks(task);
    }

    /**
     * Update period
     * @param period period to run task, ms
     */
    @SuppressWarnings("unused")
    public void update(long period){
        stop();
        next(period);
    }
}
