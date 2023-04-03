package ru.netology.cloudservice.validation;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FileNameValidatorTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private ConstraintValidatorContext context;

    @InjectMocks
    private FileNameValidator sut;

    @ParameterizedTest
    @MethodSource("validFileNameSource")
    public void testValidFileName_success(String filename) {
        var result = sut.isValid(filename, context);
        assertTrue(result);
    }

    public static Stream<Arguments> validFileNameSource() {
        return Stream.of(
                Arguments.of("solidlatinfilename123"),
                Arguments.of("chars~!@#$%^&()-=+{}[];,.'"),
                Arguments.of("solidlatinfilename.with.ext"),
                Arguments.of("sparse latin filename.ext"),
                Arguments.of("latin filename..with dots...."),
                Arguments.of("single 'quoted' latin filename.txt"),
                Arguments.of("filename with `backticks`.txt"),
                Arguments.of("latin filename with dots....ext"),
                Arguments.of("Имя файла на русском языке.расш"),
                Arguments.of("【柳青瑶原创】鼓乐《兰陵王入阵曲》耳机开最大!来听千军万马")
        );
    }

    @NullAndEmptySource
    @ParameterizedTest
    @MethodSource("invalidFileNameSource")
    public void testInvalidFileName_failure(String filename) {
        var result = sut.isValid(filename, context);
        assertFalse(result);
    }

    public static Stream<Arguments> invalidFileNameSource() {
        return Stream.of(
                Arguments.of("file/name"),
                Arguments.of("file\\name"),
                Arguments.of("file?name"),
                Arguments.of("file*name"),
                Arguments.of("file:name"),
                Arguments.of("file>name"),
                Arguments.of("file<name"),
                Arguments.of("file\"quot\"name"),
                Arguments.of("file|name")
        );
    }

}