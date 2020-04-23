FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

LINUX_DEFCONFIG_qemu-zynq7 ?= "xilinx_zynq_defconfig"
SRC_URI_append_qemu-zynq7 = " file://xilinx_zynq_defconfig"

do_configure_prepend_qemu-zynq7() {
	cp ${WORKDIR}/xilinx_zynq_defconfig ${S}/arch/arm/configs/
}
