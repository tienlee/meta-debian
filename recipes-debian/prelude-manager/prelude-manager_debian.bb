SUMMARY = "Security Information and Events Management System"
DESCRIPTION = "Prelude-Manager is a high availability server that accepts secured connections \
			   from distributed sensors or other managers and saves received events to a media \
			   specified by the user (database, logfile, mail, etc)."
HOMEPAGE = "https://www.prelude-siem.org/"
LICENSE = "GPLv2+ & LGPL-2.1+ & MIT & GPLv3+"

LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
					file://libmissing/float.c;beginline=2;endline=16;md5=ac398b98ab705f792d9b261a4d6d59fd \
					file://install-sh;beginline=6;endline=27;md5=eb067d0ae3938e8e21c21d62c608fdfd \
					file://config.sub;beginline=7;endline=25;md5=f92d7033ace1653856cc3084ddc33960 \
					"

inherit debian-package
require recipes-debian/sources/prelude-manager.inc

DEPENDS = "libprelude-native gnutls libpreludedb"

inherit autotools-brokensep gettext pkgconfig

EXTRA_OECONF += "--with-libev=embedded"

EXTRA_AUTORECONF += "-I ${S}/libmissing/m4"

do_install_append () {
	install -dm644 ${D}${datadir}/prelude-manager/
	mv ${D}${sysconfdir}/prelude-manager/prelude-manager.conf ${D}${datadir}/prelude-manager/
	rmdir ${D}${localstatedir}/run/prelude-manager
	rmdir ${D}${localstatedir}/run
	install -dm644 ${D}${localstatedir}/spool/prelude/prelude-manager
}
