# base recipe: meta-openembedded/meta-oe/recipes-support/libestr/libestr_0.1.11.bb
# base branch: warrior

SUMMARY = "some essentials for string handling (and a bit more)"
HOMEPAGE = "http://libestr.adiscon.com/"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=9d6c993486c18262afba4ca5bcb894d0"

DEBIAN_QUILT_PATCHES = ""

inherit debian-package
require recipes-debian/sources/libestr.inc

inherit autotools
