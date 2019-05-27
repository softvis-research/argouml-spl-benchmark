package groundTruthExtractor.tests;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import groundTruthExtractor.GroundTruthExtractor;

/**
 * Tests for the extractor
 * 
 * @author jabier.martinez
 */
public class ExtractorTest {

	@Test
	public void simpleTestClass() {
		File f = new File("tests/groundTruthExtractor/SimpleTestClass.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestClass", a.get(0));
	}

	@Test
	public void simpleTestMethod() {
		File f = new File("tests/groundTruthExtractor/SimpleTestMethod.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestMethod doSomething()", a.get(0));
	}
	
	@Test
	public void simpleVariousMethods() {
		File f = new File("tests/groundTruthExtractor/SimpleVariousMethods.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestMethod doSomething()", a.get(0));
		a = result.get("FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestMethod doAnotherThing()", a.get(0));
	}

	@Test
	public void nestedTest() {
		File f = new File("tests/groundTruthExtractor/NestedTest.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.NestedTest", a.get(0));
		a = result.get("FEATUREA_and_FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.NestedTest Refinement", a.get(0));
		a = result.get("FEATUREA_and_FEATUREC");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.NestedTest doSomething()", a.get(0));
		a = result.get("FEATUREA_and_FEATUREC_and_FEATURED");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.NestedTest doSomething() Refinement", a.get(0));
	}

	@Test
	public void simpleOrTest() {
		File f = new File("tests/groundTruthExtractor/SimpleOrTest.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleOrTest", a.get(0));
		a = result.get("FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleOrTest", a.get(0));
		a = result.get("FEATUREA_and_FEATUREC");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleOrTest doSomething()", a.get(0));
		a = result.get("FEATUREB_and_FEATUREC");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleOrTest doSomething()", a.get(0));
	}
	
	@Test
	public void simpleAndTest() {
		File f = new File("tests/groundTruthExtractor/SimpleAndTest.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA_and_FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleAndTest", a.get(0));
		a = result.get("FEATUREA_and_FEATUREB_and_FEATUREC");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleAndTest doSomething()", a.get(0));
	}

	@Test
	public void simpleElseTest() {
		File f = new File("tests/groundTruthExtractor/SimpleElseTest.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleElseTest doSomething() Refinement", a.get(0));
		a = result.get("not_FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleElseTest doSomething() Refinement", a.get(0));
	}
	
	@Test
	public void simpleVariousMethodsSameFeature() {
		File f = new File("tests/groundTruthExtractor/SimpleVariousMethodsSameFeature.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestMethod doSomething()", a.get(0));
		Assert.assertEquals("jab.SimpleTestMethod doSomethingElse()", a.get(1));
		Assert.assertEquals("jab.SimpleTestMethod doAnotherThing()", a.get(2));
	}
	
	@Test
	public void simpleVariousMethodsSameNestedFeature() {
		File f = new File("tests/groundTruthExtractor/SimpleVariousMethodsSameNestedFeature.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA_and_FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestMethod doSomething()", a.get(0));
		Assert.assertEquals("jab.SimpleTestMethod doSomethingElse()", a.get(1));
		Assert.assertEquals("jab.SimpleTestMethod doAnotherThing()", a.get(2));
	}
	
	// @Test
	// // TODO This test fails but there is no ELIF in ArgoUML SPL
	// public void simpleElifTest() {
	// File f = new File("tests/SimpleElifTest.java");
	// Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
	// List<String> a = result.get("FEATUREA");
	// Assert.assertNotNull(a);
	// Assert.assertEquals("jab.SimpleElifTest doSomething() Refinement",
	// a.get(0));
	// a = result.get("FEATUREB");
	// Assert.assertNotNull(a);
	// Assert.assertEquals("jab.SimpleElifTest doSomething() Refinement",
	// a.get(0));
	// }

}
