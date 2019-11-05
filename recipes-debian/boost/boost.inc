HOMEPAGE = "https://www.boost.org/"

LICENSE = "BSL-1.0 & Python-2.0"
LIC_FILES_CHKSUM = " \
    file://LICENSE_1_0.txt;md5=e4224ccaecb14d942c71d31bef20d78c \
    file://libs/python/include/boost/python/detail/python22_fixed.h;endline=7;md5=0587f6a1c767a32bb78a303e4add886c \
"

inherit debian-package
require recipes-debian/sources/boost1.67.inc
DEBIAN_UNPACK_DIR = "${WORKDIR}/boost"
