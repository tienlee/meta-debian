# base recipe: meta/recipes-support/boost/bjam-native_1.9.0.bb
# base branch: warrior

require boost.inc

SUMMARY = "Portable Boost.Jam build tool for boost"

inherit native

FILESPATH_append = ":${COREBASE}/meta/recipes-support/boost/files"
SRC_URI += "file://bjam-native-build-bjam.debug.patch"

do_compile() {
	./bootstrap.sh --with-toolset=gcc
}

do_install() {
	install -d ${D}${bindir}/
	# install unstripped version for bjam
	install -c -m 755 bjam.debug ${D}${bindir}/bjam
}
