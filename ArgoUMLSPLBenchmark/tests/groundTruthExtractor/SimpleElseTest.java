package jab;

public class SimpleElseTest {
	
	public void doSomething(){
		System.out.println("Hello");
		//#if defined(FEATUREA)
		//@#$LPS-COGNITIVE:GranularityType:Statement
		System.out.println(" world");
		//#else
		System.out.println(" everybody");
		//#endif
		System.out.println("!");
	}

}