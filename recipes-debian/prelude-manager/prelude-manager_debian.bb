SUMMARY = "Security Information and Events Management System"
DESCRIPTION = "Prelude-Manager is a high availability server that accepts secured connections \
			   from distributed sensors or other managers and saves received events to a media \
			   specified by the user (database, logfile, mail, etc)."
HOMEPAGE = "https://www.prelude-siem.org/"
LICENSE = "GPLv2+ & LGPL-2.1+ & MIT & GPLv3+"

LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
					file://libmissing/float.c;beginline=2;endline=16;md5=ac398b98ab705f792d9b261a4d6d59fd \
					file://install-sh;beginline=6;endline=27;md5=eb067d0ae3938e8e21c21d62c608fdfd \
					file://libmissing/tests/fpucw.h;beginline=1;endline=16;md5=a6bfa02cb1fad5b21adaa0d55d9fd81e \
					"

inherit debian-package
require recipes-debian/sources/prelude-manager.inc

DEPENDS = "libprelude gnutls libpreludedb"

inherit autotools-brokensep gettext pkgconfig useradd

EXTRA_OECONF += "--with-libev=embedded"

EXTRA_AUTORECONF += "-I ${S}/libmissing/m4"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "--system --no-create-home \
                       --home ${localstatedir}/run/prelude-manager \
                       --user-group prelude"

do_install_append () {
	install -dm644 ${D}${datadir}/prelude-manager/
	mv ${D}${sysconfdir}/prelude-manager/prelude-manager.conf ${D}${datadir}/prelude-manager/
	rmdir ${D}${localstatedir}/run/prelude-manager
	rmdir ${D}${localstatedir}/run
	install -dm644 ${D}${localstatedir}/spool/prelude/prelude-manager
}

pkg_postinst_${PN} () {
	cp $D${datadir}/prelude-manager/prelude-manager.conf $D${sysconfdir}/prelude-manager/prelude-manager.conf
	chmod 640 $D${sysconfdir}/prelude-manager/prelude-manager.conf
	chown prelude $D${sysconfdir}/prelude-manager/prelude-manager.conf

	if [ -d "$D${localstatedir}/spool/prelude-manager" ]; then
		chown prelude:prelude $D${localstatedir}/spool/prelude-manager
		if [ -d "$D${localstatedir}/spool/prelude-manager/failover" ]; then
			chown -R prelude:prelude $D${localstatedir}/spool/prelude-manager/failover
		fi
		if [ -d "$D${localstatedir}/spool/prelude-manager/scheduler" ]; then
			chown -R prelude:prelude $D${localstatedir}/spool/prelude-manager/scheduler
		fi
	fi
}