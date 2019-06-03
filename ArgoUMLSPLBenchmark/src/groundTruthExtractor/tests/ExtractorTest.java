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
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void simpleTestMethod() {
		File f = new File("tests/groundTruthExtractor/SimpleTestMethod.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleTestMethod doSomething()", a.get(0));
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
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
		// no more results
		result.remove("FEATUREA");
		result.remove("FEATUREB");
		Assert.assertTrue(result.isEmpty());
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
		// no more results
		result.remove("FEATUREA");
		result.remove("FEATUREA_and_FEATUREB");
		result.remove("FEATUREA_and_FEATUREC");
		result.remove("FEATUREA_and_FEATUREC_and_FEATURED");
		Assert.assertTrue(result.isEmpty());
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
		// no more results
		result.remove("FEATUREA");
		result.remove("FEATUREB");
		result.remove("FEATUREA_and_FEATUREC");
		result.remove("FEATUREB_and_FEATUREC");
		Assert.assertTrue(result.isEmpty());
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
		// no more results
		result.remove("FEATUREA_and_FEATUREB");
		result.remove("FEATUREA_and_FEATUREB_and_FEATUREC");
		Assert.assertTrue(result.isEmpty());
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
		// no more results
		result.remove("FEATUREA");
		result.remove("not_FEATUREA");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void simpleVariousMethodsSameFeature() {
		File f = new File("tests/groundTruthExtractor/SimpleVariousMethodsSameFeature.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleVariousMethodsSameFeature doSomething()", a.get(0));
		Assert.assertEquals("jab.SimpleVariousMethodsSameFeature doSomethingElse()", a.get(1));
		Assert.assertEquals("jab.SimpleVariousMethodsSameFeature doAnotherThing()", a.get(2));
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void simpleVariousMethodsSameNestedFeature() {
		File f = new File("tests/groundTruthExtractor/SimpleVariousMethodsSameNestedFeature.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA_and_FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleVariousMethodsSameNestedFeature doSomething()", a.get(0));
		Assert.assertEquals("jab.SimpleVariousMethodsSameNestedFeature doSomethingElse()", a.get(1));
		Assert.assertEquals("jab.SimpleVariousMethodsSameNestedFeature doAnotherThing()", a.get(2));
		a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SimpleVariousMethodsSameNestedFeature", a.get(0));
		// no more results
		result.remove("FEATUREA");
		result.remove("FEATUREA_and_FEATUREB");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void classWithInnerClass() {
		File f = new File("tests/groundTruthExtractor/ClassWithInnerClass.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.ClassWithInnerClass.InnerClass doSomething()", a.get(0));
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void classWithInnerClass2Levels() {
		File f = new File("tests/groundTruthExtractor/ClassWithInnerClass2Levels.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.ClassWithInnerClass2Levels.InnerClass.InnerClass2 doSomething()", a.get(0));
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void methodParameters() {
		File f = new File("tests/groundTruthExtractor/MethodParameters.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.MethodParameters MethodParameters(String,String,String,String) Refinement", a.get(0));
		// TODO To check/think
		// This seems a bit arbitrary. Also correct would be jab.MethodParameters
		// MethodParameters(String,String,String) Refinement not_FEATUREA.
		// In fact, it can be considered two separate methods without refinement
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void specialCaseOrTest() {
		// (A or B) and A = A or (A and B) = A
		File f = new File("tests/groundTruthExtractor/SpecialCaseOrTest.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SpecialCaseOrTest doSomething()", a.get(0));
		Assert.assertEquals("jab.SpecialCaseOrTest", a.get(1));
		a = result.get("FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SpecialCaseOrTest", a.get(0));
		// no more results
		result.remove("FEATUREA");
		result.remove("FEATUREB");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void specialCaseOrTest2() {
		File f = new File("tests/groundTruthExtractor/SpecialCaseOrTest2.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SpecialCaseOrTest2 doSomething() Refinement", a.get(0));
		a = result.get("FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SpecialCaseOrTest2 doSomething() Refinement", a.get(0));
		a = result.get("FEATUREA_and_FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SpecialCaseOrTest2 doSomething() Refinement", a.get(0));
		// no more results
		result.remove("FEATUREA");
		result.remove("FEATUREB");
		result.remove("FEATUREA_and_FEATUREB");
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void specialCaseAndBWithNestedA() {
		File f = new File("tests/groundTruthExtractor/SpecialCaseAndBWithNestedA.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA_and_FEATUREB");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.SpecialCaseAndBWithNestedA", a.get(0));
		// no more results
		result.remove("FEATUREA_and_FEATUREB");
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void interfaceMethod() {
		File f = new File("tests/groundTruthExtractor/InterfaceMethod.java");
		Map<String, List<String>> result = GroundTruthExtractor.parseFile(f);
		List<String> a = result.get("FEATUREA");
		Assert.assertNotNull(a);
		Assert.assertEquals("jab.InterfaceMethod doSomething()", a.get(0));
		// no more results
		result.remove("FEATUREA");
		Assert.assertTrue(result.isEmpty());
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
