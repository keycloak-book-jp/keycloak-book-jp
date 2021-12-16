package com.example.keycloak.authenticator;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

/**
 * Twilio SMS Authenticator
 */
public class SMSAuthenticator implements Authenticator {

	/** 電話番号の属性名 */
	public static final String ATTR_PHONE_NUMBER = "phoneNumber";

	/** Twilio サービスSID */
	public static final String CONFIG_SMS_SERVICE_SID = "verifySMS.service-sid";

	/** Twilio アカウントSID */
	public static final String CONFIG_SMS_ACCOUNT_SID = "verifySMS.account-sid";

	/** Twilio API認証トークン */
	public static final String CONFIG_SMS_AUTH_TOKEN = "verifySMS.auth-token";

	private static final Logger logger = Logger.getLogger(SMSAuthenticator.class);

	/**
	 * 認証処理（認証コード入力画面を表示する直前に行われる処理）
	 *
	 * @param context 認証フローコンテキスト
	 */
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		logger.debug("authenticate start");

		Response challenge;
		UserModel user = context.getUser();

		/* ロケール選択を変更した場合もauthenticate()メソッドが呼び出されるため、OTPを送信しないようにここで終了させる */
		String locale = user.getFirstAttribute("locale");
		MultivaluedMap<String, String> queryParams = context.getHttpRequest().getUri().getQueryParameters();
		String kc_locale = queryParams.getFirst("kc_locale");
		if (kc_locale != null && !kc_locale.equals(locale)) {
			logger.debug("locale is changed");
			logger.debug("authenticate end");
			challenge = context.form().createForm("sms-validation.ftl");
			context.challenge(challenge);
			return;
		}

		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		String phoneNumber = user.getFirstAttribute(ATTR_PHONE_NUMBER);
		if (phoneNumber != null) {
			TwilioClient twilioClient = new TwilioClient(
					getConfigString(config, CONFIG_SMS_SERVICE_SID),
					getConfigString(config, CONFIG_SMS_ACCOUNT_SID),
					getConfigString(config, CONFIG_SMS_AUTH_TOKEN));
			// 認証コード送信
			if (twilioClient.sendSMS(phoneNumber)) {
				// 認証コード入力画面を返却する
				challenge = context.form().createForm("sms-validation.ftl");
			} else {
				// 認証コードの送信に失敗した場合、エラー画面を返却する
				challenge = context.form().addError(new FormMessage("sendSMSCodeErrorMessage"))
						.createForm("sms-validation-error.ftl");
			}
		} else {
			// 電話番号が設定されていない場合、エラー画面を返却する
			challenge = context.form().addError(new FormMessage("missingTelNumberMessage"))
					.createForm("sms-validation-error.ftl");
		}
		context.challenge(challenge);
		logger.debug("authenticate end");
	}

	/**
	 * アクション処理（認証コード入力画面の「Sign in」ボタン押下時の処理）
	 *
	 * @param context 認証フローコンテキスト
	 */
	@Override
	public void action(AuthenticationFlowContext context) {
		logger.debug("action start");

		MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
		String enteredCode = inputData.getFirst("smsCode");

		UserModel user = context.getUser();
		String phoneNumber = user.getFirstAttribute(ATTR_PHONE_NUMBER);

		// 認証コードが正しいか確認する
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		TwilioClient twilioClient = new TwilioClient(
				getConfigString(config, CONFIG_SMS_SERVICE_SID),
				getConfigString(config, CONFIG_SMS_ACCOUNT_SID),
				getConfigString(config, CONFIG_SMS_AUTH_TOKEN) );
		if (twilioClient.verifySMS(phoneNumber, enteredCode)) {
			// 認証コードの確認に成功した場合は、認証成功とする
			context.success();
		} else {
			// 認証コードの確認に失敗した場合は、エラー画面を返却する
			Response challenge = context.form()
					.setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername())
					.addError(new FormMessage("invalidSMSCodeMessage")).createForm("sms-validation-error.ftl");
			context.challenge(challenge);
		}
		logger.debug("action end");
	}

	/*
	 * ユーザーがすでに識別されている必要があるかを返すメソッド
	 */
	@Override
	public boolean requiresUser() {
		return true;
	}

	/*
	 * このAuthenticatorがユーザーに設定されているかを返すメソッド
	 */
	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	/*
	 * ユーザーが実行する必要のある必須アクションを登録するメソッド
	 */
	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
	}

	/*
	 * このクラスが終了する時に動作するメソッド
	 */
	@Override
	public void close() {
	}

	/*
	 * 認証フローから設定値を取得する
	 */
	private String getConfigString(AuthenticatorConfigModel config, String configName) {
		String value = null;
		if (config.getConfig() != null) {
			value = config.getConfig().get(configName);
		}
		return value;
	}
}
