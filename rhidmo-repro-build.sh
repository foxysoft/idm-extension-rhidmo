#!/bin/bash
###############################################################################
# Copyright 2025 Lambert Giese
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
###############################################################################

gScriptName="$(basename "$0")"
gScriptDir="$(readlink -f "$(dirname "$0")")"
gDockerImageTag=maven:3.9.11-eclipse-temurin-8
gDockerImageDigest=sha256:8135a3d9d2247f75973a23c984baf0b0b758eed1a85491d78fd4340a9cb35f76
gDockerGroup=docker
gOptionClean=0
gOptionQuiet=0
gOptionHelp=0
declare -a gMavenArgs

function msgErr() {
  >&2 printf "[\e[1;31mERROR\e[0m]: %s\n" "$@"
}

function msgWarn() {
  >&2 printf "[\e[1;33mWARNING\e[0m]: %s\n" "$@"
}

function msgInfo() {
  >&2 printf "[\e[1;32mINFO\e[0m]: %s\n" "$@"
}

function parseArgs() {
  while [ "${1:-}" != '' ]; do
    case "$1" in
      '-c' | '--clean')
        gOptionClean=1
        ;;
      '-q' | '--quiet')
        gOptionQuiet=1
        ;;
      '-h' | '--help')
        gOptionHelp=1
        ;;
      *)
        msgErr "Unknown parameter: $1"
        return 1
        ;;
    esac
    shift
  done
}

function usage() {
  >&2 cat << eof
Usage: $gScriptName [options...]

Create or delete Rhidmo clean build with reproducible binary output
DON'T USE FOR ROUTINE DEVELOPER BUILDS - IT'S A WASTE OF BANDWIDTH AND CPU.

OPTIONS:
    [ -c | --clean   ]    Delete build results (mvn clean)
    [ -q | --quiet   ]    Build or clean without any diagnostic output
                          except for the final SHA-256 on stdout
    [ -h | --help    ]    Display this help message

Without any options, creates a fresh containerized build environment,
executes 'mvn clean package' inside and displays the SHA-256 digest
of the resulting Rhidmo ZIP archive.

With --clean, deletes all target files and directories from previous
reproducible builds by executing 'mvn clean' inside the container.
This is useful since target files and directories are owned by root,
hence normal users will lack permission to delete them.

EXIT CODES:
    0        : success
    non-zero : failure
eof
}

function setupDescriptorsForQuietMode() {
  tty -s
  if [[ $? -eq 1 || "$gOptionQuiet" == "1" ]]; then
    exec 3>&1 &> /dev/null
  else
    exec 3>&1
  fi
}

function checkPrerequisites() {
  if ! command -v docker > /dev/null; then
    msgErr "This script requires docker" \
      "Visit https://docs.docker.com/engine/install/" \
      "and apply installation instructions for your OS"
    return 1
  fi

  if ! id -nG | grep -qw "$gDockerGroup"; then
    msgWarn "Current user is not member of group $gDockerGroup" \
      "If your build fails with permission errors," \
      "visit https://docs.docker.com/engine/install/linux-postinstall/" \
      "and apply required post-installation steps."
  fi
}

function displayDigest() {
  local zipfile
  local digest

  zipfile="$(find "$gScriptDir"/ear/target -name rhidmo\*zip)"
  rc=$?

  if [[ "$rc" != "0" || ! -f "$zipfile" ]]; then
    msgErr "Rhidmo ZIP archive not found after build, RC=$rc"
    return 1
  fi

  # Use SHA-256 (not SHA-512) because that's what GitHub releases UI shows as of mid 2025
  digest="$(sha256sum "$zipfile" | cut -d' ' -f1)"
  rc=$?

  if ((rc != 0)); then
    msgErr "Rhidmo reproducible build successful, but calculating SHA-256 failed with RC=$rc"
    return 1
  fi

  msgInfo "Reproducible SHA-256 of Rhidmo ZIP archive:"

  # Output digest to stdout even with --quiet
  >&3 echo 'sha256:'"$digest"

}

function doBuild() {
  local -i rc

  msgInfo "Starting Rhidmo reproducible build"

  # Pull Docker image not only be tag name, but also by immutable identifier (digest)
  # to have a build environment that is guaranteed NEVER to change over time.
  # See https://docs.docker.com/reference/cli/docker/image/pull/#pull-an-image-by-digest-immutable-identifier
  docker run -it --rm --name rhidmo-reproducible-build \
    -v "$(pwd)":/usr/src/mymaven \
    -w /usr/src/mymaven \
    "$gDockerImageTag"@"$gDockerImageDigest" \
    sh -c "java -version && mvn --version && mvn ${gMavenArgs[*]}"
  rc=$?

  if ((rc != 0)); then
    msgErr "Rhidmo reproducible build failed with RC=$rc"
    return 1
  fi

  # Display SHA-256 on build only - cleanup has no output to digest
  if ((gOptionClean == 0)); then
    displayDigest || return 1
  fi
}

function main() {
  local -i rc

  parseArgs "$@"
  rc=$?

  if ((rc != 0)); then
    usage
    return $rc
  fi

  if ((gOptionHelp == 0)); then

    setupDescriptorsForQuietMode || return 1

    checkPrerequisites || return 1

    pushd "$gScriptDir" > /dev/null || return 1

    gMavenArgs+=("clean")
    if ((gOptionClean == 0)); then
      gMavenArgs+=("package")
    fi

    doBuild "$@"
    rc=$?

    popd > /dev/null || :
    return $rc

  else
    usage
    return 0
  fi
}

main "$@"
