/*
 * THIS SOFTWARE IS PROVIDED 'AS-IS', WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN NO EVENT WILL THE AUTHORS BE HELD LIABLE FOR ANY DAMAGES
 * ARISING FROM THE USE OF THIS SOFTWARE.
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this license document.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely.
 * 
 * This file was written by Vadym Radionov
 */
package com.github.vradionov.asyncflow;

// represent a value that will be delivered in the future

public class Promise<T> {

    private T value;
    private boolean available;
    private Exception exception;
    private Callback<T> callback;
    private boolean delivered;

    public void deliverException(Exception e) {
        exception = e;
        deliver();
    }

    public void deliver() {
        deliver(null);
    }

    public void deliver(T value) {
        synchronized (this) {
            if (available) {
                throw new IllegalStateException("result is already set");
            }
            this.value = value;
            available = true;
        }

        doDeliveryIfNeeded();
    }

    public void onDelivered(Callback<T> callback) {
        synchronized (this) {
            this.callback = callback;
        }

        doDeliveryIfNeeded();
    }
    
    
   
      
    private void doDeliveryIfNeeded() {

        Callback<T> c = null;
        T v = null;
        Exception e = null;
        
        synchronized (this) {
            if (available && !delivered && callback != null) {
                c = callback;
                v = value;
                e = exception;
                delivered = true;
            }
        }
        
        if (c != null) {
            if (e != null) {
                c.onFailure(e);
            } else {
                c.onResult(v);
            }
        }
    }

    public boolean isRealized() {
        return available;
    }

    public T getValue() {
        if (!isRealized()) {
            throw new IllegalStateException("promise is not realized yet");
        }
        return value;
    }

	public Exception getException() {
		if (!isRealized() || exception == null) {
			throw new IllegalStateException("promise is not realized or have exception");
		}
		return exception;
	}

  

    
}
