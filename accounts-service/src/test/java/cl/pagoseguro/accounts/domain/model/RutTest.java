package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.InvalidRutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RutTest {

    @ParameterizedTest(name = "\"{0}\" se normaliza como \"{1}\"")
    @CsvSource({
            "12.345.678-5, 12345678-5",
            "12345678-5,   12345678-5",
            "123456785,    12345678-5",
            "11.111.111-1, 11111111-1",
            "10.000.013-k, 10000013-K"
    })
    @DisplayName("acepta RUT válidos en distintos formatos y los normaliza")
    void acceptsValidRuts(String input, String expected) {
        assertThat(new Rut(input).value()).isEqualTo(expected);
    }

    @Test
    @DisplayName("rechaza un RUT con dígito verificador incorrecto")
    void rejectsWrongCheckDigit() {
        assertThatThrownBy(() -> new Rut("12.345.678-9"))
                .isInstanceOf(InvalidRutException.class)
                .hasMessageContaining("Dígito verificador");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1-1", "12.345.678-X"})
    @DisplayName("rechaza formatos inválidos")
    void rejectsInvalidFormat(String input) {
        assertThatThrownBy(() -> new Rut(input)).isInstanceOf(InvalidRutException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("rechaza RUT vacío o nulo")
    void rejectsBlank(String input) {
        assertThatThrownBy(() -> new Rut(input))
                .isInstanceOf(InvalidRutException.class)
                .hasMessage("El RUT es obligatorio");
    }
}
