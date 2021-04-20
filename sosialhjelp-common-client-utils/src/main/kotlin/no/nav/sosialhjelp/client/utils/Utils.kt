package no.nav.sosialhjelp.client.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.core.ParameterizedTypeReference

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

val objectMapper: ObjectMapper = ObjectMapper()
    .registerModules(KotlinModule())
    .configure(SerializationFeature.INDENT_OUTPUT, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
