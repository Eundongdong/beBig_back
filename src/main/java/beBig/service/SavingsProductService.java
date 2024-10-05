package beBig.service;

import beBig.dto.SavingsProductDto;
import beBig.mapper.BankMapper;
import beBig.mapper.SavingsProductMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SavingsProductService {

    @Autowired
    private SavingsProductMapper savingsProductMapper;

    @Autowired
    private BankMapper bankMapper;

    public void saveSavingsProductData(String jsonData) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonData);

        // baseList에서 필요한 데이터를 가져옵니다.
        JsonNode baseList = rootNode.path("result").path("baseList");
        JsonNode optionList = rootNode.path("result").path("optionList");

        for (JsonNode baseNode : baseList) {
            String savingsProductCode = baseNode.path("fin_prdt_cd").asText();
            String bankCodeFss = baseNode.path("fin_co_no").asText();
            String savingsProductName = baseNode.path("fin_prdt_nm").asText();

            // bank_code_fss로 bank_id 가져오기
            Integer bankId = bankMapper.getBankIdByFssCode(bankCodeFss);
            if (bankId == null) {
                // bankId가 null인 경우 처리
                log.warn("은행 코드를 찾을 수 없습니다. bank_code_fss: " + bankCodeFss + ", 상품명: " + savingsProductName);
                continue;  // bank_id가 없으면 데이터 삽입을 건너뜀
            }

            for (JsonNode optionNode : optionList) {
                if (optionNode.path("fin_prdt_cd").asText().equals(savingsProductCode)) {
                    SavingsProductDto dto = new SavingsProductDto();
                    dto.setSavingsProductCode(savingsProductCode);
                    dto.setBankId(bankId);  // Integer 타입으로 설정
                    dto.setSavingsProductName(savingsProductName);
                    dto.setSavingsProductType(optionNode.path("rsrv_type_nm").asText());
                    dto.setSavingsProductTerm(optionNode.path("save_trm").asInt());
                    dto.setSavingsProductRate(optionNode.path("intr_rate").decimalValue());
                    dto.setSavingsProductMaxRate(optionNode.path("intr_rate2").decimalValue());

                    // MyBatis 매퍼를 통해 DB에 저장
                    savingsProductMapper.insertSavingsProduct(dto);
                }
            }
        }
    }
}