package jab;

public class ClassWithInnerClass {

    static class InnerClass {
    	//#if defined(FEATUREA)
    	//@#$LPS-COGNITIVE:GranularityType:Method
    	public void doSomething(){
    		// do something
    	}
    	//#endif
    
}