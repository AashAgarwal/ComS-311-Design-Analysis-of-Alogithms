switch: run_tests_linux

run_tests: compile
	java -Xmx4g -cp ".;junit-4.12.jar;hamcrest-core-1.3.jar" org.junit.runner.JUnitCore AllTests

compile: *.java
	javac -cp ".;junit-4.12.jar;hamcrest-core-1.3.jar" *.java

run_tests_linux: compile_linux
	java -Xmx4g -cp ".:junit-4.12.jar:hamcrest-core-1.3.jar" org.junit.runner.JUnitCore AllTests

compile_linux: *.java
	javac -cp ".:junit-4.12.jar:hamcrest-core-1.3.jar" *.java