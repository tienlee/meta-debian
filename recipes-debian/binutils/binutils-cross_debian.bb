#
# base recipe: meta/recipes-devtools/binutils/binutils-cross_2.25.bb
# base branch: master
# base commit: 3b7c38458856805588d552508de10944ed38d9f2
#

require binutils.inc

PR = "${INC_PR}.0"

inherit cross
PROVIDES = "virtual/${TARGET_PREFIX}binutils"

INHIBIT_DEFAULT_DEPS = "1"
INHIBIT_AUTOTOOLS_DEPS = "1"

EXTRA_OECONF += "--with-sysroot=${STAGING_DIR_TARGET} \
                --disable-install-libbfd \
                --enable-poison-system-directories \
                "
do_install () {
	oe_runmake 'DESTDIR=${D}' install

	# We don't really need these, so we'll remove them...
	rm -rf ${D}${STAGING_DIR_NATIVE}${libdir_native}/libiberty.a
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/${TARGET_SYS}
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/lib/ldscripts
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/share/info
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/share/locale
	rm -rf ${D}${STAGING_DIR_NATIVE}${prefix_native}/share/man
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/share || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir}/gcc-lib || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir}64/gcc-lib || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir} || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${libdir}64 || :
	rmdir ${D}${STAGING_DIR_NATIVE}${prefix_native}/${prefix} || :
}
