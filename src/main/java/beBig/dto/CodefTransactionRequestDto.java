package beBig.dto;

import lombok.Data;

@Data
public class CodefTransactionRequestDto {
    private String organization;  // 기관코드
    private String connectedId;    // 커넥티드 아이디
    private String account;         // 계좌번호
    private String startDate;      // 시작일자
    private String endDate;        // 종료일자
    private String orderBy;
}
