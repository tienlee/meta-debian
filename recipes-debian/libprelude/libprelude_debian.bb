LICENSE = "GPLv2+ & LGPL-2.1+ & MIT & GPLv3+"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
		    file://libmissing/sleep.c;beginline=2;endline=16;md5=1cbcbcc6446388bec2983f9ea1edc055 \
		    file://install-sh;beginline=6;endline=27;md5=eb067d0ae3938e8e21c21d62c608fdfd \
		    file://m4/ax_check_aligned_access_required.m4;beginline=24;endline=50;md5=d734876954fbde01b1d67688e2ec478f \
		    "

inherit debian-package
require recipes-debian/sources/libprelude.inc

DEPENDS = "gnutls libtool perl"

SRC_URI += " \
    file://libprelude-fix-uid-gid-conflicting-types.patch \
    file://libprelude-fix-generate-perl-makefile.patch \
"

inherit autotools pkgconfig cpan-base distutils-base perlnative

EXTRA_AUTORECONF += "-I ${S}/libmissing/m4"

PACKAGECONFIG ?= "python2"
PACKAGECONFIG[python2] = "--with-python2,--without-python2,"
PACKAGECONFIG[python3] = "--with-python3,--without-python3,"
PACKAGECONFIG[lua] = "--with-lua,--without-lua,"
PACKAGECONFIG[ruby] = "--with-ruby,--without-ruby,"

do_configure_prepend() {
	perl_version=${@get_perl_version(d)}
	short_perl_version=`echo ${perl_version%.*}`
	. ${STAGING_LIBDIR}${PERL_OWN_DIR}/perl5/config.sh
	sed -i -e "s:##EXTRA_CPANFLAGS##:${EXTRA_CPANFLAGS}:" \
	       -e "s:##CC##:${cc}:" \
	       -e "s:##LD##:${ld}:" \
	       -e "s:##LDFLAGS##:${ldflags}:" \
	       -e "s:##CCFLAGS##:${ccflags}:" \
	       -e "s:##LDDLFLAGS##:${lddlflags}:" \
	       -e "s:##TMP##:SITELIBEXP=${sitelibexp} SITEARCHEXP=${sitearchexp} \
	                     INSTALLVENDORLIB=${D}${datadir}/perl5 \
	                     INSTALLVENDORARCH=${D}${libdir}/perl5 \
	                     INSTALLSITELIB=${libdir}/perl5/$short_perl_version \
	                     INSTALLSITEARCH=${libdir}/perl5/$short_perl_version \
	                     :" \
	       ${S}/bindings/Makefile.am
}

PACKAGES =+ "${PN}-perl libpreludecpp prelude-utils python-prelude"

FILES_${PN}-dev += "${bindir}/${PN}-config"
FILES_${PN}-perl = "${libdir}/perl*/*"
FILES_libpreludecpp = "${libdir}/libpreludecpp${SOLIBS}"
FILES_prelude-utils = "${bindir}/prelude-admin ${sysconfdir}/prelude/default/*.conf"
FILES_python-prelude = "${libdir}/python*/*"

BBCLASSEXTEND = "native"
