package com.kakaocert.example.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kakaocert.api.BarocertException;
import com.kakaocert.api.KakaocertService;
import com.kakaocert.api.ResponseCMS;
import com.kakaocert.api.ResponseESign;
import com.kakaocert.api.VerifyResult;
import com.kakaocert.api.cms.RequestCMS;
import com.kakaocert.api.cms.ResultCMS;
import com.kakaocert.api.esign.RequestESign;
import com.kakaocert.api.esign.ResultESign;
import com.kakaocert.api.esign.Tokens;
import com.kakaocert.api.verifyauth.RequestVerifyAuth;
import com.kakaocert.api.verifyauth.ResultVerifyAuth;

@Controller
public class KakaocertServiceController {

    @Autowired
    private KakaocertService kakaocertService;

    // 이용기관코드
    // 파트너가 등록한 이용기관의 코드, (파트너 사이트에서 확인가능)
    @Value("${kakaocert.clientCode}")
    private String ClientCode;

    @RequestMapping(value = "checkKakaoServiceAttribute")
    public String CheckKakaoServiceAttribute(Model m) {
        kakaoCert2ModelAttribute(m);
        return "checkKakaoServiceAttribute";
    }

    private void kakaoCert2ModelAttribute(Model m) {
        Field[] fields = kakaocertService.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                m.addAttribute(field.getName(), field.get(kakaocertService));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 카카오톡 사용자에게 전자서명을 요청(단건)합니다.
     * - 
     */
    @RequestMapping(value = "requestESign", method = RequestMethod.GET)
    public String requestESign(Model m) {

        // 전자서명 요청 정보 Object
        RequestESign request = new RequestESign();

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        boolean isAppUseYN = false;

        // 요청번호 40자
        request.setRequestID("kakaocert_202303130000000000000000000005");

        // 수신자 정보(휴대폰번호, 성명, 생년월일)와 Ci 값 중 택일
        request.setReceiverHP("01087674117");
        request.setReceiverName("message to be encrypted");
        request.setReceiverBirthday("19930112");
        // request.setCi("");

        request.setReqTitle("전자서명단건테스트");
        request.setExpireIn(1000);
        request.setToken("전자서명단건테스트데이터");
        request.setTokenType("TEXT"); // TEXT, HASH

        // App to App 방식 이용시, 에러시 호출할 URL
        // request.setReturnURL("https://kakao.barocert.com");

        try {
        	ResponseESign result = kakaocertService.requestESign(ClientCode, request, isAppUseYN);

            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "resultESign";
    }
    
    /*
     * 카카오톡 사용자에게 전자서명을 요청(다건)합니다.
     * - 
     */
    @RequestMapping(value = "bulkRequestESign", method = RequestMethod.GET)
    public String bulkRequestESign(Model m) {

        // 전자서명 요청 정보 Object
    	BulkRequestESign request = new BulkRequestESign();

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        boolean isAppUseYN = false;

        // 요청번호 40자
        request.setRequestID("kakaocert_202303130000000000000000000001");

        // 수신자 정보(휴대폰번호, 성명, 생년월일)와 Ci 값 중 택일
        request.setReceiverHP("01087674117");
        request.setReceiverName("message to be encrypted");
        request.setReceiverBirthday("19930112");
        // request.setCi("");

        request.setReqTitle("전자서명다건테스트");
        request.setExpireIn(1000);

        request.setTokens(new ArrayList<Tokens>());

        Tokens token = new Tokens();
        token.setReqTitle("전자서명다건문서테스트1");
        token.setToken("전자서명다건테스트데이터1");
        request.getTokens().add(token);

        token = new Tokens();
        token.setReqTitle("전자서명다건문서테스트2");
        token.setToken("전자서명다건테스트데이터2");
        request.getTokens().add(token);

        request.setTokenType("TEXT"); // TEXT, HASH

        // App to App 방식 이용시, 에러시 호출할 URL
        // request.setReturnURL("https://kakao.barocert.com");

        try {
        	ResponseESign result = kakaocertService.bulkRequestESign(ClientCode, request, isAppUseYN);

            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "bulkRequestESign";
    }

    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명 상태를 확인(단건)합니다.
     * - 
     */
    @RequestMapping(value = "getESignState", method = RequestMethod.GET)
    public String getESignResult(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "0230309201738000000000000000000000000001";

        try {
        	ResultESign result = kakaocertService.getESignState(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "getESignState";
    }
    
    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명 상태를 확인(다건)합니다.
     * - 
     */
    @RequestMapping(value = "getBulkESignState", method = RequestMethod.GET)
    public String getBulkESignState(Model m) {

        // 전자서명 요청시 반환된 접수아이디
        String receiptID = "0230309201738000000000000000000000000001";

        try {
        	BulkResultESign result = kakaocertService.getBulkESignState(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "getBulkESignState";
    }

    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명을 검증(단건)합니다.
     * - 
     */
    @RequestMapping(value = "verifyESign", method = RequestMethod.GET)
    public String verfiyESign(Model m) {

    	// 전자서명 요청시 반환된 접수아이디
        String receiptID = "0230310143306000000000000000000000000001";

        try {
        	ResultVerifyEsign result = kakaocertService.verifyESign(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "verifyESign";
    }
    
    /*
     * 전자서명 요청시 반환된 접수아이디를 통해 서명을 검증(단건)합니다.
     * - 
     */
    @RequestMapping(value = "bulkVerifyESign", method = RequestMethod.GET)
    public String bulkVerifyESign(Model m) {

    	// 전자서명 요청시 반환된 접수아이디
        String receiptID = "0230310143306000000000000000000000000001";

        try {
        	BulkVerifyResult result = kakaocertService.bulkVerifyESign(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "bulkVerifyESign";
    }

    /*
     * 카카오톡 사용자에게 본인인증 전자서명을 요청합니다.
     * - 
     */
    @RequestMapping(value = "requestVerifyAuth", method = RequestMethod.GET)
    public String requestVerifyAuth(Model m) {

        // 본인인증 요청 정보 Object
        RequestVerifyAuth request = new RequestVerifyAuth();
		
		// 요청번호 40자
		request.setRequestID("kakaocert_202303130000000000000000000001");
		
		// 수신자 정보(휴대폰번호, 성명, 생년월일)와 Ci 값 중 택일
		request.setReceiverHP("01087674117");
		request.setReceiverName("이승환");
		request.setReceiverBirthday("19930112");
		// request.setCi("");
		
		request.setReqTitle("인증요청 메시지 제목란");
		request.setExpireIn(1000);
		request.setToken("본인인증요청토큰");
		
		// App to App 방식 이용시, 에러시 호출할 URL
		// request.setReturnURL("https://kakao.barocert.com");

        try {
            String result = kakaocertService.requestVerifyAuth(ClientCode, request);

            m.addAttribute("Result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "requestVerifyAuth";
    }

    /*
     * 본인인증 요청시 반환된 접수아이디를 통해 서명 상태를 확인합니다.
     * - 
     */
    @RequestMapping(value = "getVerifyAuthState", method = RequestMethod.GET)
    public String getVerifyAuthState(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "023020000003";

        try {
        	ResultVerifyAuthState result = kakaocertService.getVerifyAuthState(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        return "getVerifyAuthState";
    }

    /*
     * 본인인증 요청시 반환된 접수아이디를 통해 본인인증 서명을 검증합니다.
     * - 
     */
    @RequestMapping(value = "verifyAuth", method = RequestMethod.GET)
    public String verifyAuth(Model m) {

        // 본인인증 요청시 반환된 접수아이디
        String receiptID = "020040000001";

        try {
        	ResultVerifyAuth result = kakaocertService.verifyAuth(ClientCode, receiptID);
        	
            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "verifyAuth";
    }

    /*
     * 카카오톡 사용자에게 자동이체 출금동의 전자서명을 요청합니다.
     * - 
     */
    @RequestMapping(value = "requestCMS", method = RequestMethod.GET)
    public String requestCMS(Model m) {

        // 자동이체 출금동의 요청 정보 Object
        RequestCMS request = new RequestCMS();

        // AppToApp 인증요청 여부
        // true - AppToApp 인증방식, false - Talk Message 인증방식
        boolean isAppUseYN = false;

        // 요청번호 40자
		request.setRequestID("kakaocert_202303130000000000000000000001");
		
		// 수신자 정보(휴대폰번호, 성명, 생년월일)와 Ci 값 중 택일
		request.setReceiverHP("이승환");
		request.setReceiverName("01087674117");
		request.setReceiverBirthday("19930112");
		// request.setCi("");
		
		request.setReqTitle("인증요청 메시지 제공란");
		request.setExpireIn(1000);
		request.setRequestCorp("청구 기관명란");
		request.setBankName("출금은행명란");
		request.setBankAccountNum("9-4324-5117-58");
		request.setBankAccountName("예금주명 입력란");
		request.setBankAccountBirthday("19930112");
		request.setBankServiceType("CMS"); // CMS, FIRM, GIRO
		
		// App to App 방식 이용시, 에러시 호출할 URL
		// request.setReturnURL("https://kakao.barocert.com");

        try {
            ResponseCMS result = kakaocertService.requestCMS(ClientCode, request, isAppUseYN);

            m.addAttribute("result", result);
        } catch (BarocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }

        return "requestCMS";
    }

    /*
     * 자동이체 출금동의 요청시 반환된 접수아이디를 통해 서명 상태를 확인합니다.
     * - 
     */
    @RequestMapping(value = "getCMSState", method = RequestMethod.GET)
    public String getCMSState(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "0230309201738000000000000000000000000001";

        try {
            ResultCMS result = kakaocertService.getCMSState(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (KakaocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "getCMSState";
    }

    /*
     * 자동이체 출금동의 요청시 반환된 접수아이디를 통해 서명을 검증합니다.
     * - 
     */
    @RequestMapping(value = "verifyCMS", method = RequestMethod.GET)
    public String verifyCMS(Model m) {

        // 출금동의 요청시 반환된 접수아이디
        String receiptID = "0230309201738000000000000000000000000001";

        try {
            VerifyResult result = kakaocertService.verifyCMS(ClientCode, receiptID);
            
            m.addAttribute("result", result);
        } catch (KakaocertException ke) {
            m.addAttribute("Exception", ke);
            return "exception";
        }
        
        return "verifyCMS";
    }

}
