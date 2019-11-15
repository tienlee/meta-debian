# base recipe: meta/recipes-extended/logrotate/logrotate_3.15.0.bb
# base branch: warrior

SUMMARY = "Rotates, compresses, removes and mails system log files"
SECTION = "console/utils"
HOMEPAGE = "https://github.com/logrotate/logrotate/issues"
LICENSE = "GPLv2"

DEPENDS="coreutils popt"

LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

inherit debian-package
require recipes-debian/sources/logrotate.inc

SRC_URI += "file://act-as-mv-when-rotate.patch \
            file://disable-check-different-filesystems.patch \
            "

PACKAGECONFIG ?= "${@bb.utils.filter('DISTRO_FEATURES', 'acl selinux', d)}"
PACKAGECONFIG[acl] = ",,acl"
PACKAGECONFIG[selinux] = ",,libselinux"

CONFFILES_${PN} += "${localstatedir}/lib/logrotate.status \
                    ${sysconfdir}/logrotate.conf \
                    ${sysconfdir}/logrotate.d/btmp \
                    ${sysconfdir}/logrotate.d/wtmp"

# If RPM_OPT_FLAGS is unset, it adds -g itself rather than obeying our
# optimization variables, so use it rather than EXTRA_CFLAGS.
EXTRA_OEMAKE = "\
	${@bb.utils.contains('PACKAGECONFIG', 'acl', 'WITH_ACL=yes', '', d)} \
	${@bb.utils.contains('PACKAGECONFIG', 'selinux', 'WITH_SELINUX=yes', '', d)} \
"
inherit autotools systemd pkgconfig

SYSTEMD_SERVICE_${PN} = "\
	${BPN}.service \
	${BPN}.timer \
"

LOGROTATE_OPTIONS ?= ""

LOGROTATE_SYSTEMD_TIMER_BASIS ?= "daily"
LOGROTATE_SYSTEMD_TIMER_ACCURACY ?= "12h"
LOGROTATE_SYSTEMD_TIMER_PERSISTENT ?= "true"

do_install(){
	oe_runmake install DESTDIR=${D} PREFIX=${D} MANDIR=${mandir}
	install -d ${D}${sysconfdir}/logrotate.d
	install -d ${D}${localstatedir}/lib
	install -p -m 644 ${S}/debian/logrotate.conf ${D}${sysconfdir}/logrotate.conf
	install -p -m 644 ${S}/examples/btmp ${D}${sysconfdir}/logrotate.d/btmp
	install -p -m 644 ${S}/examples/wtmp ${D}${sysconfdir}/logrotate.d/wtmp
	touch ${D}${localstatedir}/lib/logrotate.status

	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -d ${D}${systemd_system_unitdir}
		install -m 0644 ${S}/examples/logrotate.service ${D}${systemd_system_unitdir}/logrotate.service
		install -m 0644 ${S}/examples/logrotate.timer ${D}${systemd_system_unitdir}/logrotate.timer
		[ -z "${LOGROTATE_OPTIONS}" ] ||
			sed -ri \
				-e 's|(ExecStart=.*/logrotate.*)$|\1 ${LOGROTATE_OPTIONS}|g' \
				${D}${systemd_system_unitdir}/logrotate.service
		sed -ri \
			-e 's|(OnCalendar=).*$|\1${LOGROTATE_SYSTEMD_TIMER_BASIS}|g' \
			-e 's|(AccuracySec=).*$|\1${LOGROTATE_SYSTEMD_TIMER_ACCURACY}|g' \
			-e 's|(Persistent=).*$|\1${LOGROTATE_SYSTEMD_TIMER_PERSISTENT}|g' \
			${D}${systemd_system_unitdir}/logrotate.timer
	fi

	if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
		install -d ${D}${sysconfdir}/cron.daily
		install -p -m 0755 ${S}/examples/logrotate.cron ${D}${sysconfdir}/cron.daily/logrotate
	fi
}
