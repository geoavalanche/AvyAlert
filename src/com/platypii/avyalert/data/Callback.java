package com.platypii.avyalert.data;


/**
 * A generic callback to notify of events
 * @author platypii
 */
public interface Callback<T> {

    public void callback(T result);
    
}
