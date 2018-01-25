<img src="https://github.com/but4reuse/argouml-spl-benchmark/blob/master/README_images/logo.png" align="right" width="300" height="300">

# ArgoUML SPL Benchmark
A feature location benchmark for single systems and for families of systems. We include the ground-truth, different scenarios and a program to calculate the feature location metrics.

## Setting-up
1. Download this repository by clicking here https://github.com/but4reuse/argouml-spl-benchmark/archive/master.zip and unzip it somewhere in your computer.

2. We will call the Benchmark functionality directly from the Java source code of the benchmark so first you need an Integrated Development Environment (IDE) where you can run Java source code and Apache Ant scripts. But do not worry if you are not expert on them because you will not need to modify anything there, you will need just to launch programs and we will show you how to do it. We will explain the steps using Eclipse but you can use any IDE supporting Java and ant if you know how to do it.

Download Eclipse from https://www.eclipse.org/downloads/ (We tested with Eclipse Oxygen and Eclipse Neon):

And then, select the Java Developers package. This package will have everything you need, if you select another, or you want to use one Eclipse that you have in your computer, you might have problems.

You will also need to have java installed in your computer (at least Java 1.6). You can check it opening the Command Prompt (cmd) and entering “java -version”

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image13.png"></center>

3. Run Eclipse and Import the projects. In the main menu, File -> import -> General -> Existing projects into workspace. 

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image11.png"></center>

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image17.png"></center>

In Select root directory, “browse” and select the folder where you have the unzipped content of this repository.

From the list select exactly these projects (Selecting others can cause problems, do not import other nested projects that might be suggested by Eclipse import):
* argouml-app
* argouml-build
* argouml-core-diagrams-sequence2
* argouml-core-infra
* argouml-core-model
* argouml-core-model-euml
* argouml-core-model-mdf
* argouml-core-tools
* ArgoUMLSPLBenchmark
* org.splevo.casestudy.argoumlspl.generator

Select the option "Copy projects into workspace".

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image8.png"></center>

The 8 projects starting with argoumlspl- which is the argouml-spl code base.
The project org.splevo.casestudy.argoumlspl.generator which is a helper to create variants from SPLEvo.
The project ArgoUMLSPLBenchmark is the Benchmark that you will need to use.

At the end of these steps, your Eclipse workspace should look like this:

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image1.png"></center>

Do not worry about the errors in the argouml- projects. The benchmark will work with them.

## Getting ready

### Generating the scenarios
In the ArgoUMLSPLBenchmark project, there is a folder called “scenarios” containing the predefined scenarios defined in the Benchmark. This step will allow to create the variants associated to each of these scenarios. There is a “configs” folder in each scenario with a list of config files that contains the list of features of each config.
To start with, open the “ScenarioOriginalVariant” folder, then right click the build.xml file and click on Run As -> Ant Build

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image22.png"></center>

The console will start showing the progress of the generation of the variants.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image2.png"></center>

And it will tell you when it will be finished.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image2.png"></center>

Once the build is finished. Refresh the folder of the scenario (right click the folder and refresh, or select the folder and press F5). You will have a folder called "variants" with a set of folders (each folder contains a variant).

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image12.png"></center>

In the case of this scenario, there is only one variant.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image21.png"></center>

Repeat this process for each scenario in the “scenarios” folder. Notice that, the scenario for example, ScenarioTraditionalVariants with 10 variants might take around half an hour. You will only need to do this process once for each scenario. At least, create the one for the ScenarioTraditionalVariants as it will be needed for the example presented in this document.

Some scenarios will have more than one build file. For example, the one that will generate all possible variants contains 6 parts. You will need to launch the 6 parts. 

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image16.png"></center>

We separated it in parts to try to avoid memory problems.

Troubleshooting (Out of memory error): You might have an out of memory error after the generation of several variants. It happens in a laptop after more than 170 generated variants.

```
BUILD FAILED
[...] java.lang.OutOfMemoryError: Compressed class space
Total time: 503 minutes 42 seconds
```

We suggest to launch the build.xml of an scenario and then restart Eclipse. It is also important, if a build fail, remove the variants folder of the scenario just to prevent that there are incomplete variants when the build failed.

### Basic information

This section explains what you need to do to use your feature location technique in this  benchmark. There are some important folders in the ArgoUMLSPLBenchmark project that you need to know:

