//#if defined(FEATUREA)
//@#$LPS-COGNITIVE:GranularityType:Package
package jab;

//#if defined(FEATUREB)
//@#$LPS-COGNITIVE:GranularityType:Import
import java.util.*;
//#endif

public class NestedTest {
	
	//#if defined(FEATUREC)
	//@#$LPS-COGNITIVE:GranularityType:Method
	public void doSomething(){
		System.out.println("Hello");
		//#if defined(FEATURED)
		//@#$LPS-COGNITIVE:GranularityType:Statement
		System.out.println(" world");
		//#endif
		System.out.println("!");
	}
	//#endif
	
	public void doSomethingElse(){
		// do something
	}

}
//#endif