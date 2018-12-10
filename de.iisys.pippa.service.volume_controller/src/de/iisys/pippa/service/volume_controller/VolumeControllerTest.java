package de.iisys.pippa.service.volume_controller;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.status.StatusAccess;
import de.iisys.pippa.support.status.Status;

public class VolumeControllerTest {

	static StatusAccess status = Status.getInstance();
	static Logger log = Logger.getAnonymousLogger();
	static VolumeControllerImpl vc = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//Bundle-NativeCode: lib/libmraajava.so ; osname=Linux ; processor=x86-64
		//System.setProperty("java.library.path", System.getProperty("java.library.path") + ":/usr/lib/x86_64-linux-gnu:");
		System.out.println(System.getProperty("java.library.path"));
		vc = new VolumeControllerImpl(status, log);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		
		vc.setVolumeTo(2);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vc.setVolumeTo(8);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fail("Not yet implemented");
	}

}
