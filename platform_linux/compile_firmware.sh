#!/bin/sh
set -e
platformdir="$(dirname $(readlink -f $0))"

export axoloti_release=${axoloti_release:="$platformdir/.."}
export axoloti_runtime=${axoloti_runtime:="$platformdir/.."}
export axoloti_firmware=${axoloti_firmware:="$axoloti_release/firmware"}
export axoloti_home=${axoloti_home:="$platformdir/.."}

export PATH=$PATH:${platformdir}/bin

echo PLATFORMDIR=${platformdir}
echo PATH=$PATH


cd ${axoloti_firmware}
${axoloti_firmware}/compile_firmware_linux.sh
