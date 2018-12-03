#!/bin/sh
FILE="ktdev-$(date +%Y-%m-%d-%H_%M).git"
case $1 in
    -d|--dir)
    DIRECTORY="$2"
    ;;
    *)
    echo "Invalid argument"
    exit 1
    ;;
esac

echo "Creating git bundle "${FILE}" in "${DIRECTORY}"..."
exec git bundle create ${DIRECTORY}/${FILE} --all

