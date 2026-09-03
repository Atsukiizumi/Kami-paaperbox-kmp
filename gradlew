#!/bin/sh
APP_BASE_NAME=`basename "$0"`
APP_HOME=`cd "\`dirname \"$0\"\`" >/dev/null 2>&1 && pwd`

exec gradle "$@"
