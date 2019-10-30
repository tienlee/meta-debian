SUMMARY = "Library Security Information and Events Management System"
DESCRIPTION = "The PreludeDB Library provides an abstraction layer upon \
			   the type and the format of the database used to store IDMEF \
			   alerts. It allows developers to use the Prelude IDMEF database \
			   easily and efficiently without worrying about SQL, and to access \
			   the database independently of the type/format of the database."
HOMEPAGE = "https://www.prelude-siem.org/"
LICENSE = "GPLv2+ & LGPL-2.1+ & MIT & GPLv3+"

LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
					file://libmissing/float.c;beginline=2;endline=16;md5=e11d33f4ea7850c9753b3138a3877fa6 \
					file://install-sh;beginline=6;endline=27;md5=eb067d0ae3938e8e21c21d62c608fdfd \
					file://libmissing/tests/fpucw.h;beginline=1;endline=16;md5=a6bfa02cb1fad5b21adaa0d55d9fd81e \
					"

inherit debian-package
require recipes-debian/sources/libpreludedb.inc

DEPENDS = "libprelude gnutls libtool sqlite3"

inherit autotools pkgconfig distutils-base

SRC_URI += "file://libpreludedb-fix-generate-python2-makefile.patch"

EXTRA_OECONF += "--with-mysql=no \
				 --with-postgresql=no \
				 --with-sqlite3=${STAGING_INCDIR}/.. \
				 --enable-easy-bindings"

EXTRA_AUTORECONF += "-I ${S}/libmissing/m4"

PACKAGECONFIG ?= "python2"
PACKAGECONFIG[python2] = "--with-python2,--without-python2,"
PACKAGECONFIG[python3] = "--with-python3,--without-python3,"

do_configure_prepend() {
	sed -i -e "s|##PYTHON_SITEPACKAGES_DIR##|${PYTHON_SITEPACKAGES_DIR}|" ${S}/bindings/python/Makefile.am
}