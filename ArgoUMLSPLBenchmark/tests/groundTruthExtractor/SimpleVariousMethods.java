package jab;

public class SimpleTestMethod {

	//#if defined(FEATUREA)
	//@#$LPS-COGNITIVE:GranularityType:Method
	public void doSomething(){
		// do something
	}
	//#endif
	
	public void doSomethingElse(){
		// do something else
	}
	
	//#if defined(FEATUREB)
	//@#$LPS-COGNITIVE:GranularityType:Method
	public void doAnotherThing(){
		// do another thing
	}
	//#endif
}