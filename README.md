# gatling-analyser

The utility processes the gatling log file and creates the result table with statistics.
For the utility to work correctly, transaction names in the test must not contain spaces.

mvn package - creates jar file

java -jar <filename>.jar - runs analyser
  
file config.properties must be in the same directory as jar file
