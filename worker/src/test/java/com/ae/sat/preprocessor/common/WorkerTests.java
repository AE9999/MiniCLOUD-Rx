package com.ae.sat.preprocessor.common;

import com.ae.sat.preprocessor.common.servers.worker.App;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
public class WorkerTests {

	@Ignore
	@Test
	public void contextLoads() {
	}

}
