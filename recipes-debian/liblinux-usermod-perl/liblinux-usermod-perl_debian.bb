LICENSE = "Artistic-1.0 | GPLv1+"
LIC_FILES_CHKSUM = "file://README;beginline=164;endline=169;md5=e64cb91ad12450cd2e32695065647805"

inherit debian-package
require recipes-debian/sources/liblinux-usermod-perl.inc

DEBIAN_UNPACK_DIR = "${WORKDIR}/Linux-usermod-${PV}"

inherit cpan