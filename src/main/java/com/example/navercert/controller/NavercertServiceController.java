package com.example.navercert.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.barocert.BarocertException;
import com.barocert.navercert.NavercertService;
import com.barocert.navercert.identity.Identity;
import com.barocert.navercert.identity.IdentityReceipt;
import com.barocert.navercert.identity.IdentityResult;
import com.barocert.navercert.identity.IdentityStatus;
import com.barocert.navercert.sign.MultiSign;
import com.barocert.navercert.sign.MultiSignReceipt;
import com.barocert.navercert.sign.MultiSignResult;
import com.barocert.navercert.sign.MultiSignStatus;
import com.barocert.navercert.sign.MultiSignTokens;
import com.barocert.navercert.sign.Sign;
import com.barocert.navercert.sign.SignReceipt;
import com.barocert.navercert.sign.SignResult;
import com.barocert.navercert.sign.SignStatus;

@Controller
public class NavercertServiceController {

    @Autowired
    private NavercertService navercertService;

    // 이용기관코드
    // 파트너가 등록한 이용기관의 코드 (파트너 사이트에서 확인가능)
    @Value("${navercert.clientCode}")
    private String ClientCode;
    
    /*
     * 네이버 이용자에게 본인인증을 요청합니다.
     * https://developers.barocert.com/reference/naver/java/identity/api#RequestIdentity
     */
    @RequestMapping(value = "navercert/requestIdentity", method = RequestMethod.GET)
    public String requestIdentity(Model m) throws BarocertException {

        // 본인인증 요청 정보 객체
        Identity identity = new Identity();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        identity.setReceiverHP(navercertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        identity.setReceiverName(navercertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        identity.setReceiverBirthday(navercertService.encrypt("19700101"));

        // 고객센터 연락처 - 최대 12자
        identity.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        identity.setExpireIn(1000);

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        identity.setAppUseYN(false);

        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // identity.setDeviceOSType("ANDROID");

        // App to App 방식 이용시, 호출할 URL
        // identity.setReturnURL("navercert://Identity");

        try {
            IdentityReceipt result = navercertService.requestIdentity(ClientCode, identity);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/requestIdentity";
    }

    /*
     * 본인인증 요청 후 반환받은 접수아이디로 본인인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/naver/java/identity/api#GetIdentityStatus
     */
    @RequestMapping(value = "navercert/getIdentityStatus", method = RequestMethod.GET)
    public String getIdentityStatus(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02309060230600000880000000000001";

        try {
            IdentityStatus result = navercertService.getIdentityStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/getIdentityStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 반환받은 전자서명값(signedData)과 [1. RequestIdentity] 함수 호출에 입력한 Token의 동일 여부를 확인하여 이용자의 본인인증 검증을 완료합니다.
     * 네이버 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * https://developers.barocert.com/reference/naver/java/identity/api#VerifyIdentity
     */
    @RequestMapping(value = "navercert/verifyIdentity", method = RequestMethod.GET)
    public String verifyIdentity(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02309060230600000880000000000001";

        try {
            IdentityResult result = navercertService.verifyIdentity(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/verifyIdentity";
    }

    /*
     * 네이버 이용자에게 단건(1건) 문서의 전자서명을 요청합니다.
     * https://developers.barocert.com/reference/naver/java/sign/api-single#RequestSign
     */
    @RequestMapping(value = "navercert/requestSign", method = RequestMethod.GET)
    public String requestSign(Model m) throws BarocertException {

        // 전자서명 요청 정보 객체
        Sign sign = new Sign();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        sign.setReceiverHP(navercertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        sign.setReceiverName(navercertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        sign.setReceiverBirthday(navercertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        sign.setReqTitle("전자서명단건테스트");
        // 고객센터 연락처 - 최대 12자
        sign.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        sign.setExpireIn(1000);
        // 인증요청 메시지 - 최대 500자
        sign.setReqMessage(navercertService.encrypt("전자서명 인증요청 메시지"));
        // 서명 원문 - 원문 2,800자 까지 입력가능
        sign.setToken(navercertService.encrypt("전자서명 단건 테스트 데이터"));
        // 서명 원문 유형
        // TEXT - 일반 텍스트, HASH - HASH 데이터
        sign.setTokenType("TEXT");

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        sign.setAppUseYN(false);

        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // sign.setDeviceOSType("ANDROID");

        // App to App 방식 이용시, 호출할 URL
        // sign.setReturnURL("navercert://Sign");

        try {
            SignReceipt result = navercertService.requestSign(ClientCode, sign);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/requestSign";
    }

    /*
     * 전자서명(단건) 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/naver/java/sign/api-single#GetSignStatus
     */
    @RequestMapping(value = "navercert/getSignStatus", method = RequestMethod.GET)
    public String getSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02309060230600000880000000000002";

        try {
            SignStatus result = navercertService.getSignStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/getSignStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 네이버 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * https://developers.barocert.com/reference/naver/java/sign/api-single#VerifySign
     */
    @RequestMapping(value = "navercert/verifySign", method = RequestMethod.GET)
    public String verfiySign(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02309060230600000880000000000002";

        try {
            SignResult result = navercertService.verifySign(ClientCode, receiptID);
            m.addAttribute("result", result);

        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/verifySign";
    }

    /*
     * 네이버 이용자에게 복수(최대 50건) 문서의 전자서명을 요청합니다.
     * https://developers.barocert.com/reference/naver/java/sign/api-multi#RequestMultiSign
     */
    @RequestMapping(value = "navercert/requestMultiSign", method = RequestMethod.GET)
    public String requestMultiSign(Model m) throws BarocertException {

        // 전자서명 요청 정보 객체
        MultiSign multiSign = new MultiSign();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        multiSign.setReceiverHP(navercertService.encrypt("01012341234"));
        // 수신자 성명 - 80자
        multiSign.setReceiverName(navercertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        multiSign.setReceiverBirthday(navercertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        multiSign.setReqTitle("전자서명복수테스트");
        // 고객센터 연락처 - 최대 12자
        multiSign.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력
        multiSign.setExpireIn(1000);
        // 인증요청 메시지 - 최대 500자
        multiSign.setReqMessage(navercertService.encrypt("전자서명 인증요청 메시지"));

        // 개별문서 등록 - 최대 50 건
        // 개별 요청 정보 객체
        MultiSignTokens token = new MultiSignTokens();
        // 서명 원문 - 원문 2,800자 까지 입력가능
        token.setToken(navercertService.encrypt("전자서명복수테스트데이터1"));
        // 서명 원문 유형
        // 'TEXT' - 일반 텍스트, 'HASH' - HASH 데이터
        token.setTokenType("TEXT");

        multiSign.addToken(token);

        // 개별 요청 정보 객체
        MultiSignTokens token2 = new MultiSignTokens();
        // 서명 원문 - 원문 2,800자 까지 입력가능
        token2.setToken(navercertService.encrypt("전자서명복수테스트데이터2"));
        // 서명 원문 유형
        // 'TEXT' - 일반 텍스트, 'HASH' - HASH 데이터
        token2.setTokenType("TEXT");

        multiSign.addToken(token2);

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        multiSign.setAppUseYN(false);

        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // multiSign.setDeviceOSType("ANDROID");

        // App to App 방식 이용시, 에러시 호출할 URL
        // multiSign.setReturnURL("navercert://Sign");

        try {
            MultiSignReceipt result = navercertService.requestMultiSign(ClientCode, multiSign);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/requestMultiSign";
    }

    /*
     * 전자서명(복수) 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * https://developers.barocert.com/reference/naver/java/sign/api-multi#GetMultiSignStatus
     */
    @RequestMapping(value = "navercert/getMultiSignStatus", method = RequestMethod.GET)
    public String getMultiSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02309060230600000880000000000003";

        try {
            MultiSignStatus result = navercertService.getMultiSignStatus(ClientCode, receiptID);
            m.addAttribute("result", result);

        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/getMultiSignStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 네이버 보안정책에 따라 검증 API는 1회만 호출할 수 있습니다. 재시도시 오류가 반환됩니다.
     * https://developers.barocert.com/reference/naver/java/sign/api-multi#VerifyMultiSign
     */
    @RequestMapping(value = "navercert/verifyMultiSign", method = RequestMethod.GET)
    public String verifyMultiSign(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02309060230600000880000000000003";

        try {
            MultiSignResult result = navercertService.verifyMultiSign(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "navercert/verifyMultiSign";
    }
}
