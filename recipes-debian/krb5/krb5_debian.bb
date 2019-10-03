#
# base recipe: http://cgit.openembedded.org/meta-openembedded/tree/meta-oe/recipes-connectivity/krb5/krb5_1.17.bb
# base branch: warrior
#

SUMMARY = "A network authentication protocol"
DESCRIPTION = "Kerberos is a system for authenticating users and services on a network.\n\
 Kerberos is a trusted third-party service.  That means that there is a\n\
 third party (the Kerberos server) that is trusted by all the entities on\n\
 the network (users and services, usually called "principals").\n\
 .\n\
 This is the MIT reference implementation of Kerberos V5."
HOMEPAGE = "http://web.mit.edu/kerberos/"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../NOTICE;md5=aff541e7261f1926ac6a2a9a7bbab839"

inherit debian-package
require recipes-debian/sources/krb5.inc

inherit autotools-brokensep perlnative systemd

S = "${DEBIAN_UNPACK_DIR}/src/"

DEPENDS += "bison-native ncurses e2fsprogs e2fsprogs-native openssl"

do_configure() {
	gnu-configize --force
	autoreconf
	oe_runconf
}

CACHED_CONFIGUREVARS += " \
    krb5_cv_attr_constructor_destructor=yes ac_cv_func_regcomp=yes \
    ac_cv_printf_positional=yes"

CFLAGS_append = " -fPIC -DDESTRUCTOR_ATTR_WORKS=1 -I${STAGING_INCDIR}/et "                                                                 
LDFLAGS_append = " -lpthread "

EXTRA_OECONF += " \
    --localstatedir=${sysconfdir} \
    --with-system-et --with-system-ss \
    --disable-rpath --enable-shared --without-tcl \
"

PACKAGECONFIG ??= ""
PACKAGECONFIG[ldap] = "--with-ldap,--without-ldap,libldap"
PACKAGECONFIG[verto] = "--with-system-verto,--without-system-verto,libverto"

do_install_append() {
	install -d ${D}${sysconfdir}/init.d/ \
	           ${D}${systemd_system_unitdir}
	install -m 0755 ${S}/../debian/krb5-admin-server.init \
	                ${D}${sysconfdir}/init.d/krb5-admin-server
	install -m 0644 ${S}/../debian/krb5-admin-server.service \
	                ${D}${systemd_system_unitdir}/
	install -m 0755 ${S}/../debian/krb5_newrealm ${D}${sbindir}/

	# base on debian/krb5-gss-samples.install
	mv ${D}${sbindir}/gss-server ${D}${bindir}/gss-server

	install -m 0755 ${S}/../debian/krb5-kdc.init ${D}${sysconfdir}/init.d/krb5-kdc
	install -m 0644 ${S}/../debian/krb5-kdc.service ${D}${systemd_system_unitdir}/

	install -d ${D}${datadir}/krb5-kdc ${D}${docdir}/krb5-kdc/examples                                                                 
	install -m 0644 ${S}/../debian/kdc.conf \
	            ${D}${datadir}/krb5-kdc/kdc.conf.template
	ln -sf ${datadir}/krb5-kdc/kdc.conf.template \
	            ${D}${docdir}/krb5-kdc/examples/kdc.conf

	install -d ${D}${sysconfdir}/insserv/overrides/ \
	           ${D}${systemd_system_unitdir}/krb5-admin-server.service.d/ \
	           ${D}${systemd_system_unitdir}/krb5-kdc.service.d/
	install -m 0644 ${S}/../debian/krb5-kdc-ldap.insserv-override \
	                ${D}${sysconfdir}/insserv/overrides/krb5-kdc
	install -m 0644 ${S}/../debian/slapd-before-kdc.conf \
	                ${D}${systemd_system_unitdir}/krb5-admin-server.service.d/                    
	install -m 0644 ${S}/../debian/slapd-before-kdc.conf \
	                ${D}${systemd_system_unitdir}/krb5-kdc.service.d/

	install -m 0755 ${S}/../debian/krb5-kpropd.init \
	                ${D}${sysconfdir}/init.d/krb5-kpropd
	install -m 0644 ${S}/../debian/krb5-kpropd.service \
	                ${D}${systemd_system_unitdir}/

	# base on debian/rules & debian/libkrb5-dev.links
	cp -p ${D}${bindir}/krb5-config ${D}${bindir}/krb5-config.mit
	ln -sf krb5-config.mit ${D}${bindir}/krb5-config

	install -d ${D}${libdir}/tmpfiles.d
	install -m 0644 ${S}/../debian/krb5-otp.tmpfile \
	                ${D}${libdir}/tmpfiles.d/krb5-otp.conf

	# base on debian/krb5-multidev.install.in
	install -d ${D}${includedir}/mit-krb5 \
	           ${D}${libdir}/mit-krb5 \
	           ${D}${libdir}/pkgconfig/mit-krb5/
	for file in gssapi gssapi.h gssrpc kadm5 kdb.h krb5 krb5.h profile.h; do
		mv ${D}${includedir}/$file ${D}${includedir}/mit-krb5
	done
	(cd ${D}${includedir}/mit-krb5/ && find . -type d -print) \
	    | (cd ${D}${includedir}/ && xargs mkdir -p)
	(cd ${D}${includedir}/mit-krb5 && find . -type f -print) \
	    | (cd ${D}${includedir}/ && xargs -I+ ln -s mit-krb5/+ +)

	for file in libgssapi_krb5.so libk5crypto.so libkadm5clnt.so libkadm5srv.so \
	            libkrb5.so libgssrpc.so libkadm5clnt_mit.so libkadm5srv_mit.so \
	            libkdb5.so libkrb5support.so; do
		LINKLIB=$(basename $(readlink ${D}${libdir}/$file))
		ln -s ../$LINKLIB ${D}${libdir}/mit-krb5/$file
		ln -sf mit-krb5/$file ${D}${libdir}/$file
	done

	for file in gssrpc.pc kadm-client.pc kadm-server.pc kdb.pc krb5-gssapi.pc krb5.pc; do
		cp -p ${D}${libdir}/pkgconfig/$file \
		      ${D}${libdir}/pkgconfig/mit-krb5/
		ln -sf mit-krb5/$file ${D}${libdir}/pkgconfig/$file
	done
	# base on debian/krb5-kdc.dirs.in
	install -d ${D}${localstatedir}/lib/krb5kdc \
	           ${D}${sysconfdir}/krb5kdc
	# base on debian/libgssapi-krb5-2.dirs
	install -d ${D}${sysconfdir}/gss/mech.d
	# base on debian/libkrb5-3.dirs.in
	install -d ${D}${libdir}/krb5/plugins/krb5

	chmod 700 ${D}${localstatedir}/lib/krb5kdc
	chmod 700 ${D}${sysconfdir}/krb5kdc

	# remove unneeded directories/files
	rm -rf ${D}${datadir}/examples \
	       ${D}${bindir}/sclient \
	       ${D}${bindir}/sim_client \
	       ${D}${bindir}/uuclient \
	       ${D}${libdir}/krb5/plugins/preauth/test.so \
	       ${D}${libdir}/krb5/plugins/authdata/ \
	       ${D}${libdir}/krb5/plugins/libkrb5/ \
	       ${D}${sbindir}/krb5-send-pr \
	       ${D}${sbindir}/sim_server \
	       ${D}${sbindir}/sserver \
	       ${D}${sbindir}/uuserver \
	       ${D}${localstatedir}/run     
}

