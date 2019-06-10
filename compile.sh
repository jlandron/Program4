#!/bin/sh

rm -f *~
rm -f *.class

echo "client compilation"
javac *.java
rmic FileClient
echo "done"

echo "server compilation"
rmic FileServer
echo "done"
