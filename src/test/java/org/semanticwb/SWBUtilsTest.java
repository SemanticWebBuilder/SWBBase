package org.semanticwb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;

import org.junit.Assert;

public class SWBUtilsTest {
	private static final Logger log = SWBUtils.getLogger(SWBUtilsTest.class);
	
	@Test
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
	
	@Test
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

}
