#!/bin/bash
#####################################################################################
# Launch tmm without the updater (or the updater if tmm.jar is missing)
#####################################################################################

cd "$(dirname "$0")"

# have a look if we need to launch the updater or tmm directly
if [ -f tmm.jar ]; then
  ARGS="-Dsilent=noupdate"
fi

ARGS="$ARGS -Djava.net.preferIPv4Stack=true -Dappbase=http://www.tinymediamanager.org/"

# execute it :)
java $ARGS -jar getdown.jar .   