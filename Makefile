all: td-bisection.jar

td-bisection.jar: src/main/java/App.java
	mvn compile assembly:single
	mv target/td-bisection-1.0-jar-with-dependencies.jar td-bisection.jar

example: td-bisection.jar FORCE
	java -jar td-bisection.jar --show -g example/example.gr -td example/example.td

example-benchmark: td-bisection.jar example/example.gr example/example.td FORCE
	./benchmark example

clean:
	rm td-bisection.jar
	mvn clean

FORCE:
