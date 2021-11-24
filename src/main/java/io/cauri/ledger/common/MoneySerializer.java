package io.cauri.ledger.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * las convenciones internas de API: "amount decimal como string en JSON (\"1250.50\")".
 * Always emits two decimal places regardless of the scale held in memory.
 */
public class MoneySerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
