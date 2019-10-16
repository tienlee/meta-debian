SUMMARY = "Network Time Protocol daemon and utility programs"
DESCRIPTION = "NTP, the Network Time Protocol, is used to keep computer clocks \
accurate by synchronizing them over the Internet or a local network,\
or by following an accurate hardware receiver that interprets GPS, \
DCF-77, or similar time signals."

inherit debian-package
require recipes-debian/sources/ntpsec.inc

LICENSE = "NTP & BSD-2-Clause & BSD-3-Clause & MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=943cc2e062ab52a1442248ecf4312663"

DEBIAN_UNPACK_DIR = "${WORKDIR}/${BPN}-${REPACK_PV}"

DEPENDS += "bison-native openssl python3"

inherit waf python3native

EXTRA_OECONF = " \
	--cross-compiler="${CC}" \
	--cross-cflags="${CFLAGS} -g" --cross-ldflags="${LDFLAGS}" \
	--enable-debug-gdb \
	CC="${BUILD_CC}" \
"

do_configure() {
	(cd ${S} && python3 ./waf configure  --prefix=${prefix} ${WAF_EXTRA_CONF} ${EXTRA_OECONF})
}

do_compile()  {
	(cd ${S} && python3 ./waf build ${@oe.utils.parallel_make_argument(d, '-j%d', limit=64)})
}

do_install() {
	(cd ${S} && python3 ./waf install --destdir=${D})

	# Move file base on debian package structure
	mv ${D}${bindir}/ntploggps ${D}${sbindir}/ntploggps
}

PACKAGES =+ "ntpsec-ntpviz python3-ntp"

FILES_ntpsec-ntpviz += "${bindir}/ntpviz ${sbindir}/ntploggps ${sbindir}/ntplogtemp"
FILES_python3-ntp += "${libdir}/python3/dist-packages/*"

RDEPENDS_${PN} += "glibc-binaries python3-core python3-ntp"
RDEPENDS_ntpsec-ntpviz += "glibc-binaries python3-core python"
RDEPENDS_python3-ntp += "python3-core"
