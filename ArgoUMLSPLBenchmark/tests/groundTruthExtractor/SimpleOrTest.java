//#if defined(FEATUREA) or defined(FEATUREB)
//@#$LPS-FEATUREA:GranularityType:Package
package jab;

public class SimpleOrTest {
	
	//#if defined(FEATUREC)
	//@#$LPS-FEATUREC:GranularityType:Method
	public void doSomething(){
		// do something
	}
	//#endif

}
//#endif