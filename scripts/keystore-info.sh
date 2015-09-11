#!/bin/bash
#Make sure the path below are always correct.
# or add them to the system path
release_alias=release-key
release_key_file="~/Secure/oursaviorgames-keystore.jks"
debug_alias=androiddebugkey
debug_key_file="~/.android/debug.keystore"

if test -z $1; then
	echo "Usage: $0 <release,debug> [-v]"
	exit 1
fi
if test ! -z $2 && test $2 != "-v"; then
	echo "Usage: $0 <release,debug> [-v]"
	exit 1
fi

if test $1 = "debug"; then
	alias=$debug_alias
	key_file=$debug_key_file
fi
if test $1 = "release"; then
	alias=$release_alias
	key_file=$release_key_file
fi

cmd="keytool -exportcert -alias $alias -keystore $key_file"
if test ! -z $2 &&  test $2 = "-v"; then
	cmd="${cmd} -list -v"
	eval $cmd
else
	cmd="${cmd} | openssl sha1 -binary | openssl base64"
	eval $cmd
fi
