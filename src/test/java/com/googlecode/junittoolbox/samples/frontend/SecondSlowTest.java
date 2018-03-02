package com.googlecode.junittoolbox.samples.frontend;

import com.googlecode.junittoolbox.ParallelRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ParallelRunner.class)
public class SecondSlowTest {

	@Test
	public void slowOneTest() throws InterruptedException {
		sleep(2000);
	}

	@Test
	public void slowTwoTest() throws InterruptedException {
		sleep(2000);
	}

	@Test
	public void slowThreeTest() throws InterruptedException {
		sleep(2000);
	}

	private void sleep(int timeout) throws InterruptedException {
		Thread.sleep(timeout);
	}
}
