package com.platypii.avyalert;


/**
 * A generic callback to notify of events
 * @author platypii
 */
public interface Callback<T> {

    public void callback(T result);
    
}
