package de.dasbabypixel.gamelauncher.api.util.logging.slf4j

import de.dasbabypixel.gamelauncher.api.util.logging.Marker
import de.dasbabypixel.gamelauncher.api.util.logging.log4j.LWJGLLogging
import de.dasbabypixel.gamelauncher.api.util.logging.log4j.Log4jConfiguration
import org.slf4j.Logger

class SLF4JLogger(val l: Logger) : de.dasbabypixel.gamelauncher.api.util.logging.Logger {
    init {
        LWJGLLogging.init()
    }

    override val name: String
        get() = l.name

    override fun debug(msg: String) {
        l.debug(msg)
    }

    override fun debug(format: String, arg: Any?) {
        l.debug(format, arg)
    }

    override fun debug(format: String, arg1: Any?, arg2: Any?) {
        l.debug(format, arg1, arg2)
    }

    override fun debug(format: String, vararg arguments: Any?) {
        l.debug(format, arguments)
    }

    override fun debug(msg: String, t: Throwable) {
        l.debug(msg, t)
    }

    override fun debug(
        marker: Marker, msg: String
    ) {
        l.debug((marker as SLF4JMarker).m, msg)
    }

    override fun debug(
        marker: Marker, format: String, arg: Any?
    ) {
        l.debug((marker as SLF4JMarker).m, format, arg)
    }

    override fun debug(
        marker: Marker, format: String, arg1: Any?, arg2: Any?
    ) {
        l.debug((marker as SLF4JMarker).m, format, arg1, arg2)
    }

    override fun debug(
        marker: Marker, format: String, vararg arguments: Any?
    ) {
        l.debug((marker as SLF4JMarker).m, format, arguments)
    }

    override fun debug(
        marker: Marker, msg: String, t: Throwable
    ) {
        l.debug((marker as SLF4JMarker).m, msg, t)
    }

    override fun info(msg: String) {
        l.info(msg)
    }

    override fun info(format: String, arg: Any?) {
        l.info(format, arg)
    }

    override fun info(format: String, arg1: Any?, arg2: Any?) {
        l.info(format, arg1, arg2)
    }

    override fun info(format: String, vararg arguments: Any?) {
        l.info(format, arguments)
    }

    override fun info(msg: String, t: Throwable) {
        l.info(msg, t)
    }

    override fun info(
        marker: Marker, msg: String
    ) {
        l.info((marker as SLF4JMarker).m, msg)
    }

    override fun info(
        marker: Marker, format: String, arg: Any?
    ) {
        l.info((marker as SLF4JMarker).m, format, arg)
    }

    override fun info(
        marker: Marker, format: String, arg1: Any?, arg2: Any?
    ) {
        l.info((marker as SLF4JMarker).m, format, arg1, arg2)
    }

    override fun info(
        marker: Marker, format: String, vararg arguments: Any?
    ) {
        l.info((marker as SLF4JMarker).m, format, arguments)
    }

    override fun info(
        marker: Marker, msg: String, t: Throwable
    ) {
        l.info((marker as SLF4JMarker).m, msg, t)
    }

    override fun warn(msg: String) {
        l.warn(msg)
    }

    override fun warn(format: String, arg: Any?) {
        l.warn(format, arg)
    }

    override fun warn(format: String, arg1: Any?, arg2: Any?) {
        l.warn(format, arg1, arg2)
    }

    override fun warn(format: String, vararg arguments: Any?) {
        l.warn(format, arguments)
    }

    override fun warn(msg: String, t: Throwable) {
        l.warn(msg, t)
    }

    override fun warn(
        marker: Marker, msg: String
    ) {
        l.warn((marker as SLF4JMarker).m, msg)
    }

    override fun warn(
        marker: Marker, format: String, arg: Any?
    ) {
        l.warn((marker as SLF4JMarker).m, format, arg)
    }

    override fun warn(
        marker: Marker, format: String, arg1: Any?, arg2: Any?
    ) {
        l.warn((marker as SLF4JMarker).m, format, arg1, arg2)
    }

    override fun warn(
        marker: Marker, format: String, vararg arguments: Any?
    ) {
        l.warn((marker as SLF4JMarker).m, format, arguments)
    }

    override fun warn(
        marker: Marker, msg: String, t: Throwable
    ) {
        l.warn((marker as SLF4JMarker).m, msg, t)
    }

    override fun error(msg: String) {
        l.error(msg)
    }

    override fun error(format: String, arg: Any?) {
        l.error(format, arg)
    }

    override fun error(format: String, arg1: Any?, arg2: Any?) {
        l.error(format, arg1, arg2)
    }

    override fun error(format: String, vararg arguments: Any?) {
        l.error(format, arguments)
    }

    override fun error(msg: String, t: Throwable) {
        l.error(msg, t)
    }

    override fun error(
        marker: Marker, msg: String
    ) {
        l.error((marker as SLF4JMarker).m, msg)
    }

    override fun error(
        marker: Marker, format: String, arg: Any?
    ) {
        l.error((marker as SLF4JMarker).m, format, arg)
    }

    override fun error(
        marker: Marker, format: String, arg1: Any?, arg2: Any?
    ) {
        l.error((marker as SLF4JMarker).m, format, arg1, arg2)
    }

    override fun error(
        marker: Marker, format: String, vararg arguments: Any?
    ) {
        l.error((marker as SLF4JMarker).m, format, arguments)
    }

    override fun error(
        marker: Marker, msg: String, t: Throwable
    ) {
        l.error((marker as SLF4JMarker).m, msg, t)
    }
}