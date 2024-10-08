package beBig.vo;

import lombok.Data;

@Data
public class FinTypeVo {
    private int finTypeCode;
    private String finTypeAnimal;
    private String finTypeTitle;
    private String finTypeAnimalDescription;
    private String finTypeTitleDescription;
    private String finTypeHabit1;
    private String finTypeHabit2;
    private double finTypeUseRate;
    private double finTypeSaveRate;
}