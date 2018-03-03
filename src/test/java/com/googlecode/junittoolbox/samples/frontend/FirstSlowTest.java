package com.googlecode.junittoolbox.samples.frontend;

import com.googlecode.junittoolbox.ParallelRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ParallelRunner.class)
public class FirstSlowTest {

	@Test
	public void slowOneTest() throws InterruptedException {
		sleep(2000);
		System.out.println("One");
	}

	@Test
	public void slowTwoTest() throws InterruptedException {
		sleep(2000);
		System.out.println("Two");
	}

	@Test
	public void slowThreeTest() throws InterruptedException {
		sleep(2000);
		System.out.println("Three");
	}

	private void sleep(int timeout) throws InterruptedException {
		Thread.sleep(timeout);
	}
}
