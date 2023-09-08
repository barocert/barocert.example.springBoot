package com.example.passcert.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.barocert.BarocertException;
import com.barocert.passcert.PasscertService;
import com.barocert.passcert.cms.CMS;
import com.barocert.passcert.cms.CMSReceipt;
import com.barocert.passcert.cms.CMSResult;
import com.barocert.passcert.cms.CMSStatus;
import com.barocert.passcert.cms.CMSVerify;
import com.barocert.passcert.identity.Identity;
import com.barocert.passcert.identity.IdentityReceipt;
import com.barocert.passcert.identity.IdentityResult;
import com.barocert.passcert.identity.IdentityStatus;
import com.barocert.passcert.identity.IdentityVerify;
import com.barocert.passcert.login.Login;
import com.barocert.passcert.login.LoginReceipt;
import com.barocert.passcert.login.LoginResult;
import com.barocert.passcert.login.LoginStatus;
import com.barocert.passcert.login.LoginVerify;
import com.barocert.passcert.sign.Sign;
import com.barocert.passcert.sign.SignReceipt;
import com.barocert.passcert.sign.SignResult;
import com.barocert.passcert.sign.SignStatus;
import com.barocert.passcert.sign.SignVerify;

@Controller
public class PasscertServiceController {

    @Autowired
    private PasscertService passcertService;

    // 이용기관코드
    // 파트너가 등록한 이용기관의 코드 (파트너 사이트에서 확인가능)
    @Value("${passcert.clientCode}")
    private String ClientCode;
    
