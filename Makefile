all:
	javac com/ergy/fset/*.java

test:
	java com/ergy/fset/TestSuite 10000 >& test.out

doc:
	javadoc -link http://docs.oracle.com/javase/7/docs/api/ -d Doc/JavaDoc com/ergy/fset/*Pure*.java

fset.jar: all
	jar cf fset.jar com/ergy/fset/Pure*.class
