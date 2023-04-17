package com.kakaocert.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.barocert.BarocertException;
import com.barocert.kakaocert.KakaocertService;
import com.barocert.kakaocert.cms.CMS;
import com.barocert.kakaocert.cms.CMSReceipt;
import com.barocert.kakaocert.cms.CMSResult;
import com.barocert.kakaocert.cms.CMSStatus;
import com.barocert.kakaocert.identity.Identity;
import com.barocert.kakaocert.identity.IdentityReceipt;
import com.barocert.kakaocert.identity.IdentityResult;
import com.barocert.kakaocert.identity.IdentityStatus;
import com.barocert.kakaocert.sign.MultiSign;
import com.barocert.kakaocert.sign.MultiSignReceipt;
import com.barocert.kakaocert.sign.MultiSignResult;
import com.barocert.kakaocert.sign.MultiSignStatus;
import com.barocert.kakaocert.sign.MultiSignTokens;
import com.barocert.kakaocert.sign.Sign;
import com.barocert.kakaocert.sign.SignReceipt;
import com.barocert.kakaocert.sign.SignResult;
import com.barocert.kakaocert.sign.SignStatus;

@Controller
public class KakaocertServiceController {

    @Autowired
    private KakaocertService kakaocertService;

    // 이용기관코드
    // 파트너가 등록한 이용기관의 코드, (파트너 사이트에서 확인가능)
    @Value("${kakaocert.clientCode}")
    private String ClientCode;
    
