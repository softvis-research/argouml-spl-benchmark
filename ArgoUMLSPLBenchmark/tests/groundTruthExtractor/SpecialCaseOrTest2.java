package jab;

// Example: org.argouml.uml.diagram.DiagramFactory.createInternal(DiagramType, Object, Object, DiagramSettings)
public class SpecialCaseOrTest2 {
	
	public void doSomething(){
            //#if defined(FEATUREA) or defined(FEATUREB)
            if (
                 //#if defined(FEATUREA)
                 true
                 //#endif
                 //#if defined(FEATUREA) and defined(FEATUREB)
                 ||
                 //#endif
                 //#if defined(FEATUREB)
                 false
                 //#endif
                  ) {
                // do something
            } else {
            //#endif
                // do something 2
            //#if defined(FEATUREA) or defined(FEATUREB)
            }
            //#endif
    }
	
}

// no FEATUREA nor FEATUREB
// do something 2

// FEATUREA
// if(true) { // do something } else { // do something 2 }

// FEATUREB
// if(false) { // do something } else { // do something 2 }

// FEATUREA and FEATUREB
// if(true || false) { // do something } else { // do something 2 }

