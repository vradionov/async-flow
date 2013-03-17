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

import java.util.concurrent.Executor;

// TODO description

public abstract class Action {

    public static <T,R> IAsyncAction<T,R> fromExecutor(final Executor executor, final IAction<T,R> action) {
        return new AsyncAction<T,R>() {

            public Promise<R> doAction(final T input) {
                final Promise<R> promise = new Promise<R>();
                executor.execute(new Runnable() {

                    public void run() {
                        try {
                            R result = action.doAction(input);
                            promise.deliver(result);
                        } catch (Exception e) {
                            promise.deliverException(e);
                        }
                    }
                });
                return promise;
            }
        };
    }
}