PACKAGES =+ "${PN}-admin-server ${PN}-gss-samples ${PN}-kdc ${PN}-k5tls \
             ${PN}-kdc-ldap ${PN}-kpropd ${PN}-multidev ${PN}-otp ${PN}-pkinit \
             ${PN}-user libgssapi-krb5 libgssrpc libk5crypto libkadm5clnt-mit \
             libkadm5srv-mit libkdb5 libkrad-dev libkrad libkrb5 libkrb5support \
             ${@bb.utils.contains('PACKAGECONFIG', 'verto', '', 'libverto', d)}"

FILES_${PN}-admin-server = "\
    ${sysconfdir}/init.d/krb5-admin-server \
    ${systemd_system_unitdir}/krb5-admin-server.service \
    ${sbindir}/kadmin.local \
    ${sbindir}/kadmind \
    ${sbindir}/kprop \
    ${sbindir}/krb5_newrealm \
    "
FILES_${PN}-gss-samples = "\
    ${bindir}/gss-client \
    ${bindir}/gss-server \
    "
FILES_${PN}-kdc = "\
    ${sysconfdir}/init.d/krb5-kdc \
    ${systemd_system_unitdir}/krb5-kdc.service \
    ${libdir}/krb5/plugins/kdb/db2.so \
    ${sbindir}/kdb5_util \
    ${sbindir}/kproplog \
    ${sbindir}/krb5kdc \
    ${datadir}/krb5-kdc/kdc.conf.template \
    ${localstatedir}/* \
    ${sysconfdir}/krb5kdc \
    "
FILES_${PN}-k5tls = "\
    ${libdir}/krb5/plugins/tls/k5tls.so \
    "
FILES_${PN}-kdc-ldap = "\
    ${sysconfdir}/insserv/overrides/krb5-kdc \
    ${systemd_system_unitdir}/krb5-admin-server.service.d/slapd-before-kdc.conf \
    ${systemd_system_unitdir}/krb5-kdc.service.d/slapd-before-kdc.conf \
    ${libdir}/krb5/libkdb_ldap${SOLIBS} \
    ${libdir}/krb5/plugins/kdb/kldap.so \
    ${sbindir}/kdb5_ldap_util \
    "
