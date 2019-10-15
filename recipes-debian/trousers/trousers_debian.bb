# base recipe: meta-security/meta-tpm/recipes-tpm/trousers/trousers_git.bb
# base branch: warrior

SUMMARY = "TrouSerS - An open-source TCG Software Stack implementation."
LICENSE = "BSD"
HOMEPAGE = "http://sourceforge.net/projects/trousers/"
LIC_FILES_CHKSUM = "file://README;startline=3;endline=4;md5=2af28fbed0832e4d83a9e6dd68bb4413"
SECTION = "security/tpm"

inherit debian-package
require recipes-debian/sources/trousers.inc

DEPENDS = "openssl"

SRC_URI += " \
    	file://trousers.init.sh \
    	file://trousers-udev.rules \
    	file://tcsd.service \
        file://get-user-ps-path-use-POSIX-getpwent-instead-of-getpwe.patch \
        file://0001-build-don-t-override-localstatedir-mandir-sysconfdir.patch \
    	"

inherit autotools pkgconfig useradd update-rc.d ${@bb.utils.contains('VIRTUAL-RUNTIME_init_manager','systemd','systemd','', d)}

PACKAGECONFIG ?= "gmp "
PACKAGECONFIG[gmp] = "--with-gmp, --with-gmp=no, gmp"
PACKAGECONFIG[gtk] = "--with-gui=gtk, --with-gui=none, gtk+"

do_debian_patch_prepend () {
	rm -rf .pc
}

do_install () {
	oe_runmake DESTDIR=${D} install
}

do_install_append() {
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/trousers.init.sh ${D}${sysconfdir}/init.d/trousers
	install -d ${D}${sysconfdir}/udev/rules.d
	install -m 0644 ${WORKDIR}/trousers-udev.rules ${D}${sysconfdir}/udev/rules.d/45-trousers.rules

	if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${WORKDIR}/tcsd.service ${D}${systemd_unitdir}/system/
		sed -i -e 's#@SBINDIR@#${sbindir}#g' ${D}${systemd_unitdir}/system/tcsd.service
	fi        
}

CONFFILES_${PN} += "${sysconfig}/tcsd.conf"

PACKAGES = " \
	libtspi \
	libtspi-dbg \
	libtspi-dev \
	libtspi-doc \
	libtspi-staticdev \
	trousers \
	trousers-dbg \
	trousers-doc \
	"

# libtspi needs tcsd for most (all?) operations, so suggest to
# install that.
RRECOMMENDS_libtspi = "${PN}"

FILES_libtspi = " \
	${libdir}/*${SOLIBS} \
	"
FILES_libtspi-dbg = " \
	${libdir}/.debug \
	"
FILES_libtspi-dev = " \
	${includedir} \
	${libdir}/*.so \
	"
FILES_libtspi-doc = " \
	${mandir}/man3 \
	"
FILES_libtspi-staticdev = " \
	${libdir}/*.la \
	${libdir}/*.a \
	"
FILES_${PN} = " \
	${sbindir}/tcsd \
	${sysconfdir} \
	${localstatedir} \
	"

FILES_${PN}-dev += "${libdir}/trousers"

FILES_${PN}-dbg = " \
	${sbindir}/.debug \
	"
FILES_${PN}-doc = " \
	${mandir}/man5 \
	${mandir}/man8 \
	"

INITSCRIPT_NAME = "trousers"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 19 0 1 6 ."

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "--system tss"
USERADD_PARAM_${PN} = "--system -M -d /var/lib/tpm -s /bin/false -g tss tss"

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "tcsd.service"
SYSTEMD_AUTO_ENABLE = "disable"

BBCLASSEXTEND = "native"

