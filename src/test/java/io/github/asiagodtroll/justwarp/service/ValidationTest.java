package io.github.asiagodtroll.justwarp.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationTest {
    @Test
    void validatesNamesByDomainRules() throws Exception {
        assertDoesNotThrow(() -> NameValidator.warp("傳送點"));
        assertThrows(WarpException.class, () -> NameValidator.group("none"));
        assertThrows(WarpException.class, () -> NameValidator.customIcon("minecraft:stone"));
        assertThrows(WarpException.class, () -> NameValidator.warp("has space"));
    }

    @Test
    void validatesPlayerHeadTexturePayload() {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/test\"}}}";
        String valid = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> IconPolicy.validateTexture(valid));
        assertThrows(WarpException.class, () -> IconPolicy.validateTexture("not-base64"));
    }

    @Test
    void warpExceptionDefensivelyCopiesArguments() {
        Object[] arguments = {"home"};
        WarpException exception = new WarpException("error.warp_missing", arguments);

        arguments[0] = "changed";
        Object[] returned = exception.arguments();
        returned[0] = "changed again";

        assertArrayEquals(new Object[]{"home"}, exception.arguments());
    }
}
