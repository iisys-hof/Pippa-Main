package de.iisys.pippa.service.speech_out;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iisys.pippa.core.skill_executable.ASkillExecutable;
import de.iisys.pippa.core.speech_out.SpeechOutListener;

public class SpeechOutTest {

	SpeechOutImpl speechOut = null;
	ASkillExecutable execA = null;
	ASkillExecutable execB = null;
	ASkillExecutable execC = null;

	String harvardSentencesShort = "The birch canoe slid on the smooth planks.\r\n";

	String harvardSentencesLong = "The birch canoe slid on the smooth planks.\r\n"
			+ "Glue the sheet to the dark blue background.\r\n" + "It's easy to tell the depth of a well.\r\n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		String soundfilesPath = System.getProperty("user.home") + "\\Desktop\\Sound\\";

		File path = new File(soundfilesPath);

		for (File file : path.listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}

	}

	@Before
	public void setUp() throws Exception {

		speechOut = new SpeechOutImpl();
		execA = new TestSkillExecutable("ID1");
		execB = new TestSkillExecutable("ID2");
		execC = new TestSkillExecutable("ID3");

		speechOut.registerShort((SpeechOutListener) this.execA);
		speechOut.registerLong((SpeechOutListener) this.execB);

	}

	@After
	public void tearDown() throws Exception {

		String soundfilesPath = System.getProperty("user.home") + "\\Desktop\\Sound\\";

		File path = new File(soundfilesPath);

		for (File file : path.listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}

	}

	@Test
	public void testRegister() {

		assertEquals(this.speechOut.shortExecutable, this.execA);
		assertEquals(this.speechOut.longExecutable, this.execB);

	}

	@Test
	public void testSetShortOutputText() {

		this.speechOut.setOutputText((SpeechOutListener) this.execA, this.harvardSentencesShort, false);

		assertNotNull(speechOut.shortClip);
		assertNotNull(speechOut.shortLine);
		assertNotNull(speechOut.shortGain);

		assertNull(speechOut.longClip);
		assertNull(speechOut.longLine);
		assertNull(speechOut.longGain);

	}

	@Test
	public void testSetLongOutputText() {

		this.speechOut.setOutputText((SpeechOutListener) this.execB, this.harvardSentencesShort, false);

		assertNull(speechOut.shortClip);
		assertNull(speechOut.shortLine);
		assertNull(speechOut.shortGain);

		assertNotNull(speechOut.longClip);
		assertNotNull(speechOut.longLine);
		assertNotNull(speechOut.longGain);

	}

	@Test
	public void testSetWrongOutputText() {

		this.speechOut.setOutputText((SpeechOutListener) this.execC, this.harvardSentencesShort, false);

		assertNull(speechOut.shortClip);
		assertNull(speechOut.shortLine);
		assertNull(speechOut.shortGain);

		assertNull(speechOut.longClip);
		assertNull(speechOut.longLine);
		assertNull(speechOut.longGain);

	}

	@Test
	public void testShortOutput() {

		this.speechOut.setOutputText((SpeechOutListener) this.execA, this.harvardSentencesShort, false);

		this.speechOut.play((SpeechOutListener) this.execA);
		assertTrue(this.speechOut.startShort);
		assertFalse(this.speechOut.startLong);

		this.speechOut.pause((SpeechOutListener) this.execA);
		// no pause for short output, nothing to assert
		assertFalse(this.speechOut.pauseLong);

		this.speechOut.stop((SpeechOutListener) this.execA);
		assertTrue(this.speechOut.stopShort);
		assertFalse(this.speechOut.stopLong);

	}

	@Test
	public void testLongOutput() {

		this.speechOut.setOutputText((SpeechOutListener) this.execB, this.harvardSentencesShort, false);

		this.speechOut.play((SpeechOutListener) this.execB);
		assertFalse(this.speechOut.startShort);
		assertTrue(this.speechOut.startLong);

		this.speechOut.pause((SpeechOutListener) this.execB);
		// no pause for short output, nothing to assert
		assertTrue(this.speechOut.pauseLong);

		this.speechOut.stop((SpeechOutListener) this.execB);
		assertFalse(this.speechOut.stopShort);
		assertTrue(this.speechOut.stopLong);

	}

	@Test
	public void testWrongOutput() {

		this.speechOut.setOutputText((SpeechOutListener) this.execB, this.harvardSentencesShort, false);

		this.speechOut.play((SpeechOutListener) this.execC);
		assertFalse(this.speechOut.startShort);
		assertFalse(this.speechOut.startLong);

		this.speechOut.pause((SpeechOutListener) this.execC);
		// no pause for short output, nothing to assert
		assertFalse(this.speechOut.pauseLong);

		this.speechOut.stop((SpeechOutListener) this.execC);
		assertFalse(this.speechOut.stopShort);
		assertFalse(this.speechOut.stopLong);

	}

	/*
	 * @Test public void testListeningShort() {
	 * 
	 * Thread t = new Thread(this.speechOut); t.start();
	 * 
	 * this.speechOut.setOutputText((SpeechOutListener) this.execA,
	 * this.harvardSentencesShort, false); this.speechOut.play((SpeechOutListener)
	 * this.execA);
	 * 
	 * try { Thread.sleep(5000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * @Test public void testListeningLong() {
	 * 
	 * Thread t = new Thread(this.speechOut); t.start();
	 * 
	 * this.speechOut.setOutputText((SpeechOutListener) this.execB,
	 * this.harvardSentencesLong, false); this.speechOut.play((SpeechOutListener)
	 * this.execB);
	 * 
	 * try { Thread.sleep(2000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * this.speechOut.pause((SpeechOutListener) this.execB);
	 * 
	 * try { Thread.sleep(2000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * this.speechOut.play((SpeechOutListener) this.execB);
	 * 
	 * try { Thread.sleep(6000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * @Test public void testListeningBoth() {
	 * 
	 * Thread t = new Thread(this.speechOut); t.start();
	 * 
	 * this.speechOut.setOutputText((SpeechOutListener) this.execA,
	 * this.harvardSentencesShort, false);
	 * this.speechOut.setOutputText((SpeechOutListener) this.execB,
	 * this.harvardSentencesLong, false); this.speechOut.play((SpeechOutListener)
	 * this.execB);
	 * 
	 * try { Thread.sleep(1000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * this.speechOut.play((SpeechOutListener) this.execA);
	 * 
	 * try { Thread.sleep(8000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * @Test public void testListeningAbortShort() {
	 * 
	 * Thread t = new Thread(this.speechOut); t.start();
	 * 
	 * this.speechOut.setOutputText((SpeechOutListener) this.execA,
	 * this.harvardSentencesShort, false); this.speechOut.play((SpeechOutListener)
	 * this.execA);
	 * 
	 * try { Thread.sleep(1000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * this.speechOut.registerShort(execC);
	 * 
	 * try { Thread.sleep(9000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 * 
	 * @Test public void testListeningAbortLong() {
	 * 
	 * Thread t = new Thread(this.speechOut); t.start();
	 * 
	 * this.speechOut.setOutputText((SpeechOutListener) this.execB,
	 * this.harvardSentencesLong, false); this.speechOut.play((SpeechOutListener)
	 * this.execB);
	 * 
	 * try { Thread.sleep(2000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * this.speechOut.registerLong(execC);
	 * 
	 * try { Thread.sleep(8000); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 */
}