* featuresInfo: It contains a features.txt file with the feature ids, feature names (separated by comma as there are synonyms or alternative namings) and the description. Id, names and description are separated by the symbol “;”.
You might want to use the information there to create queries for feature location techniques based on information retrieval.
scenarios: The benchmark predefined scenarios, you should provide the results for each of them. In each scenario you have the “variants” folder with the source code of each variant (now that you have created the scenarios) and a “configs” folder where you have information of the features present in each variant. You might want to use the information in the configs folder for intersection-based techniques. In the featuresInfo there is also the featureModel.txt which is a simple textual representation of the feature model of ArgoUML SPL using the feature ids.

* groundTruth: A set of 24 txt files containing the traces of the feature, feature combinations and feature negations of ArgoUML SPL.
Obviously, you cannot use this ground-truth information inside your feature location technique.
yourResults: This is the folder where you need to put your results (either manually or automatically, as you prefer). The results must be in the same format as the ground-truth.

* yourResultsMetrics: Once you put your results in the “yourResults” folder, you can launch the metrics calculation program to get a csv file in this “yourResultsMetrics” folder. We will show how in the next sections.

## A complete example showing the whole process
We have prepared an example of a feature location technique to show you the process. This technique will output the results in the “yourResults” folder as the benchmark is expecting. Remember that you can do this automatically, or you can just put the results there manually.
The technique is in the ArgoUMLSPLBenchmark project, in the src/techniqueExample package. Right click the java class there -> Run as -> Java Application

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image7.png"></center>

The console will output the process (and the technique itself also calculated and reports the time measure, remember to measure also the time of your feature location technique).

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image20.png"></center>

Then refresh “yourResults” folder (select the folder and press F5).

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image15.png"></center>

In the case of your feature location technique, the results will be the ones created from your technique. In fact, your feature location technique does not need to be in Java, you can use whatever you want and then put the results there.

Then, we launch the program to get the metrics. It is in the src/metricsCalculation package. Right click the java file, Run as -> Java application.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image6.png"></center>

You can see the progress in the console.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image4.png"></center>

Then, refresh “yourResultsMetrics” and you will have this csv file with all the metrics.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image14.png"></center>

In the console output you have also a gnuplot script that you can copy and paste in gnuplot. You can download gnuplot here: http://www.gnuplot.info/download.html (Tested with gnuplot 5.2).

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image5.png"></center>

And then click enter and you have the graph below. You can use as example to graphically report the metrics of a given scenario.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image10.png"></center>


## Utils for feature location techniques' developers
In the src/utils package of ArgoUMLSPLBenchmark project, you have some Util classes that might be useful if you are using Java to develop your feature location technique. However, you can still use the benchmark without using them. We present them just in case you want to take them.

FeatureUtils is helpful to get the information about features and configurations that you can use for each scenario.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image19.png"></center>

TraceIdUtils can be helpful to create the id of the traces needed for “yourResults” files. If you want to use it, this class expects you to use the JDT Java parser as the parameter types belongs to JDT.

<img align="middle" src="https://github.com/but4reuse/argouml-spl-benchmark/raw/master/README_images/image3.png"></center>

Finally, FileUtils has standard helpful methods to manipulate files, write in files etc.

## Launching an ArgoUML variant (if you want to do it for some reason)

If for some reason you want to launch a specific variant: in Eclipse, File -> import -> existing projects and select the folder of the generated variant. Now you will have this variant as an Eclipse project. Then, right click the file ArgoUML.launch that exists in the variant and click on Run as -> ArgoUML. The ArgoUML will be executed.

Troubleshooting (ArgoUML variant is not launching). If it is not executed and you have an error in the Eclipse console

```
[...] java.lang.Error: Unresolved compilation problem.
```

you should set java compiler compatibility with Java 1.6. For this, right click the imported project -> Properties -> Java Compiler and set the Compiler compliance level to 1.6.


## Technical documentation about the benchmark (no needed for feature location techniques’ developers)

### Ground-Truth extractor

GroundTruthExtractor.java has a main method used to create the txt files in the groundTruth folder based on a parsing of the ArgoUML SPL source code. It launch some JUnit tests that are in the extractor.tests package.

### Utils for creating the scenarios
RandomScenariosConfigsGenerator.java has a main method used to define the random scenarios.

ScenarioBuildXMLFilesGenerator.java has a main method used to create the build files of each scenario based on the content of the configs folder of each scenario.
