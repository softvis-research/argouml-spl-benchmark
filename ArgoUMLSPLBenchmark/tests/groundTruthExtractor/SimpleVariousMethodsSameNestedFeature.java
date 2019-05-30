//#if defined(FEATUREA)
//@#$LPS-ACTIVITYDIAGRAM:GranularityType:Class
package jab;

// Example found in org.argouml.uml.diagram.activity.ui.UMLActivityDiagram
public class SimpleVariousMethodsSameNestedFeature {
    
    //#if defined(FEATUREB)
    //@#$LPS-FEATUREB:GranularityType:Method
	public void doSomething(){
		// do something
	}
    //@#$LPS-FEATUREB:GranularityType:Method
	public void doSomethingElse(){
		// do something else
	}
    //@#$LPS-FEATUREB:GranularityType:Method
	public void doAnotherThing(){
		// do another thing
	}
    //#endif
	
}
//#endif