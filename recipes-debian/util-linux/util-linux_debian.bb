require recipes-core/util-linux/util-linux.inc

LICENSE = "GPLv2+ & LGPLv2.1+ & GPLv3+ & BSD-3-Clause & BSD-4-Clause & GPLv2 & MIT & LGPLv2+ & PD & LGPLv3+"

#GPL-3+  -> libmount/python/context.c
#MIT 	 -> sys-utils/flock.c
#LGPL-2+ -> lib/procutils.c
#LGPL-3+ -> libmount/python/context.c
#Public-domain -> lib/blkdev.c
#BSD-4-Clause  -> text-utils/rev.c
LIC_FILES_CHKSUM = "\
    file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://README.licensing;md5=972a134f1e14b2b060e365df2fab0099 \
    file://Documentation/licenses/COPYING.BSD-3-Clause;md5=58dcd8452651fc8b07d1f65ce07ca8af \
    file://Documentation/licenses/COPYING.BSD-4-Clause-UC;md5=263860f8968d8bafa5392cab74285262 \
    file://Documentation/licenses/COPYING.GPL-2.0-or-later;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://Documentation/licenses/COPYING.ISC;md5=8ae98663bac55afe5d989919d296f28a \
    file://Documentation/licenses/COPYING.LGPL-2.1-or-later;md5=4fbd65380cdd255951079008b364516c \
    file://libblkid/COPYING;md5=693bcbbe16d3a4a4b37bc906bc01cc04 \
    file://libmount/COPYING;md5=7c7e39fb7d70ffe5d693a643e29987c2 \
    file://libsmartcols/COPYING;md5=693bcbbe16d3a4a4b37bc906bc01cc04 \
    file://libuuid/COPYING;md5=6d2cafc999feb2c2de84d4d24b23290c \
    file://libmount/python/context.c;endline=20;md5=5b784a6bf6aa8588c21cc1ed05376f7f \
    file://sys-utils/flock.c;endline=23;md5=d634740484f5cb632fdd9421c595c21a \
    file://lib/procutils.c;endline=15;md5=a1baefb4273a5f75c453335bab17d7e8 \
    file://libmount/python/context.c;endline=20;md5=5b784a6bf6aa8588c21cc1ed05376f7f \
    file://lib/blkdev.c;endline=6;md5=e97943ca94d6f6a7dc66b0026b42529c \
    file://text-utils/rev.c;endline=49;md5=74de9451b16aeab9294d2be6d7bea902 \
"

require recipes-debian/sources/util-linux.inc
inherit debian-package

FILESPATH_append = ":${COREBASE}/meta/recipes-core/util-linux/util-linux"

DEBIAN_QUILT_PATCHES = ""

SRC_URI += "\
    file://configure-sbindir.patch \
    file://runuser.pamd \
    file://runuser-l.pamd \
    file://ptest.patch \
    file://run-ptest \
    file://display_testname_for_subtest.patch \
    file://avoid_parallel_tests.patch \
    file://check-for-_HAVE_STRUCT_TERMIOS_C_OSPEED.patch \
"
