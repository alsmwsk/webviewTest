package com.example.tgkim.webviewtest;

import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.HttpURLConnection;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {

    // http://bluelink.connected-car.io/private/v1/user/social/naver
    // https://nid.naver.com/oauth2.0/authorize?client_id=pqCUssd6jHqWhxMF2nNM&redirect_uri=http%3A%2F%2Fbluelink.connected-car.io%3A80%2Fweb%2Fsocial%2Fnaver%2Fcallback&response_type=code&state=abb91b52-964e-45ab-8994-a3c42bc5eef9
    // 네이버 로그인 했을 경우
    //https://nid.naver.com/oauth2.0/authorize?response_type=code&oauth_token=4TE4bLIxtQ80e7p1&state=abb91b52-964e-45ab-8994-a3c42bc5eef9&client_id=pqCUssd6jHqWhxMF2nNM&redirect_uri=http%3A%2F%2Fbluelink.connected-car.io%3A80%2Fweb%2Fsocial%2Fnaver%2Fcallback&locale=&inapp_view=&oauth_os=(null)
    //
    WebView webView;
    WebSettings webSettings;
    String code = "";
    static String DeviceID = "";
    static final String pushRegId = "APA91bHT3wTnIE1mFWT_R8u9XTyttR8Z6takb3yNiBZKouzrG9RY-HPA9_NkMSCt-JXlK9Kosx4bcj4b5ZMM-VX4Vbhb3ivI7TKkgLvYfJEZik_734UPBfwUaDA07r-I-Mi4s-wQMUtV";

    private long backPressedTime = 0;
    private final long FINISH_INTERVAL_TIME = 2000;

    static final String ACCOUNT_STATE = "smart_entry_account_01"; // 임의로 지정한 값..?
    static final String CLIENT_ID = "25fa8900-60b0-4f5d-802b-04c7168f64ea";
    static final String client_secret = "secret";

    static final String GET_OAUTH_URL = "http://bluelink.connected-car.io/api/v1/user/oauth2/authorize"; //auth2 인증 url
    static final String REDIRECT_URI = "http://bluelink.connected-car.io/api/v1/user/oauth2/redirect";
    static final String POST_TOKEN_URL = "http://bluelink.connected-car.io/api/v1/user/oauth2/token";
    static final String GET_USER_PROFILE_URL = "http://bluelink.connected-car.io/api/v1/user/profile";
    static final String POST_PUSH_DEVICE_REGISTRATION_URL = "http://bluelink.connected-car.io/api/v1/spa/notifications/register";
    static final String SIGN_URL = "http://bluelink.connected-car.io/signin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        webView = findViewById(R.id.loginWebView);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                Log.d(TAG, "onPageStarted: " + url);

                AlertDialog alertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                alertDialog = builder.setMessage(url).setPositiveButton("확인",null).create();
                alertDialog.show();

            }

            //로그인 화면에서 로그인을 누르거나 맨처음에 로딩창이 나오고 로그인화면으로 바뀔때 실행된다.
            //매개변수 안의 url은 넘어가고자 하는 주소값
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Toast.makeText(LoginActivity.this, "onPageFinished : view : " + view + " url : " + url , Toast.LENGTH_LONG).show();
                Log.d(TAG, "onPageFinished: url : " + url);
                System.out.println(url);

                // 앱을 시작하자마자 제일 먼저 호출됨
                // http://bluelink.connected-car.io/api/v1/user/oauth2/authorize?response_type=code&client_id=25fa8900-60b0-4f5d-802b-04c7168f64ea&redirect_uri=http://bluelink.connected-car.io/api/v1/user/oauth2/redirect&state=smart_entry_account_01
                // authcode 요청 하는부분
                if (url.contains(REDIRECT_URI)){
                    // "http://bluelink.connected-car.io/api/v1/user/oauth2/redirect"
                    // 이 url안에 state도 포함되어서 전달되는 모양
                    Log.d(TAG, "onPageFinished: redirect_url" + REDIRECT_URI);
                    int idx = url.indexOf("state"); // 이해하기 힘든부분 state가 몇번째 인덱스에서 시작하는지 알려줌 시작인덱스는 0부터 없으면 -1 리턴함
                    String state = url.substring(idx + 6); // 이해하기 힘든부분
                                                           // url의 state 시작 인덱스부터 +6 한 인덱스부터 끝까지의 값이 state 여기서는 예시로
                    //smart_entry_account_01 스테이트의 현재 들어가 있는 값

                    // idx < 0 이라는 state라는 문자열이 없다는 뜻
                    // GET /api/v1/user/oauth2/authorize?response_type=code&client_id=123456789&redirect_uri=https%3A%2F%2Fwww.example.com%2Fauth%2Fbluelink&state=r_basicprofile&lang=de
                    // webView.loadUrl(GET_OAUTH_URL + "?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&state=" + ACCOUNT_STATE); // 1차적으로 OAUTH2 인증 후 redirect 페이지로 이동
                    if (idx < 0 || !state.equals(ACCOUNT_STATE)) { // 앞서 입력한 state 값이 정상적인지 체크.
                        Toast.makeText(getApplicationContext(), "잘못된 접근입니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    HttpURLConnection

                    /*
                    idx = url.indexOf("code");
                    code = url.substring(idx+5, idx + 5 + 22); // 인증 코드 18자리 oauth code

                    */
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.setNetworkAvailable(true);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // DomStorage?
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        webView.loadUrl(GET_OAUTH_URL + "?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&state=" + ACCOUNT_STATE); // 1차적으로 OAUTH2 인증 후 redirect 페이지로 이동
        //로그인 페이지에 들어가기전에 oauth 인증을 받고 그다음에 redirect 페이지로 이동 맞나.?
        //http://bluelink.connected-car.io/api/v1/user/oauth2/authorize?response_type=code&client_id=25fa8900-60b0-4f5d-802b-04c7168f64ea&redirect_uri=
        //http://bluelink.connected-car.io/api/v1/user/oauth2/redirect&state=smart_entry_account_01
        //redirect : http://bluelink.connected-car.io/web/authorize
        //그다음에 signin : http://bluelink.connected-car.io/signin

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        webView.loadUrl(GET_OAUTH_URL + "?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&state=" + ACCOUNT_STATE);
//                    }
//                });
//            }
//        }).start();


    }

    public class MyJavascriptInterface {

        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            System.out.println(html);
        }
    }

    //뒤로가기 키 누르면 앱종료
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if ( (keyCode == KeyEvent.KEYCODE_BACK) && (webView.getUrl().equals(SIGN_URL)) ){
            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                super.onBackPressed();
                return false;
            } else {
                backPressedTime = tempTime;
                Toast.makeText(this, "뒤로 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                keyCode = KeyEvent.KEYCODE_UNKNOWN;
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            if (webView.getOriginalUrl().equals(SIGN_URL)){
                webView.loadUrl(GET_OAUTH_URL + "?response_type=code&client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&state=" + ACCOUNT_STATE);
            }
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_BACK) && !(webView.canGoBack())) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }
}
