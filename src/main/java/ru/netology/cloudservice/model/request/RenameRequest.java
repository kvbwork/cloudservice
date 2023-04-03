package ru.netology.cloudservice.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import ru.netology.cloudservice.validation.ValidFileName;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenameRequest {

    @ValidFileName
    String filename;

}