    /*
     * 패스 이용자에게 본인인증을 요청합니다.
     * https://developers.barocert.com/reference/pass/java/identity/api#RequestIdentity
     */
    @RequestMapping(value = "passcert/requestIdentity", method = RequestMethod.GET)
    public String requestIdentity(Model m) throws BarocertException {

        // 본인인증 요청 정보 객체
        Identity identity = new Identity();
        
        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        identity.setReceiverHP(passcertService.encrypt("01012341234"));
        // 수신자 성명 - 최대 80자
        identity.setReceiverName(passcertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        identity.setReceiverBirthday(passcertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        identity.setReqTitle("인증요청 메시지 제목");
        // 인증요청 메시지 내용 - 최대 500자
        identity.setReqMessage(passcertService.encrypt("인증요청 메시지 내용"));
        // 고객센터 연락처 - 최대 12자
        identity.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        identity.setExpireIn(1000);
        // 서명 원문 
        identity.setToken(passcertService.encrypt("본인인증요청토큰"));

        // 사용자 동의 필요 여부
        identity.setUserAgreementYN(true);
        // 사용자 정보 포함 여부
        identity.setReceiverInfoYN(true);
        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        identity.setAppUseYN(false);
        // ApptoApp 인증방식에서 사용
        // 통신사 유형('SKT', 'KT', 'LGU'), 대문자 입력(대소문자 구분)
        // identity.setTelcoType("SKT");
        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // identity.setDeviceOSType("IOS");
        
        try {
            IdentityReceipt result = passcertService.requestIdentity(ClientCode, identity);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/requestIdentity";
    }

    /*
     * 본인인증 요청 후 반환받은 접수아이디로 본인인증 진행 상태를 확인합니다.
     * 상태확인 함수는 본인인증 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 본인인증 요청 함수를 호출한 당일 23시 59분 59초 이후 상태확인 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/identity/api#GetIdentityStatus
     */
    @RequestMapping(value = "passcert/getIdentityStatus", method = RequestMethod.GET)
    public String getIdentityStatus(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000023";

        try {
            IdentityStatus result = passcertService.getIdentityStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/getIdentityStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 반환받은 전자서명값(signedData)과 [1. RequestIdentity] 함수 호출에 입력한 Token의 동일 여부를 확인하여 이용자의 본인인증 검증을 완료합니다.
     * 검증 함수는 본인인증 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 본인인증 요청 함수를 호출한 당일 23시 59분 59초 이후 검증 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/identity/api#VerifyIdentity
     */
    @RequestMapping(value = "passcert/verifyIdentity", method = RequestMethod.GET)
    public String verifyIdentity(Model m) throws BarocertException {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000023";

        // 서명검증 요청 정보 객체
        IdentityVerify request = new IdentityVerify();
        // 서명검증 요청 휴대폰번호 - 11자 (하이픈 제외)
        request.setReceiverHP(passcertService.encrypt("01012341234")); 
        // 서명검증 요청 성명 - 최대 80자
        request.setReceiverName(passcertService.encrypt("홍길동")); 

        try {
            IdentityResult result = passcertService.verifyIdentity(ClientCode, receiptID, request);
            m.addAttribute("result", result);

        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/verifyIdentity";
    }

    /*
     * 패스 이용자에게 문서의 전자서명을 요청합니다.
     * https://developers.barocert.com/reference/pass/java/sign/api#RequestSign
     */
    @RequestMapping(value = "passcert/requestSign", method = RequestMethod.GET)
    public String requestSign(Model m) throws BarocertException {
        
        // 전자서명 요청 정보 객체
        Sign sign = new Sign();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        sign.setReceiverHP(passcertService.encrypt("01012341234"));
        // 수신자 성명 - 최대 80자
        sign.setReceiverName(passcertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        sign.setReceiverBirthday(passcertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        sign.setReqTitle("전자서명 테스트");
        // 인증요청 메시지 - 최대 500자
        sign.setReqMessage(passcertService.encrypt("패스써트 전자서명 인증요청 메시지"));
        // 고객센터 연락처 - 최대 12자
        sign.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        sign.setExpireIn(1000);
        // 서명 원문 - 원문 2,800자 까지 입력가능
        sign.setToken(passcertService.encrypt("전자서명 테스트 데이터"));
        // 서명 원문 유형
        // 'TEXT' - 일반 텍스트, 'HASH' - HASH 데이터, 'URL' - URL 데이터
        // 원본데이터(originalTypeCode, originalURL, originalFormatCode) 입력시 'TEXT'사용 불가
        sign.setTokenType("URL");

        // 사용자 동의 필요 여부
        sign.setUserAgreementYN(true);
        // 사용자 정보 포함 여부
        sign.setReceiverInfoYN(true);
        
        // 원본유형코드
        // 'AG' - 동의서, 'AP' - 신청서, 'CT' - 계약서, 'GD' - 안내서, 'NT' - 통지서, 'TR' - 약관
        sign.setOriginalTypeCode("TR");
        // 원본조회URL
        sign.setOriginalURL("https://www.passcert.co.kr");
        // 원본형태코드
        // ('TEXT', 'HTML', 'DOWNLOAD_IMAGE', 'DOWNLOAD_DOCUMENT')
        sign.setOriginalFormatCode("HTML");
        
        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        sign.setAppUseYN(false);
        // ApptoApp 인증방식에서 사용
        // 통신사 유형('SKT', 'KT', 'LGU'), 대문자 입력(대소문자 구분)
        // sign.setTelcoType("SKT");
        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // sign.setDeviceOSType("IOS");
        
        try {
            SignReceipt result = passcertService.requestSign(ClientCode, sign);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/requestSign";
    }

    /*
     * 전자서명 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * 상태확인 함수는 전자서명 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 전자서명 요청 함수를 호출한 당일 23시 59분 59초 이후 상태확인 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/sign/api#GetSignStatus
     */
    @RequestMapping(value = "passcert/getSignStatus", method = RequestMethod.GET)
    public String getSignStatus(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000024";

        try {
            SignStatus result = passcertService.getSignStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/getSignStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 검증 함수는 전자서명 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 전자서명 요청 함수를 호출한 당일 23시 59분 59초 이후 검증 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/sign/api#VerifySign
     */
    @RequestMapping(value = "passcert/verifySign", method = RequestMethod.GET)
    public String verfiySign(Model m) throws BarocertException {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000024";

        // 서명검증 요청 정보 객체
        SignVerify request = new SignVerify();
        // 서명검증 요청자 휴대폰번호 - 11자 (하이픈 제외)
        request.setReceiverHP(passcertService.encrypt("01012341234")); 
        // 서명검증 요청자 성명 - 최대 80자
        request.setReceiverName(passcertService.encrypt("홍길동"));
        
        try {
            SignResult result = passcertService.verifySign(ClientCode, receiptID, request);
            m.addAttribute("result", result);

        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/verifySign";
    }
    
    /*
     * 패스 이용자에게 자동이체 출금동의를 요청합니다.
     * https://developers.barocert.com/reference/pass/java/cms/api#RequestCMS
     */
    @RequestMapping(value = "passcert/requestCMS", method = RequestMethod.GET)
    public String requestCMS(Model m) throws BarocertException {

        // 출금동의 요청 정보 객체
        CMS cms = new CMS();

        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        cms.setReceiverHP(passcertService.encrypt("01012341234"));
        // 수신자 성명 - 최대 80자
        cms.setReceiverName(passcertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        cms.setReceiverBirthday(passcertService.encrypt("19700101"));
        // 인증요청 메시지 제목 - 최대 40자
        cms.setReqTitle("출금동의 테스트");
        // 인증요청 메시지 - 최대 500자
        cms.setReqMessage(passcertService.encrypt("패스써트 출금동의 인증요청 메시지"));
        // 고객센터 연락처 - 최대 12자
        cms.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        cms.setExpireIn(1000);
        
        // 사용자 동의 필요 여부
        cms.setUserAgreementYN(true);
        // 사용자 정보 포함 여부
        cms.setReceiverInfoYN(true);
        // 출금은행명 - 최대 100자
        cms.setBankName(passcertService.encrypt("국민은행"));
        // 출금계좌번호 - 최대 31자
        cms.setBankAccountNum(passcertService.encrypt("9-****-5117-58"));
        // 출금계좌 예금주명 - 최대 100자
        cms.setBankAccountName(passcertService.encrypt("홍길동"));
        // 출금유형 
        // CMS - 출금동의, OPEN_BANK - 오픈뱅킹
        cms.setBankServiceType(passcertService.encrypt("CMS"));
        // 출금액
        cms.setBankWithdraw(passcertService.encrypt("1,000,000원"));

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        cms.setAppUseYN(false);
        // ApptoApp 인증방식에서 사용
        // 통신사 유형('SKT', 'KT', 'LGU'), 대문자 입력(대소문자 구분)
        // request.setTelcoType("SKT");
        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // request.setDeviceOSType("IOS");
        
        try{
            CMSReceipt result = passcertService.requestCMS(ClientCode, cms);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/requestCMS";
    }

    /*
     * 자동이체 출금동의 요청 후 반환받은 접수아이디로 인증 진행 상태를 확인합니다.
     * 상태확인 함수는 자동이체 출금동의 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 자동이체 출금동의 요청 함수를 호출한 당일 23시 59분 59초 이후 상태확인 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/cms/api#GetCMSStatus
     */
    @RequestMapping(value = "passcert/getCMSStatus", method = RequestMethod.GET)
    public String getCMSStatus(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000025";

        try {
            CMSStatus result = passcertService.getCMSStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/getCMSStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 검증 함수는 자동이체 출금동의 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 자동이체 출금동의 요청 함수를 호출한 당일 23시 59분 59초 이후 검증 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/cms/api#VerifyCMS
     */
    @RequestMapping(value = "passcert/verifyCMS", method = RequestMethod.GET)
    public String verifyCMS(Model m) throws BarocertException {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000025";

        // 서명검증 요청 정보 객체
        CMSVerify request = new CMSVerify(); 
        // 서명검증 요청자 휴대폰번호 - 11자 (하이픈 제외)
        request.setReceiverHP(passcertService.encrypt("01012341234")); 
        // 서명검증 요청자 성명 - 최대 80자
        request.setReceiverName(passcertService.encrypt("홍길동"));

        try {
            CMSResult result = passcertService.verifyCMS(ClientCode, receiptID, request);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/verifyCMS";
    }

    /*
     * 패스 이용자에게 간편로그인을 요청합니다.
     * https://developers.barocert.com/reference/pass/java/login/api#RequestLogin
     */
    @RequestMapping(value = "passcert/requestLogin", method = RequestMethod.GET)
    public String requestLogin(Model m) throws BarocertException {

        // 본인인증 요청 정보 객체
        Login login = new Login();
        
        // 수신자 휴대폰번호 - 11자 (하이픈 제외)
        login.setReceiverHP(passcertService.encrypt("01012341234"));
        // 수신자 성명 - 최대 80자
        login.setReceiverName(passcertService.encrypt("홍길동"));
        // 수신자 생년월일 - 8자 (yyyyMMdd)
        login.setReceiverBirthday(passcertService.encrypt("19700101"));

        // 인증요청 메시지 제목 - 최대 40자
        login.setReqTitle("간편로그인 테스트");
        // 인증요청 메시지 내용 - 최대 500자
        login.setReqMessage(passcertService.encrypt("패스써트 간편로그인 인증요청 메시지"));
        // 고객센터 연락처 - 최대 12자
        login.setCallCenterNum("1600-9854");
        // 인증요청 만료시간 - 최대 1,000(초)까지 입력 가능
        login.setExpireIn(1000);
        // 서명 원문 
        login.setToken(passcertService.encrypt("본인인증요청토큰"));

        // 사용자 동의 필요 여부
        login.setUserAgreementYN(false);
        // 사용자 정보 포함 여부
        login.setReceiverInfoYN(true);
        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        login.setAppUseYN(false);
        // ApptoApp 인증방식에서 사용
        // 통신사 유형('SKT', 'KT', 'LGU'), 대문자 입력(대소문자 구분)
        // login.setTelcoType("SKT");
        // ApptoApp 인증방식에서 사용
        // 모바일장비 유형('ANDROID', 'IOS'), 대문자 입력(대소문자 구분)
        // login.setDeviceOSType("IOS");
        
        try {
            LoginReceipt result = passcertService.requestLogin(ClientCode, login);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/requestLogin";
    }

    /*
     * 간편로그인 요청 후 반환받은 접수아이디로 진행 상태를 확인합니다.
     * 상태확인 함수는 간편로그인 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 간편로그인 요청 함수를 호출한 당일 23시 59분 59초 이후 상태확인 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/login/api#GetLoginStatus
     */
    @RequestMapping(value = "passcert/getLoginStatus", method = RequestMethod.GET)
    public String getLoginStatus(Model m) {

        // 간편로그인 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000026";

        try {
            LoginStatus result = passcertService.getLoginStatus(ClientCode, receiptID);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/getLoginStatus";
    }

    /*
     * 완료된 전자서명을 검증하고 전자서명값(signedData)을 반환 받습니다.
     * 검증 함수는 간편로그인 요청 함수를 호출한 당일 23시 59분 59초까지만 호출 가능합니다.
     * 간편로그인 요청 함수를 호출한 당일 23시 59분 59초 이후 검증 함수를 호출할 경우 오류가 반환됩니다.
     * https://developers.barocert.com/reference/pass/java/login/api#VerifyLogin
     */
    @RequestMapping(value = "passcert/verifyLogin", method = RequestMethod.GET)
    public String verifyLogin(Model m) throws BarocertException {

        // 간편로그인 요청시 반환된 접수아이디
        String receiptID = "02307240230600000440000000000026";

        // 서명검증 요청 정보 객체
        LoginVerify request = new LoginVerify();
        // 서명검증 요청 휴대폰번호 - 11자 (하이픈 제외)
        request.setReceiverHP(passcertService.encrypt("01012341234")); 
        // 서명검증 요청 성명 - 최대 80자
        request.setReceiverName(passcertService.encrypt("홍길동")); 

        try {
            LoginResult result = passcertService.verifyLogin(ClientCode, receiptID, request);
            m.addAttribute("result", result);
        } catch (BarocertException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "passcert/verifyLogin";
    }

}