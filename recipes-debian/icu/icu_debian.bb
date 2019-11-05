require recipes-support/icu/icu.inc

LIC_FILES_CHKSUM = "file://../LICENSE;md5=63752c57bd0b365c9af9f427ef79c819"

inherit debian-package
require recipes-debian/sources/icu.inc

S = "${DEBIAN_UNPACK_DIR}/source"
SPDX_S = "${DEBIAN_UNPACK_DIR}"
