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

gScriptDir="$(readlink -f "$(dirname "$0")")"
gDockerImageTag=maven:3.9.11-eclipse-temurin-8
gDockerImageDigest=sha256:8135a3d9d2247f75973a23c984baf0b0b758eed1a85491d78fd4340a9cb35f76
gDockerGroup=docker

function msgErr() {
  >&2 printf "[\e[1;31mERROR\e[0m]: %s\n" "$@"
}

function msgWarn() {
  >&2 printf "[\e[1;33mWARNING\e[0m]: %s\n" "$@"
}

function msgInfo() {
  >&2 printf "[\e[1;32mINFO\e[0m]: %s\n" "$@"
}

function doBuild() {
  local -i rc
  local checksum
  local zipfile

  msgInfo "Starting Rhidmo reproducible build"

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

  # Pull Docker image not only be tag name, but also by immutable identifier (digest)
  # to have a build environment that is guaranteed NEVER to change over time.
  # See https://docs.docker.com/reference/cli/docker/image/pull/#pull-an-image-by-digest-immutable-identifier
  docker run -it --rm --name rhidmo-reproducible-build \
    -v "$(pwd)":/usr/src/mymaven \
    -w /usr/src/mymaven \
    "$gDockerImageTag"@"$gDockerImageDigest" \
    sh -c "java -version && mvn --version && mvn clean package"
  rc=$?

  if ((rc != 0)); then
    msgErr "Rhidmo reproducible build failed with RC=$rc; can't calculate SHA-256 sum"
    return 1
  fi

  zipfile="$(find "$gScriptDir"/ear/target -name rhidmo\*zip)"
  rc=$?

  if [[ "$rc" != "0" || ! -f "$zipfile" ]]; then
    msgErr "Rhidmo ZIP archive not found after build, RC=$rc"
    return 1
  fi

  # Use SHA-256 (not SHA-512) because that's what GitHub releases UI shows as of mid 2025
  checksum="$(sha256sum "$zipfile" | cut -d' ' -f1)"
  rc=$?

  if ((rc != 0)); then
    msgErr "Rhidmo reproducible build successful, but calculating SHA-256 sum failed with RC=$rc"
    return 1
  fi

  msgInfo "Reproducible SHA-256 sum of Rhidmo ZIP archive:" \
    "$checksum"

}

function main() {
  local -i rc
  pushd "$gScriptDir" > /dev/null || return 1
  doBuild "$@"
  rc=$?
  popd > /dev/null || :
  return $rc
}

main "$@"
