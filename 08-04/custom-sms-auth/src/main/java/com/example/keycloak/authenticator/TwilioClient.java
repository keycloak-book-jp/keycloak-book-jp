package com.example.keycloak.authenticator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Twilio SMSサービスクライアントクラス
 */
public class TwilioClient {

	private static final Logger logger = Logger.getLogger(TwilioClient.class);

	/** Twilio API ベースURL */
	public static final String DEFAULT_API_URI = "https://verify.twilio.com/v2/Services/";
	/** Twilio API 認証コード送信パス */
	public static final String PHONE_VERIFICATION_SEND_API_PATH = "/Verifications";
	/** Twilio API 認証コード確認パス */
	public static final String PHONE_VERIFICATION_CHECK_API_PATH = "/VerificationCheck";
	/** 電話番号 日本の国番号 */
	public static final String COUNTRY_CODE = "+81";
	/* HTTPクライアントのコネクションタイムアウト */
	private static final int CONNECTION_TIMEOUT = 10000;
	/* HTTPクライアントのソケットタイムアウト */
	private static final int SOCKET_TIMEOUT = 30500;
	/* Twilio Service SID */
	private final String serviceSid;
	/* Twilio Account SID */
	private final String accountSid;
	/* Twilio Auth Token */
	private final String authToken;

	/*
	 * コンストラクタ
	 */
	public TwilioClient(String serviceSid, String accountSid, String authToken) {
		this.serviceSid = serviceSid;
		this.accountSid = accountSid;
		this.authToken = authToken;
	}

	/**
	 * SMSで認証コードを送信する
	 *
	 * @param telNum 電話番号
	 * @return SMS送信ができればtrue
	 */
	public boolean sendSMS(String telNum) {
		logger.debug("sendSMS start");
		RequestBuilder param = RequestBuilder.post()
			.setUri(DEFAULT_API_URI + serviceSid + PHONE_VERIFICATION_SEND_API_PATH)
			.setVersion(HttpVersion.HTTP_1_1)
			.setCharset(StandardCharsets.UTF_8);
		param.addHeader(HttpHeaders.ACCEPT, "application/json");
		param.addHeader(HttpHeaders.ACCEPT_ENCODING, "utf-8");
		param.addHeader(HttpHeaders.AUTHORIZATION, getAuthString(accountSid, authToken));
		param.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		param.addParameter("To", COUNTRY_CODE + telNum);
		param.addParameter("Channel", "sms");

		HttpResponse response = request(param);
		if (response == null) return false;
		int statusCode = response.getStatusLine().getStatusCode();
		boolean isSuccess = statusCode >= 200 && statusCode <= 299;
		if (isSuccess) {
			logger.debugv("sendSMS Status Code: {0}", statusCode);
			logger.debugv("sendSMS JSON String: {0}", getString(response));
		} else {
			logger.error("sendSMS Failed to send SMS.");
			logger.errorv("sendSMS Status Code: {0}", statusCode);
			logger.errorv("sendSMS JSON String: {0}", getString(response));
		}
		logger.debug("sendSMS end");
		return isSuccess;
	}

	/**
	 * 認証コードが一致するか確認する
	 *
	 * @param telNum 電話番号
	 * @param code 認証コード
	 * @return 認証コードが一致すればtrue
	 */
	public boolean verifySMS(String telNum, String code) {
		logger.debug("verifySMS start");
		if (code == null || code.isEmpty() || code.length() > 10) return false;

		RequestBuilder param = RequestBuilder.post()
				.setUri(DEFAULT_API_URI + serviceSid + PHONE_VERIFICATION_CHECK_API_PATH)
				.setVersion(HttpVersion.HTTP_1_1)
				.setCharset(StandardCharsets.UTF_8);
		param.addHeader(HttpHeaders.ACCEPT, "application/json");
		param.addHeader(HttpHeaders.ACCEPT_ENCODING, "utf-8");
		param.addHeader(HttpHeaders.AUTHORIZATION, getAuthString(accountSid, authToken));
		param.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		param.addParameter("To", COUNTRY_CODE + telNum);
		param.addParameter("Code", code);

		HttpResponse response = request(param);
		if (response == null) return false;
		int statusCode = response.getStatusLine().getStatusCode();
		logger.debugv("verifySMS Status Code: {0}", statusCode);
		String jsonString = getString(response);
		if (jsonString == null) return false;
		logger.debugv("verifySMS JSON String: {0}", jsonString);
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			return statusCode >= 200 && statusCode <= 299 && "approved".equals(jsonObject.getString("status"))
					&& jsonObject.getBoolean("valid");
		} catch (JSONException e) {
			logger.error(e);
		} finally {
			logger.debug("verifySMS end");
		}
		return false;
	}

	/**
	 *  HTTP リクエスト送信
	 */
	private HttpResponse request(final RequestBuilder param) {
		try {
			HttpClient client = buildHttpClient();
			return client.execute(param.build());
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	/*
	 * HTTP クライアント作成
	 */
	private HttpClient buildHttpClient() {
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(CONNECTION_TIMEOUT)
			.setSocketTimeout(SOCKET_TIMEOUT)
			.build();

		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		clientBuilder.useSystemProperties();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(10);
		connectionManager.setMaxTotal(10 * 2);
		clientBuilder
			.setConnectionManager(connectionManager)
			.setDefaultRequestConfig(config);

		return clientBuilder.build();
	}

	/*
	 * エンコードした認証情報を返す
	 */
	private String getAuthString(String accountSid, String authToken) {
		String credentials = accountSid + ":" + authToken;
		return "Basic " + DatatypeConverter.printBase64Binary(credentials.getBytes(StandardCharsets.US_ASCII));
	}

	/*
	 * レスポンスデータ処理（json文字列に変換）
	 */
	private String getString(HttpResponse response) {
		HttpEntity entity = response.getEntity();
		if (entity == null) return null;
		try (InputStream stream = new BufferedHttpEntity(entity).getContent()) {
			return getContent(stream);
		} catch (final IOException e) {
			logger.error(e);
			return null;
		}
	}

	/**
	 * 入力ストリームを文字配列に変換
	 */
	private String getContent(InputStream stream) {
		if (stream != null) {
			try (Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A")) {
				if (scanner.hasNext()) {
					return scanner.next();
				}
			}
		}
		return null;
	}
}
