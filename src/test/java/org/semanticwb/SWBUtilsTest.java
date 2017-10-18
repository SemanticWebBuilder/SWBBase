package org.semanticwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;
import org.w3c.dom.Document;

import org.junit.Assert;

public class SWBUtilsTest {
	private static final Logger log = SWBUtils.getLogger(SWBUtilsTest.class);
	
	//@Test
	public void TestXMLToDom() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("cd_catalog.xml").getFile());
		try (FileInputStream fis = new FileInputStream(file)) {
			String content = SWBUtils.IO.readInputStream(fis, "utf-8");
			Document dom = SWBUtils.XML.xmlToDom(content);
			Assert.assertTrue(content.equals(SWBUtils.XML.domToXml(dom, false)));
		} catch (IOException ioex) {
			log.error(ioex);
		}
	}
	
	//@Test
	public void TestCopyDom() throws SWBException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("cd_catalog.xml").getFile());
		try (FileInputStream fis = new FileInputStream(file)) {
			String content = SWBUtils.IO.readInputStream(fis, "utf-8");
			Document dom = SWBUtils.XML.xmlToDom(content);
			Document dom2 = SWBUtils.XML.copyDom(dom);
			
			Assert.assertTrue(content.equals(SWBUtils.XML.domToXml(dom2, false)));
		} catch (IOException ioex) {
			log.error(ioex);
		}
	}
	
	@Test
	public void TestLocaleDates() {
		//DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, new Locale("en"));
		SimpleDateFormat sdf = new SimpleDateFormat("ss", new Locale("es"));
		System.out.println(sdf.format(new Date()));
		//System.out.println(SWBUtils.TEXT.getStrDate(new Date(), "en", null));
		System.out.println(SWBUtils.TEXT.getStrDate(new Date(), "es", "ss"));
		//System.out.println(sdf.format(new Date()));
	}

}
