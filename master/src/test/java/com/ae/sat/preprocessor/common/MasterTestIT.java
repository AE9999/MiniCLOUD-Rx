package com.ae.sat.preprocessor.common;

import com.ae.sat.master.App;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@ActiveProfiles("localDocker")
public class MasterTestIT {

	//@Ignore
	@Test
	public void contextLoads() {
	}

}
