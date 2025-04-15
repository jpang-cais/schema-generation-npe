package org.example

import com.fasterxml.jackson.annotation.JsonProperty
import io.confluent.kafka.schemaregistry.json.JsonSchemaUtils
import io.confluent.kafka.schemaregistry.json.SpecificationVersion
import io.confluent.kafka.schemaregistry.json.jackson.Jackson
import java.math.BigDecimal
import javax.money.Monetary
import javax.money.MonetaryAmount
import javax.validation.Valid
import kotlin.math.max
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.zalando.jackson.datatype.money.BigDecimalAmountWriter
import org.zalando.jackson.datatype.money.MoneyModule

class SchemaGenerationTest {

    data class Foo(
        @field:Valid
        @get:JsonProperty("amount", required = true)
        val amount: Money,
    )

    val MonetaryAmount.roundedAmount: BigDecimal
        get() = with(Monetary.getDefaultRounding()).number.numberValue(BigDecimal::class.java).let {
            it.setScale(max(currency.defaultFractionDigits, it.scale()))
        }

    // This test fails when using version 7.8.2 of `io.confluent:kafka-json-schema-serializer`, but passes with 7.8.1.
    @Test
    fun `should generate schema`() {
        val foo = Foo(Money.of(10, "USD"))

        val objectMapper = Jackson.newObjectMapper().apply {
            registerModules(
                MoneyModule().withNumbers(BigDecimalAmountWriter { it.roundedAmount }),
            )
        }

        val schema = JsonSchemaUtils.getSchema(foo, SpecificationVersion.DRAFT_7, true, true, objectMapper, null)

        assertEquals(
            """{"${'$'}schema":"http://json-schema.org/draft-07/schema#","title":"Foo","type":"object","additionalProperties":false,"properties":{"amount":{"${'$'}ref":"#/definitions/Money"}},"required":["amount"],"definitions":{"Money":{"type":"object","additionalProperties":false,"properties":{"amount":{"type":"number"},"currency":{"type":"string"},"formatted":{"oneOf":[{"type":"null","title":"Not included"},{"type":"string"}]}},"required":["amount","currency"]}}}""",
            schema.toString(),
        )
    }
}
