package com.example.kakaocert.controller;

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
import com.barocert.kakaocert.login.LoginResult;
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
    // 파트너가 등록한 이용기관의 코드 (파트너 사이트에서 확인가능)
    @Value("${kakaocert.clientCode}")
    private String ClientCode;
    
    /*
     * 카카오톡 이용자에게 본인인증을 요청합니다.
     * https://developers.barocert.com/reference/kakao/java/identity/api#RequestIdentity
     */
    @RequestMapping(value = "kakaocert/requestIdentity", method = RequestMethod.GET)
    public String requestIdentity(Model m) throws BarocertException {

        // 본인인증 요청 정보 객체
        Identity identity = new Identity();

        
        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        identity.setReceiverHP(kakaocertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        identity.setReceiverName(kakaocertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        identity.setReceiverBirthday(kakaocertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        identity.setReqTitle("인증요청 메시지 제목란");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        identity.setExpireIn(1000);
        // 서명 원문 - 최대 40자 까지 입력가능
        identity.setToken(kakaocertService.encrypt("본인인증요청토큰"));

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        identity.setAppUseYN(false);

        // App to App 방식 이용시, 호출할 URL
        // identity.setReturnURL("https://www.kakaocert.com");

        try {
            IdentityReceipt result = kakaocertService.requestIdentity(ClientCode, identity);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestIdentity";
    }

    /*
     * 본인인증 요청 후 반환받은 접수아이디로 본인인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/kakao/java/identity/api#GetIdentityStatus
     */
    @RequestMapping(value = "kakaocert/getIdentityStatus", method = RequestMethod.GET)
    public String getIdentityStatus(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000001";

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
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 반환받은 전자서명값(signedData)과 [1. RequestIdentity] 함수 호출에 입력한 Token의 동일 여부를 확인하여 이용자의 본인인증 검증을 완료합니다.
     * 카카오 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * 전자서명 완료일시로부터 10분 이후에 검증 API를 호출하면 오류가 반환됩니다.
     * https://developers.barocert.com/reference/kakao/java/identity/api#VerifyIdentity
     */
    @RequestMapping(value = "kakaocert/verifyIdentity", method = RequestMethod.GET)
    public String verifyIdentity(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000001";

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
     * 카카오톡 이용자에게 단건(1건) 문서의 전자서명을 요청합니다.
     * https://developers.barocert.com/reference/kakao/java/sign/api-single#RequestSign
     */
    @RequestMapping(value = "kakaocert/requestSign", method = RequestMethod.GET)
    public String requestSign(Model m) throws BarocertException {

        // 전자서명 요청 정보 객체
        Sign sign = new Sign();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        sign.setReceiverHP(kakaocertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        sign.setReceiverName(kakaocertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        sign.setReceiverBirthday(kakaocertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        sign.setReqTitle("전자서명단건테스트");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        sign.setExpireIn(1000);
        // 서명 원문 - 원문 2,800자 까지 입력가능
        sign.setToken(kakaocertService.encrypt("전자서명단건테스트데이터"));
        // 서명 원문 유형
        // TEXT - 일반 텍스트, HASH - HASH 데이터
        sign.setTokenType("TEXT");

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        sign.setAppUseYN(false);

        // App to App 방식 이용시, 호출할 URL
        // sign.setReturnURL("https://www.kakaocert.com");

        try {
            SignReceipt result = kakaocertService.requestSign(ClientCode, sign);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestSign";
    }

    /*
     * 전자서명(단건) 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/kakao/java/sign/api-single#GetSignStatus
     */
    @RequestMapping(value = "kakaocert/getSignStatus", method = RequestMethod.GET)
    public String getSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000002";

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
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 카카오 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * 전자서명 완료일시로부터 10분 이후에 검증 API를 호출하면 오류가 반환됩니다.
     * https://developers.barocert.com/reference/kakao/java/sign/api-single#VerifySign
     */
    @RequestMapping(value = "kakaocert/verifySign", method = RequestMethod.GET)
    public String verfiySign(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000002";

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
     * 카카오톡 이용자에게 복수(최대 20건) 문서의 전자서명을 요청합니다.
     * https://developers.barocert.com/reference/kakao/java/sign/api-multi#RequestMultiSign
     */
    @RequestMapping(value = "kakaocert/requestMultiSign", method = RequestMethod.GET)
    public String requestMultiSign(Model m) throws BarocertException {

        // 전자서명 요청 정보 객체
        MultiSign multiSign = new MultiSign();

        
        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        multiSign.setReceiverHP(kakaocertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        multiSign.setReceiverName(kakaocertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        multiSign.setReceiverBirthday(kakaocertService.encrypt("19700101"));
        // multiSign.setCi(kakaocertService.encrypt(""));

        // 인증요청 메시지 제목 - 최대 40자
        multiSign.setReqTitle("전자서명복수테스트");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        multiSign.setExpireIn(1000);

        // 개별문서 등록 - 최대 20 건
        // 개별 요청 정보 객체
        MultiSignTokens token = new MultiSignTokens();
        // 인증요청 메시지 제목 - 최대 40자
        token.setReqTitle("전자서명복수문서테스트1");
        // 서명 원문 - 원문 2,800자 까지 입력가능
        token.setToken(kakaocertService.encrypt("전자서명복수테스트데이터1"));
        multiSign.addToken(token);

        // 개별 요청 정보 객체
        MultiSignTokens token2 = new MultiSignTokens();
        // 인증요청 메시지 제목 - 최대 40자
        token2.setReqTitle("전자서명복수문서테스트2");
        // 서명 원문 - 원문 2,800자 까지 입력가능
        token2.setToken(kakaocertService.encrypt("전자서명복수테스트데이터2"));
        multiSign.addToken(token2);

        // 서명 원문 유형
        // TEXT - 일반 텍스트, HASH - HASH 데이터
        multiSign.setTokenType("TEXT");

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        multiSign.setAppUseYN(false);

        // App to App 방식 이용시, 호출할 URL
        // multiSign.setReturnURL("https://www.kakaocert.com");

        try {
            MultiSignReceipt result = kakaocertService.requestMultiSign(ClientCode, multiSign);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestMultiSign";
    }

    /*
     * 전자서명(복수) 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/kakao/java/sign/api-multi#GetMultiSignStatus
     */
    @RequestMapping(value = "kakaocert/getMultiSignStatus", method = RequestMethod.GET)
    public String getMultiSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000003";

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
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 카카오 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * 전자서명 완료일시로부터 10분 이후에 검증 API를 호출하면 오류가 반환됩니다.
     * https://developers.barocert.com/reference/kakao/java/sign/api-multi#VerifyMultiSign
     */
    @RequestMapping(value = "kakaocert/verifyMultiSign", method = RequestMethod.GET)
    public String verifyMultiSign(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000003";

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
     * 카카오톡 이용자에게 자동이체 출금동의를 요청합니다.
     * https://developers.barocert.com/reference/kakao/java/cms/api#RequestCMS
     */
    @RequestMapping(value = "kakaocert/requestCMS", method = RequestMethod.GET)
    public String requestCMS(Model m) throws BarocertException {

        // 출금동의 요청 정보 객체
        CMS cms = new CMS();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        cms.setReceiverHP(kakaocertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        cms.setReceiverName(kakaocertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        cms.setReceiverBirthday(kakaocertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        cms.setReqTitle("인증요청 메시지 제공란");

        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        cms.setExpireIn(1000);

        // 청구기관명 - 최대 100자
        cms.setRequestCorp(kakaocertService.encrypt("청구기관명란"));
        // 출금은행명 - 최대 100자
        cms.setBankName(kakaocertService.encrypt("출금은행명란"));
        // 출금계좌번호 - 최대 32자
        cms.setBankAccountNum(kakaocertService.encrypt("9-***4-5117-58"));
        // 출금계좌 예금주명 - 최대 100자
        cms.setBankAccountName(kakaocertService.encrypt("예금주명 입력란"));
        // 출금계좌 예금주 생년월일 - 8자
        cms.setBankAccountBirthday(kakaocertService.encrypt("19700101"));
        // 출금유형
        // CMS - 출금동의용, FIRM - 펌뱅킹, GIRO - 지로용
        cms.setBankServiceType(kakaocertService.encrypt("CMS")); // CMS, FIRM, GIRO

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        cms.setAppUseYN(false);

        // App to App 방식 이용시, 호출할 URL
        // cms.setReturnURL("https://www.kakaocert.com");

        try {
            CMSReceipt result = kakaocertService.requestCMS(ClientCode, cms);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/requestCMS";
    }

    /*
     * 자동이체 출금동의 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/kakao/java/cms/api#GetCMSStatus
     */
    @RequestMapping(value = "kakaocert/getCMSStatus", method = RequestMethod.GET)
    public String getCMSStatus(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000004";

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
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 카카오 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * 전자서명 완료일시로부터 10분 이후에 검증 API를 호출하면 오류가 반환됩니다.
     * https://developers.barocert.com/reference/kakao/java/cms/api#VerifyCMS
     */
    @RequestMapping(value = "kakaocert/verifyCMS", method = RequestMethod.GET)
    public String verifyCMS(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "02307240230400000010000000000004";

        try {
            CMSResult result = kakaocertService.verifyCMS(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/verifyCMS";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명 데이터 전문(signedData)을 반환 받습니다.
     * 카카오 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * 전자서명 완료일시로부터 10분 이후에 검증 API를 호출하면 오류가 반환됩니다.
     * https://developers.barocert.com/reference/kakao/java/login/api#VerifyLogin
     */
    @RequestMapping(value = "kakaocert/verifyLogin", method = RequestMethod.GET)
    public String verifyLogin(Model m) {

        // 간편로그인 토큰받기 요청시 반환된 txID
        String txID = "01daa94d3f-5ac9-429c-8661-40d0ad9ce3e3";

        try {
            LoginResult result = kakaocertService.verifyLogin(ClientCode, txID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "kakaocert/verifyLogin";
    }

}
