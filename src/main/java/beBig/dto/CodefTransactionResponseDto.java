package beBig.dto;

import lombok.Data;

import java.util.List;

@Data
public class CodefTransactionResponseDto {
    // codef 거래내역 응답 데이터
    private String accountNum;  // 계좌번호
    private List<HistoryItem> resTrHistoryList;  // 거래내역 리스트

    @Data
    public static class HistoryItem {
        private String resAccountTrDate; // 거래 날짜
        private String resAccountTrTime; // 거래 시간
        private String resAccountOut; // 출금액
        private String resAccountIn; // 입금액
        private String resAccountDesc1; // 거래 설명 1
        private String resAccountDesc2; // 거래 설명 2
        private String resAccountDesc3; // 거래 설명 3
        private String resAccountDesc4; // 거래 설명 4
        private String resAfterTranBalance; // 거래 후 잔액
        private String tranDesc; // 거래 설명
    }
}