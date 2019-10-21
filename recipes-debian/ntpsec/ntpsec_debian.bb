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

inherit waf python3native useradd

EXTRA_OECONF += " \
    --cross-compiler="${CC}" \
    --cross-cflags="${CFLAGS} -g" --cross-ldflags="${LDFLAGS}" \
    --enable-debug-gdb --enable-mssntp --enable-warnings \
    --refclock=local,spectracom,generic,arbiter,nmea,pps,hpgps,shm,trimble,jjy,zyfer,neoclock \
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

	# According to debian/rules
	rm -rf ${D}${libdir}/python3/dist-packages/ntp/__pycache__
	mv ${D}${bindir}/ntptime \
	   ${D}${bindir}/ntpkeygen \
	   ${D}${bindir}/ntploggps \
	   ${D}${bindir}/ntplogtemp \
	   ${D}${bindir}/ntpleapfetch \
	   ${D}${sbindir}/
	chown -R root:root ${D}${sbindir}
	install -m 755 ${S}/ntpclients/ntpmon.py ${D}${bindir}/ntpmon
	install -m 755 \
	        ${S}/ntpclients/ntptrace.py \
	        ${D}${bindir}/ntptrace
	install -m 755 \
	        ${S}/attic/ntpdate \
	        ${D}${sbindir}/
	install -m 755 \
	        ${S}/ntpclients/ntpwait.py \
	        ${D}${sbindir}/ntpwait

	install -d ${D}${systemd_system_unitdir}
	install -m 644 \
	        ${S}/etc/ntpd.service \
	        ${D}${systemd_system_unitdir}/ntpsec.service
	install -m 644 \
	        ${S}/etc/ntp-wait.service \
	        ${D}${systemd_system_unitdir}/ntpsec-wait.service
	install -m 644 \
	        ${S}/debian/ntpsec.ntpsec-rotate-stats.service \
	        ${D}${systemd_system_unitdir}/ntpsec-rotate-stats.service
	install -m 644 \
	        ${S}/debian/ntpsec.ntpsec-rotate-stats.timer \
	        ${D}${systemd_system_unitdir}/ntpsec-rotate-stats.timer

	install -m 644 \
	        ${S}/etc/ntploggps.service \
	        ${S}/etc/ntploggps.timer \
	        ${S}/etc/ntplogtemp.service \
	        ${S}/etc/ntplogtemp.timer \
	        ${S}/etc/ntpviz-daily.service \
	        ${S}/etc/ntpviz-daily.timer \
	        ${S}/etc/ntpviz-weekly.service \
	        ${S}/etc/ntpviz-weekly.timer \
	        ${D}${systemd_system_unitdir}/

	install -d ${D}${sysconfdir}/ntpviz/day
	install -m 644 ${S}/www/day/footer \
	        ${D}${sysconfdir}/ntpviz/day/footer.html
	install -m 644 ${S}/www/day/header \
	        ${D}${sysconfdir}/ntpviz/day/header.html
	install -m 644 ${S}/www/index.html \
	        ${D}${sysconfdir}/ntpviz/index.html
	install -m 644 ${S}/debian/ntpviz.options \
	        ${D}${sysconfdir}/ntpviz/options
	install -d ${D}${sysconfdir}/ntpviz/week
	install -m 644 ${S}/www/week/footer \
	        ${D}${sysconfdir}/ntpviz/week/footer.html
	install -m 644 ${S}/www/week/header \
	        ${D}${sysconfdir}/ntpviz/week/header.html

	install -d ${D}${datadir}/ntpsec-ntpviz/ntpviz
	install -m 644 \
	        ${S}/www/favicon.ico \
	        ${S}/www/ntpsec-logo.png \
	        ${D}${datadir}/ntpsec-ntpviz/ntpviz

	install -D -m 0755 ${S}/debian/ntpdate-debian \
	        ${D}${sbindir}/ntpdate-debian
	install -D -m 0644 ${S}/debian/ntpsec.dhcp \
	        ${D}${sysconfdir}/dhcp/dhclient-exit-hooks.d/ntpsec
	install -D -m 0755 ${S}/debian/ntpsec.networkmanager \
	        ${D}${sysconfdir}/NetworkManager/dispatcher.d/ntpsec
	install -D -m 0644 ${S}/debian/ntpsec-ntpdate.dhcp \
	        ${D}${sysconfdir}/dhcp/dhclient-exit-hooks.d/ntpsec-ntpdate
	install -D -m 0755 ${S}/debian/ntpsec-ntpdate.networkmanager \
	        ${D}${sysconfdir}/NetworkManager/dispatcher.d/ntpsec-ntpdate
	install -D -m 0644 ${S}/debian/ntpsec-systemd-netif.path \
	        ${D}${systemd_system_unitdir}/ntpsec-systemd-netif.path
	install -D -m 0644 ${S}/debian/ntpsec-systemd-netif.service \
	        ${D}${systemd_system_unitdir}/ntpsec-systemd-netif.service
	install -D -m 0644 ${S}/debian/ntp.conf ${D}${sysconfdir}/ntpsec/ntp.conf
	install -D -m 0644 ${S}/debian/ntpviz.conf \
	        ${D}${sysconfdir}/ntpsec/ntp.d/ntpviz.conf

	# install apparmor profile
	install -D -m 0644 ${S}/debian/apparmor-profile \
	        ${D}${sysconfdir}/apparmor.d/usr.sbin.ntpd
	install -D -m 0644 ${S}/debian/apparmor-profile.tunable \
	        ${D}${sysconfdir}/apparmor.d/tunables/ntpd
	install -d ${D}${sysconfdir}/apparmor/init/network-interface-security
	ln -sf ${sysconfdir}/apparmor.d/usr.sbin.ntpd \
	       ${D}${sysconfdir}/apparmor/init/network-interface-security/usr.sbin.ntpd

	# install apport hook
	install -D -m 644 ${S}/debian/source_ntpsec.py \
	        ${D}${datadir}/apport/package-hooks/source_ntpsec.py
	install -d ${D}${nonarch_libdir}/ntp
	install -m 0755 ${S}/debian/ntp-systemd-wrapper ${D}${nonarch_libdir}/ntp/
	install -m 0755 ${S}/debian/rotate-stats ${D}${nonarch_libdir}/ntp/

	# Install cron files
	install -d ${D}${sysconfdir}/cron.d
	install -m 0644 ${S}/debian/ntpsec.cron.d \
	                ${D}${sysconfdir}/cron.d/ntpsec
	install -m 0644 ${S}/debian/ntpsec-ntpviz.cron.d \
	                ${D}${sysconfdir}/cron.d/ntpsec-ntpviz

	# Init file
	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${S}/debian/ntpsec.init ${D}${sysconfdir}/init.d/ntpsec

	install -d ${D}${sysconfdir}/default
	install -m 0644 ${S}/debian/ntpsec.default \
	                ${D}${sysconfdir}/default/ntpsec
	install -m 0644 ${S}/debian/ntpsec-ntpdate.default \
	                ${D}${sysconfdir}/default/ntpsec-ntpdate
	install -m 0644 ${S}/debian/ntpsec-ntpviz.ntpviz.default \
	                ${D}${sysconfdir}/default/ntpviz

	install -d ${D}${sysconfdir}/network/if-up.d \
	           ${D}${sysconfdir}/apache2/conf-available
	install -m 0755 ${S}/debian/ntpsec-ntpdate.if-up \
	                ${D}${sysconfdir}/network/if-up.d/ntpsec-ntpdate
	install -m 0644 ${S}/debian/ntpsec-ntpviz.apache2 \
	                ${D}${sysconfdir}/apache2/conf-available/ntpsec-ntpviz.conf

	install -d ${D}/run/lock
	#Link
	install -d ${D}${localstatedir}/lib/ntpsec/ntpviz/day \
	           ${D}${localstatedir}/lib/ntpsec/ntpviz/week/
	ln -sf ${sysconfdir}/ntpviz/day/footer.html ${D}${localstatedir}/lib/ntpsec/ntpviz/day/footer
	ln -sf ${sysconfdir}/ntpviz/day/header.html ${D}${localstatedir}/lib/ntpsec/ntpviz/day/header
	ln -sf ${sysconfdir}/ntpviz/index.html ${D}${datadir}/ntpsec-ntpviz/ntpviz/index.html
	ln -sf ${sysconfdir}/ntpviz/week/footer.html ${D}${localstatedir}/lib/ntpsec/ntpviz/week/footer
	ln -sf ${sysconfdir}/ntpviz/week/header.html ${D}${localstatedir}/lib/ntpsec/ntpviz/week/header
	ln -sf ${localstatedir}/lib/ntpsec/ntpviz/day ${D}${datadir}/ntpsec-ntpviz/ntpviz/day
	ln -sf ${localstatedir}/lib/ntpsec/ntpviz/week ${D}${datadir}/ntpsec-ntpviz/ntpviz/week
}

USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "--system --no-create-home --home /nonexistent ntpsec"

pkg_postinst_${PN}() {
	install -o ntpsec -g ntpsec -d $D${localstatedir}/lib/ntpsec
}

PACKAGES =+ "${PN}-ntpviz python3-ntp ${PN}-ntpdate"

FILES_${PN} += " \
    ${nonarch_libdir}/ntp \
    ${datadir}/apport \
    ${systemd_system_unitdir} \
    /run/lock \
"
FILES_${PN}-ntpviz = " \
    ${bindir}/ntpviz \
    ${sbindir}/ntplog* \
    ${sysconfdir}/ntpviz \
    ${sysconfdir}/*/*ntpviz \
    ${sysconfdir}/*/*/*ntpviz.conf \
    ${systemd_system_unitdir}/ntplog* \
    ${systemd_system_unitdir}/ntpviz* \
    ${datadir}/ntpsec-ntpviz \
    ${localstatedir}/lib/ntpsec/ntpviz \
"
FILES_python3-ntp = "${libdir}/python3/dist-packages/*"

FILES_${PN}-ntpdate = " \
    ${bindir}/ntpdig \
    ${sbindir}/ntpdate* \
    ${sysconfdir}/default/ntpsec-ntpdate \
    ${sysconfdir}/*/*/ntpsec-ntpdate \
"

RDEPENDS_${PN} += "python3-ntp lsb"
RDEPENDS_${PN}-ntpdate += "python3-ntp"
RDEPENDS_${PN}-ntpviz += "python3-ntp python"
RDEPENDS_python3-ntp += "python3-core"
