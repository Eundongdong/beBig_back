package beBig.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinInfoResponseDto {
    private int finTypeCode;
    private String finTypeAnimal;
    private String finTypeTitle;
    private String finTypeAnimalDescription;
    private String finTypeTitleDescription;
    private String finTypeHabit1;
    private String finTypeHabit2;
}