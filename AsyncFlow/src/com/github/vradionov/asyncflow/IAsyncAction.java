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

public interface IAsyncAction<T,R> {
    public Promise<R> doAction(T input);
    public <R1> IAsyncAction<T,R1> then(IAsyncAction<R,R1> action);
}
