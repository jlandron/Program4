#!/bin/sh

echo "Shutting down server on" $1 "at port " $2
java ServerShutdown $1 $2
echo "done"
