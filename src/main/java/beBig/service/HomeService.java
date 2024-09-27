package beBig.service;

import beBig.form.AccountForm;
import beBig.form.CodefResponseForm;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
    List<CodefResponseForm> getUserAccount(Long userId, AccountForm accountForm) throws Exception;
    boolean addAccount(Long userId, List<CodefResponseForm> codefResponseFormList);
    List<AccountVo> showMyAccount(Long userId) throws Exception;
}
