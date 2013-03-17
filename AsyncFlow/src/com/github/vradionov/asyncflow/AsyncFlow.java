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

public class AsyncFlow<R> {

    private final Promise<R> promise;

    public AsyncFlow(Promise<R> promise) {
        this.promise = promise;
    }

    public static <T,R> AsyncFlow<R> create(IAsyncAction<T,R> action, T arg) {
        Promise<R> promise = action.doAction(arg);
        return new AsyncFlow<R>(promise);
    }

    public <R1> AsyncFlow<R1> then(final IAction<R,R1> action) {

        final Promise<R1> resultPromise = new Promise<R1>();

        promise.onDelivered(new Callback<R>() {

            public void onResult(R result) {
                R1 newResult;
                try {
                    newResult = action.doAction(result);
                } catch (Exception e) {
                    resultPromise.deliverException(e);
                    return;
                }

                resultPromise.deliver(newResult);

            }

            public void onFailure(Exception e) {
                resultPromise.deliverException(e);
            }
        });

        return new AsyncFlow<R1>(resultPromise);
    }

    public <R1> AsyncFlow<R1> then(final IAsyncAction<R,R1> action) {
        final Promise<R1> resultPromise = new Promise<R1>();

        promise.onDelivered(new Callback<R>() {

            public void onResult(R result) {
                Promise<R1> newPromise = action.doAction(result);
                newPromise.onDelivered(new Callback<R1>() {

                    public void onResult(R1 result) {
                        resultPromise.deliver(result);
                    }

                    public void onFailure(Exception e) {
                        resultPromise.deliverException(e);
                    }
                });

            }

            public void onFailure(Exception e) {
                resultPromise.deliverException(e);
            }
        });

        return new AsyncFlow<R1>(resultPromise);
    }

    public AsyncFlow<R> recover(final IAction<Exception,R> action) {
        final Promise<R> resultPromise = new Promise<R>();

        promise.onDelivered(new Callback<R>() {

            public void onResult(R result) {

                resultPromise.deliver(result);

            }

            public void onFailure(Exception e) {

                R result;
                try {
                    result = action.doAction(e);
                } catch (Exception e1) {
                    resultPromise.deliverException(e1);
                    return;
                }

                resultPromise.deliver(result);
            }
        });

        return new AsyncFlow<R>(resultPromise);
    }

    public AsyncFlow<R> recover(final IAsyncAction<Exception,R> action) {
        final Promise<R> resultPromise = new Promise<R>();

        promise.onDelivered(new Callback<R>() {

            public void onResult(R result) {

                resultPromise.deliver(result);

            }

            public void onFailure(Exception e) {

                Promise<R> newPromise = action.doAction(e);
                newPromise.onDelivered(new Callback<R>() {

                    public void onResult(R result) {
                        resultPromise.deliver(result);
                    }

                    public void onFailure(Exception e) {
                        resultPromise.deliverException(e);
                    }
                });
            }
        });

        return new AsyncFlow<R>(resultPromise);
    }

    public <Any> AsyncFlow<Any> inParallelWithNext(final IAsyncAction<R,Any> action) {
        final Promise<Any> resultPromise = new Promise<Any>();

        promise.onDelivered(new Callback<R>() {

            @SuppressWarnings("unused")
            public void onResult(R result) {
                Promise<Any> newPromise = action.doAction(result);
                resultPromise.deliver(); // force running rest chain before the action completed
            }

            public void onFailure(Exception e) {
                resultPromise.deliverException(e);
            }
        });

        return new AsyncFlow<Any>(resultPromise);
    }

    public AsyncFlow<R> returnInExecutor(final Executor executor) {
        final Promise<R> resultPromise = new Promise<R>();
        
        promise.onDelivered(new Callback<R>() {

            @Override
            public void onResult(final R result) {
                executor.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        resultPromise.deliver(result);
                    }
                });
            }

            @Override
            public void onFailure(final Exception e) {
                executor.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        resultPromise.deliverException(e);
                    }
                });
            }
        });
        
        return new AsyncFlow<R>(resultPromise);
    }

    public Promise<R> getPromise() {
        return promise;
    }

}
