require recipes-support/boost/boost.inc
require boost.inc

# Overwrite do_configure from base boost.inc
do_configure() {
	cp -f ${S}/libs/config/include/boost/config/platform/linux.hpp \
	      ${S}/libs/config/include/boost/config/platform/linux-gnueabi.hpp

	echo 'using gcc : : ${CXX} : <compileflags>"${CPPFLAGS}" <cflags>"${CFLAGS}" <cxxflags>"${CXXFLAGS} -Wno-unused-local-typedefs" <linkflags>"${LDFLAGS}" ;' > ${WORKDIR}/user-config.jam

	# If we want Python then we need to tell Boost *exactly* where to find it
	if ${@bb.utils.contains('BOOST_LIBS', 'python', 'true', 'false', d)}; then
		echo "using python : ${PYTHON_BASEVERSION} : ${STAGING_DIR_HOST}${bindir}/python3 : ${STAGING_DIR_HOST}${includedir}/${PYTHON_DIR}${PYTHON_ABI} : ${STAGING_DIR_HOST}${libdir}/${PYTHON_DIR} ;" >> ${WORKDIR}/user-config.jam
	fi

	if ${@bb.utils.contains('BOOST_LIBS', 'mpi', 'true', 'false', d)}; then
		echo "using mpi : : <find-shared-library>mpi ;" >> ${WORKDIR}/user-config.jam
	fi

	CC="${BUILD_CC}" CFLAGS="${BUILD_CFLAGS}" ./bootstrap.sh --with-bjam=bjam --with-toolset=gcc

	# Boost can't be trusted to find Python on it's own, so remove any mention
	# of it from the boost configuration
	sed -i '/using python/d' ${S}/project-config.jam
}
