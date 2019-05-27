package jab;

// Example: org.argouml.profile.UserDefinedProfile.UserDefinedProfile(String, URL, Set<Critic>, Set<String>)
public class MethodParameters {
    
    public MethodParameters(String a, String b, 
            //#if defined(FEATUREA)
            //@#$LPS-FEATUREA:GranularityType:MethodSignature
    		String c,
            //#endif
    		String d) {
        // do something
    }

}
