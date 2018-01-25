package jab;

public class SimpleElifTest {
	
	public void doSomething(){
		System.out.println("Hello");
		//#if defined(FEATUREA)
		//@#$LPS-COGNITIVE:GranularityType:Statement
		System.out.println(" world");
		//#elif defined(FEATUREB)
		//@#$LPS-COGNITIVE:GranularityType:Statement
		System.out.println(" everybody");
		//#endif
		System.out.println("!");
	}

}