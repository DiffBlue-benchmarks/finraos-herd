#This script attempts to compile unit tests, and deletes test files that don't compile
#The purpose of this script is to make it easier to work with testsuites that contain non-compileable tests.
#Intended usage.
# 1: Run ./delete-bad-tests.sh
# 2: mvn test-compile will run, if there is a non-compilable test, it will be removed from its .java file
# 3: step 2 is repeated until mvn test-compile finishes succesfully (all tests compiled).

set -e
while ! mvn test-compile --batch-mode > outp.txt ; do
   FILE=`grep "\[ERROR\] /[^:]*\.java:" outp.txt | head -n 1 | sed "s/\[ERROR\] \(\/[^:]*\.java\).*/\1/"`
   LINE=`grep "\[ERROR\] /[^:]*\.java:" outp.txt | head -n 1 | sed "s/\[ERROR\] \/[^:]*\.java:.\([0-9]*\).*/\1/"`
   echo $FILE $LINE
   ./delete-test.sh $FILE $LINE
done

