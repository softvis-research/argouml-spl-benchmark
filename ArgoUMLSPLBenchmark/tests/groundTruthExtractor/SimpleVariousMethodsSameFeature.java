package jab;

public class SimpleVariousMethodsSameFeature {
    
    //#if defined(FEATUREA)
    //@#$LPS-FEATUREA:GranularityType:Method
	public void doSomething(){
		// do something
	}
    //@#$LPS-FEATUREA:GranularityType:Method
	public void doSomethingElse(){
		// do something else
	}
    //@#$LPS-FEATUREA:GranularityType:Method
	public void doAnotherThing(){
		// do another thing
	}
    //#endif
	
}