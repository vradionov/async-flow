package com.github.vradionov.test;

import static org.junit.Assert.*;

import java.util.concurrent.Executor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.ExactComparisonCriteria;

import com.github.vradionov.asyncflow.Action;
import com.github.vradionov.asyncflow.AsyncFlow;
import com.github.vradionov.asyncflow.IAction;
import com.github.vradionov.asyncflow.IAsyncAction;
import com.github.vradionov.asyncflow.Promise;

public class AsyncFlowTest {
	
	Executor background = new Executor() {
		
		@Override
		public void execute(Runnable r) {
			r.run();
		}
	};
	
	private <T> IAction<T,T> returnAction(T likeThis) {
		return new IAction<T, T>() {

			@Override
			public T doAction(T input) throws Exception {
				return input;
			}
		};
	}
	
	private <T> IAction<T,T> throwAction(final String msg, T likeThis) {
		return new IAction<T, T>() {

			@Override
			public T doAction(T input) throws Exception {
				throw new Exception(msg);
			}
		};
	}
	
	private <T> IAction<T, Boolean> compareAction(final T compareWith) {
		return new IAction<T, Boolean>() {

			@Override
			public Boolean doAction(T input) throws Exception {
				return compareWith.equals(input);
			}
		};
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testThen() {
		for (int chainSize = 2; chainSize < 10; ++chainSize) {
			testThen(chainSize, 1, 1);
			testThen(chainSize, 1, 2);
		}
	}
	
	@Test
	public void testThenOnFailure() {
		for (int chainSize = 2; chainSize < 10; ++chainSize) {
			testThenOnFailure(chainSize, 1, 1);
		}
	}
	
	@Test
	public void testRecover() {
		for (int chainSize = 2; chainSize < 10; ++chainSize) {
			testRecover(chainSize, 1, 1);
			testRecover(chainSize, 1, 2);
		}
	}
	

	
	public <T> void testThen(int chainSize, T arg1, final T arg2) {
		
		if (chainSize < 2) {
			throw new IllegalStateException("chainSize must be >= 2");
		}
		
		IAsyncAction<T,T> action = Action.fromExecutor(background, returnAction(arg1));
		
		AsyncFlow<T> flow = AsyncFlow.create(action, arg1);
		for (int i = 1; i < chainSize - 1; i++) {
			flow = flow.then(action);
		}
		
		flow = flow.recover(new IAction<Exception, T>() {

			@Override
			public T doAction(Exception input) throws Exception {
				return arg2;
			}
		});
		
	    AsyncFlow<Boolean> finalFlow = flow.then(compareAction(arg2));
		
		Boolean result = waitForResult(finalFlow);
		Boolean result1 = arg1.equals(arg2);
	
		assertTrue(result.equals(result1));
	}
	

	public <T> void testThenOnFailure(int chainSize, T arg1, T arg2) {
		
		if (chainSize < 2) {
			throw new IllegalStateException("chainSize must be >= 2");
		}
		
		IAsyncAction<T,T> action = Action.fromExecutor(background, returnAction(arg1));
		IAsyncAction<T,T> throwAction = Action.fromExecutor(background, throwAction("msg", arg1));
		
		AsyncFlow<T> flow = AsyncFlow.create(throwAction, arg1);
		for (int i = 1; i < chainSize - 1; i++) {
			flow = flow.then(action);
		}
		
	    AsyncFlow<Boolean> finalFlow = flow.then(compareAction(arg2));
		
		Exception result = waitForException(finalFlow);
		assertNotNull(result);
	}

	
	public <T> void testRecover(int chainSize, final T arg1, T arg2) {
		
		if (chainSize < 2) {
			throw new IllegalStateException("chainSize must be >= 2");
		}
		
		IAsyncAction<T,T> action = Action.fromExecutor(background, returnAction(arg1));
		IAsyncAction<T,T> throwAction = Action.fromExecutor(background, throwAction("msg", arg1));
		
		AsyncFlow<T> flow = AsyncFlow.create(throwAction, arg2);
		for (int i = 1; i < chainSize - 1; i++) {
			flow = flow.then(action);
		}
		
		flow = flow.recover(new IAction<Exception, T>() {

			@Override
			public T doAction(Exception input) throws Exception {
				return arg1;
			}
		});
		
	    AsyncFlow<Boolean> finalFlow = flow.then(compareAction(arg2));
		

		Boolean result = waitForResult(finalFlow);
		Boolean result1 = arg1.equals(arg2);
	
		assertTrue(result.equals(result1));
	}
	
	
	
	private <T> T waitForResult(AsyncFlow<T> flow) {
		return waitForResult(flow.getPromise());
	}
	
	private <T> T waitForResult(Promise<T> promise) {
		
		while(!promise.isRealized()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return promise.getValue();
	}
	
	
	private <T> Exception waitForException(AsyncFlow<T> flow) {
		return waitForException(flow.getPromise());
	}
	
    private <T> Exception waitForException(Promise<T> promise) {
		
		while(!promise.isRealized()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return promise.getException();
	}
}
