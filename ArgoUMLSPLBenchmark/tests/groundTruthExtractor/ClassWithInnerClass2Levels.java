package jab;

// Example: org.argouml.uml.ui.TabConstraints.ConstraintModel.CR
public class ClassWithInnerClass2Levels {

    static class InnerClass {
    	
    	static class InnerClass2 {
    		//#if defined(FEATUREA)
    		//@#$LPS-FEATUREA:GranularityType:Method
    		public void doSomething(){
    			// do something
    		}
    		//#endif
    	}
    }
}