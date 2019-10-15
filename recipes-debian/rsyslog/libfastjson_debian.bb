# base recipe: meta-openembedded/meta-oe/recipes-extended/rsyslog/libfastjson_0.99.8.bb
# base branch: warrior

SUMMARY = "A fork of json-c library"
HOMEPAGE = "https://github.com/rsyslog/libfastjson"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=a958bb07122368f3e1d9b2efe07d231f"

DEBIAN_QUILT_PATCHES = ""

inherit debian-package
require recipes-debian/sources/libfastjson.inc

SRC_URI += "file://0001-fix-jump-misses-init-gcc-8-warning.patch"

inherit autotools