FILES_${PN}-kpropd = "\
    ${sysconfdir}/init.d/krb5-kpropd \
    ${systemd_system_unitdir}/krb5-kpropd.service \
    ${sbindir}/kpropd \
    "
FILES_${PN}-multidev = "\
    ${bindir}/krb5-config.mit \
    ${includedir}/mit-krb5/* \
    ${libdir}/mit-krb5/* \
    ${libdir}/pkgconfig/mit-krb5* \
    "
FILES_${PN}-otp = "\
    ${libdir}/tmpfiles.d/krb5-otp.conf \
    ${libdir}/krb5/plugins/preauth/otp.so \
    "
FILES_${PN}-pkinit = "\
    ${libdir}/krb5/plugins/preauth/pkinit.so \
"
FILES_${PN}-user = "\
    ${bindir}/k5srvutil \
    ${bindir}/kadmin \
    ${bindir}/kdestroy \
    ${bindir}/kinit \
    ${bindir}/klist \
    ${bindir}/kpasswd \
    ${bindir}/ksu \
    ${bindir}/kswitch \
    ${bindir}/ktutil \
    ${bindir}/kvno \
"
FILES_libgssapi-krb5 = "\
    ${libdir}/libgssapi_krb5${SOLIBS} \
    ${sysconfdir}/gss/mech.d \
"
FILES_libgssrpc = "\
    ${libdir}/libgssrpc${SOLIBS} \
"
FILES_libk5crypto = "\
    ${libdir}/libk5crypto${SOLIBS} \
"
FILES_libkadm5clnt-mit = "\
    ${libdir}/libkadm5clnt_mit${SOLIBS} \
"
FILES_libkadm5srv-mit = "\
    ${libdir}/libkadm5srv_mit${SOLIBS} \
"
FILES_libkdb5 = "\
    ${libdir}/libkdb5${SOLIBS} \
"
FILES_libkrad-dev = "\
    ${includedir}/krad.h \
    ${libdir}/libkrad.so \
"
FILES_libkrad = "\
    ${libdir}/libkrad${SOLIBS} \
"
FILES_libkrb5 = "\
    ${libdir}/krb5/plugins/preauth/spake.so \
    ${libdir}/krb5/plugins/krb5 \
    ${libdir}/libkrb5${SOLIBS} \
"
FILES_${PN}-dev = "\
    ${bindir}/krb5-config \
    ${includedir}/* \
    ${libdir}/*${SOLIBSDEV} \
    ${libdir}/pkgconfig/* \
"
FILES_libkrb5support = "\
    ${libdir}/libkrb5support${SOLIBS} \
"
FILES_libverto = "\
    ${libdir}/libverto${SOLIBS} \
"

DEBIANNAME_${PN}-dev = "libkrb5-dev"
INSANE_SKIP_${PN}-multidev = "dev-so dev-deps"

RDEPENDS_${PN}-multidev += "${PN}-dev"
RDEPENDS_${PN}-kdc += "${PN}-user"
RDEPENDS_${PN}-admin-server += "${PN}-kdc"

SYSTEMD_PACKAGES = "${PN}-admin-server ${PN}-kdc"
SYSTEMD_SERVICE_${PN}-admin-server = "krb5-admin-server.service"
SYSTEMD_SERVICE_${PN}-kdc = "krb5-kdc.service"

# base on debian/krb5-kdc.postinst
pkg_postinst_${PN}-kdc () {
    KRB5LD_DEFAULT_REALM="EXAMPLE.COM"
    # Try to get default realm from /etc/krb5.conf
    if [ -f $D${sysconfdir}/krb5.conf ]; then
        KRB5LD_DEFAULT_REALM=$(grep "^\s*default_realm\s*=" $D${sysconfdir}/krb5.conf | cut -d= -f2 | xargs)
    fi

    if [ ! -f "$D${sysconfdir}/krb5kdc/kdc.conf" ]; then
        sed -e "s/@MYREALM/$KRB5LD_DEFAULT_REALM/" \
            $D${datadir}/krb5-kdc/kdc.conf.template > $D${sysconfdir}/krb5kdc/kdc.conf
    fi

    if [ ! -d "$D${sysconfdir}/default" ]; then
        mkdir $D${sysconfdir}/default
    fi

    if [ -f "$D${sysconfdir}/default/krb5-kdc" ] ; then
            . $D${sysconfdir}/default/krb5-kdc
    fi
    cat <<'EOF' > $D${sysconfdir}/default/krb5-kdc

# Automatically generated.  Only the value of DAEMON_ARGS will be preserved.
# If you change anything in this file other than DAEMON_ARGS, first run
# dpkg-reconfigure krb5-kdc and disable managing the KDC configuration with
# debconf.  Otherwise, changes will be overwritten.

EOF
    if [ -n "$DAEMON_ARGS" ] ; then
        echo "DAEMON_ARGS=\"$DAEMON_ARGS\"" >> $D${sysconfdir}/default/krb5-kdc
    fi
}
