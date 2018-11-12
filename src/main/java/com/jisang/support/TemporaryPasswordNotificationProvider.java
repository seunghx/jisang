package com.jisang.support;

public interface TemporaryPasswordNotificationProvider {
	
	/**
	 * 
	 * @param email - 이 파라미터는 목적지 정보(이메일 주소)를 의미 하는 것이 아닌, 유저의 id를 의미하는 것이다. email로 임시 비밀 번호를 전송하는 
	 * 		  시나리오에서는 이 파라미터를 그대로 사용하면 되겠지만 다른 시나리오(예를 들어 SMS - 물론 SMS로 임시비밀번호를 전송하는 것이 좋은 방법인지는 
	 * 		  모르겠으나)에서는 이 파라미터를 사용해 임시 비밀번호를 전송할 목적지 정보(예 : 핸드폰 번호)를 가져와야 할 것이다.
	 */
	public void sendTemporaryPassword(String email, String temporaryPassword);
}
