//#if defined(FEATUREA) and defined(FEATUREB) 
//@#$LPS-FEATUREA:GranularityType:Package
//@#$LPS-FEATUREB:GranularityType:Class

package jab;

//#if defined(FEATUREA)
//@#$LPS-FEATUREA:GranularityType:Import
//@#$LPS-FEATUREA:Localization:NestedIfdef-FEATUREB
import x;
//#endif

// Example: org.argouml.uml.cognitive.critics.CrClassWithoutComponent
public class SpecialCaseAndBWithNestedA {
	
}
//#endif