    /*
     * 카카오톡 사용자에게 본인인증 전자서명을 요청합니다.
     */
    @RequestMapping(value = "kakaocert/requestIdentity", method = RequestMethod.GET)
    public String requestIdentity(Model m) throws BarocertException {

        // 본인인증 요청 정보 객체
        Identity identityRequest = new Identity();

        // 수신자 정보
        // 휴대폰번호,성명,생년월일 또는 Ci(연계정보)값 중 택 일
        identityRequest.setReceiverHP(kakaocertService.encrypt("01054437896"));
        identityRequest.setReceiverName(kakaocertService.encrypt("최상혁"));
        identityRequest.setReceiverBirthday(kakaocertService.encrypt("19880301"));
        // identityRequest.setCi(kakaocertService.encrypt(""));

        // 인증요청 메시지 제목 - 최대 40자
        identityRequest.setReqTitle("인증요청 메시지 제목란");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        identityRequest.setExpireIn(1000);
        // 서명 원문 - 최대 2,800자 까지 입력가능
        identityRequest.setToken(kakaocertService.encrypt("본인인증요청토큰"));

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        identityRequest.setAppUseYN(false);

        // App to App 방식 이용시, 호출할 URL
        // IdentityRequest.setReturnURL("https://www.kakaocert.com");

        try {
            IdentityReceipt result = kakaocertService.requestIdentity(ClientCode, identityRequest);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestIdentity";
    }

    /*
     * 본인인증 요청시 반환된 접수아이디를 통해 서명 상태를 확인합니다.
     */
    @RequestMapping(value = "kakaocert/getIdentityStatus", method = RequestMethod.GET)
    public String getIdentityStatus(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02304050230300000040000000000003";

        try {
            IdentityStatus result = kakaocertService.getIdentityStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/getIdentityStatus";
    }

    /*
     * 본인인증 요청시 반환된 접수아이디를 통해 본인인증 서명을 검증합니다. 
     * 검증하기 API는 완료된 전자서명 요청당 1회만 요청 가능하며, 사용자가 서명을 완료후 유효시간(10분)이내에만 요청가능 합니다.
     */
    @RequestMapping(value = "kakaocert/verifyIdentity", method = RequestMethod.GET)
    public String verifyIdentity(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02304050230300000040000000000003";

        try {
            IdentityResult result = kakaocertService.verifyIdentity(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/verifyIdentity";
    }

    /*
     * 카카오톡 사용자에게 전자서명을 요청합니다.(단건)
     */
    @RequestMapping(value = "kakaocert/requestSign", method = RequestMethod.GET)
    public String requestSign(Model m) throws BarocertException {

        // 전자서명 요청 정보 객체
        Sign eSignRequest = new Sign();

        // 수신자 정보
        // 휴대폰번호,성명,생년월일 또는 Ci(연계정보)값 중 택 일
        eSignRequest.setReceiverHP(kakaocertService.encrypt("01054437896"));
        eSignRequest.setReceiverName(kakaocertService.encrypt("최상혁"));
        eSignRequest.setReceiverBirthday(kakaocertService.encrypt("19880301"));
        // eSignRequest.setCi(kakaocertService.encrypt(""));

        // 인증요청 메시지 제목 - 최대 40자
        eSignRequest.setReqTitle("전자서명단건테스트");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        eSignRequest.setExpireIn(1000);
        // 서명 원문 - 원문 2,800자 까지 입력가능
        eSignRequest.setToken(kakaocertService.encrypt("전자서명단건테스트데이터"));
        // 서명 원문 유형
        // TEXT - 일반 텍스트, HASH - HASH 데이터
        eSignRequest.setTokenType("TEXT");

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        eSignRequest.setAppUseYN(false);

        // App to App 방식 이용시, 호출할 URL
        // eSignRequest.setReturnURL("https://www.kakaocert.com");

        try {
            SignReceipt result = kakaocertService.requestSign(ClientCode, eSignRequest);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestSign";
    }

    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명 상태를 확인합니다. (단건)
     */
    @RequestMapping(value = "kakaocert/getSignStatus", method = RequestMethod.GET)
    public String getSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02304050230300000040000000000007";

        try {
            SignStatus result = kakaocertService.getSignStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/getSignStatus";
    }

    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명을 검증합니다. (단건)
     * 검증하기 API는 완료된 전자서명 요청당 1회만 요청 가능하며, 사용자가 서명을 완료후 유효시간(10분)이내에만 요청가능 합니다.
     */
    @RequestMapping(value = "kakaocert/verifySign", method = RequestMethod.GET)
    public String verfiySign(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02304050230300000040000000000007";

        try {
            SignResult result = kakaocertService.verifySign(ClientCode, receiptID);
            m.addAttribute("result", result);

        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/verifySign";
    }

    /*
     * 카카오톡 사용자에게 전자서명을 요청합니다.(복수)
     */
    @RequestMapping(value = "kakaocert/requestMultiSign", method = RequestMethod.GET)
    public String requestMultiSign(Model m) throws BarocertException {

        // 전자서명 요청 정보 객체
        MultiSign multiSignRequest = new MultiSign();

        // 수신자 정보
        // 휴대폰번호,성명,생년월일 또는 Ci(연계정보)값 중 택 일
        multiSignRequest.setReceiverHP(kakaocertService.encrypt("01054437896"));
        multiSignRequest.setReceiverName(kakaocertService.encrypt("최상혁"));
        multiSignRequest.setReceiverBirthday(kakaocertService.encrypt("19880301"));
        // multiSignRequest.setCi(kakaocertService.encrypt(""));

        // 인증요청 메시지 제목 - 최대 40자
        multiSignRequest.setReqTitle("전자서명복수테스트");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        multiSignRequest.setExpireIn(1000);

        // 개별문서 등록 - 최대 20 건
        // 개별 요청 정보 객체
        MultiSignTokens token = new MultiSignTokens();
        // 인증요청 메시지 제목 - 최대 40자
        token.setReqTitle("전자서명복수문서테스트1");
        // 서명 원문 - 원문 2,800자 까지 입력가능
        token.setToken(kakaocertService.encrypt("전자서명복수테스트데이터1"));
        multiSignRequest.addToken(token);

        // 개별 요청 정보 객체
        MultiSignTokens token2 = new MultiSignTokens();
        // 인증요청 메시지 제목 - 최대 40자
        token2.setReqTitle("전자서명복수문서테스트2");
        // 서명 원문 - 원문 2,800자 까지 입력가능
        token2.setToken(kakaocertService.encrypt("전자서명복수테스트데이터2"));
        multiSignRequest.addToken(token2);

        // 서명 원문 유형
        // TEXT - 일반 텍스트, HASH - HASH 데이터
        multiSignRequest.setTokenType("TEXT");

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        multiSignRequest.setAppUseYN(false);

        // App to App 방식 이용시, 에러시 호출할 URL
        // request.setReturnURL("https://www.kakaocert.com");

        try {
            MultiSignReceipt result = kakaocertService.requestMultiSign(ClientCode, multiSignRequest);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestMultiSign";
    }

    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명 상태를 확인합니다. (복수)
     */
    @RequestMapping(value = "kakaocert/getMultiSignStatus", method = RequestMethod.GET)
    public String getMultiSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02304110230300000040000000000022";

        try {
            MultiSignStatus result = kakaocertService.getMultiSignStatus(ClientCode, receiptID);
            m.addAttribute("result", result);

        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/getMultiSignStatus";
    }

    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명을 검증합니다. (복수)
     * 검증하기 API는 완료된 전자서명 요청당 1회만 요청 가능하며, 사용자가 서명을 완료후 유효시간(10분)이내에만 요청가능 합니다.
     */
    @RequestMapping(value = "kakaocert/verifyMultiSign", method = RequestMethod.GET)
    public String verifyMultiSign(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02304110230300000040000000000022";

        try {
            MultiSignResult result = kakaocertService.verifyMultiSign(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/verifyMultiSign";
    }

    /*
     * 카카오톡 사용자에게 자동이체 출금동의 전자서명을 요청합니다.
     */
    @RequestMapping(value = "kakaocert/requestCMS", method = RequestMethod.GET)
    public String requestCMS(Model m) throws BarocertException {

        // 출금동의 요청 정보 객체
        CMS cmsRequest = new CMS();

        // 수신자 정보
        // 휴대폰번호,성명,생년월일 또는 Ci(연계정보)값 중 택 일
        cmsRequest.setReceiverHP(kakaocertService.encrypt("01054437896"));
        cmsRequest.setReceiverName(kakaocertService.encrypt("최상혁"));
        cmsRequest.setReceiverBirthday(kakaocertService.encrypt("19880301"));
        // cmsRequest.setCi(kakaocertService.encrypt("");

        // 인증요청 메시지 제목 - 최대 40자
        cmsRequest.setReqTitle("인증요청 메시지 제공란");

        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        cmsRequest.setExpireIn(1000);

        // 청구기관명 - 최대 100자
        cmsRequest.setRequestCorp(kakaocertService.encrypt("청구기관명란"));
        // 출금은행명 - 최대 100자
        cmsRequest.setBankName(kakaocertService.encrypt("출금은행명란"));
        // 출금계좌번호 - 최대 32자
        cmsRequest.setBankAccountNum(kakaocertService.encrypt("9-4324-5117-58"));
        // 출금계좌 예금주명 - 최대 100자
        cmsRequest.setBankAccountName(kakaocertService.encrypt("예금주명 입력란"));
        // 출금계좌 예금주 생년월일 - 8자
        cmsRequest.setBankAccountBirthday(kakaocertService.encrypt("19930112"));
        // 출금유형
        // CMS - 출금동의용, FIRM - 펌뱅킹, GIRO - 지로용
        cmsRequest.setBankServiceType(kakaocertService.encrypt("CMS")); // CMS, FIRM, GIRO

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        cmsRequest.setAppUseYN(false);

        // App to App 방식 이용시, 에러시 호출할 URL
        // request.setReturnURL("https://www.kakaocert.com");

        try {
            CMSReceipt result = kakaocertService.requestCMS(ClientCode, cmsRequest);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestCMS";
    }

    /*
     * 자동이체 출금동의 요청시 반환된 접수아이디를 통해 서명 상태를 확인합니다.
     */
    @RequestMapping(value = "kakaocert/getCMSStatus", method = RequestMethod.GET)
    public String getCMSStatus(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "02304050230300000040000000000008";

        try {
            CMSStatus result = kakaocertService.getCMSStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/getCMSStatus";
    }

    /*
     * 자동이체 출금동의 요청시 반환된 접수아이디를 통해 서명을 검증합니다.
     * 검증하기 API는 완료된 전자서명 요청당 1회만 요청 가능하며, 사용자가 서명을 완료후 유효시간(10분)이내에만 요청가능 합니다.
     */
    @RequestMapping(value = "kakaocert/verifyCMS", method = RequestMethod.GET)
    public String verifyCMS(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "02304050230300000040000000000008";

        try {
            CMSResult result = kakaocertService.verifyCMS(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/verifyCMS";
    }

}
