package beBig.dto;

import lombok.Data;

@Data
public class AccountRequestDto {
    private String bank;
    private String userBankId;
    private String bankPassword;
}
